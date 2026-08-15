package dev.iliv007.ivai.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Base64

/**
 * A minimal encryption boundary for future provider credentials.
 *
 * The vault persists only a versioned ciphertext envelope. Callers must not log a value
 * returned by [read]. This phase does not expose any UI or network integration.
 */
interface SecretCipher {
    fun encrypt(plaintext: ByteArray): EncryptedSecretPayload
    fun decrypt(payload: EncryptedSecretPayload): ByteArray
    fun deleteKey()
}

data class EncryptedSecretPayload(
    val version: Int,
    val iv: ByteArray,
    val ciphertext: ByteArray
) {
    init {
        require(version == CURRENT_VERSION) { "Unsupported secret payload version" }
        require(iv.isNotEmpty()) { "Secret payload IV is required" }
        require(ciphertext.isNotEmpty()) { "Secret payload ciphertext is required" }
    }

    fun encode(): String = listOf(
        "v=$version",
        "iv=${encoder.encodeToString(iv)}",
        "ct=${encoder.encodeToString(ciphertext)}"
    ).joinToString(";")

    companion object {
        const val CURRENT_VERSION = 1
        private val encoder = Base64.getUrlEncoder().withoutPadding()
        private val decoder = Base64.getUrlDecoder()

        fun decode(encoded: String): EncryptedSecretPayload? = runCatching {
            val parts = encoded.split(';').associate {
                val keyValue = it.split('=', limit = 2)
                require(keyValue.size == 2) { "Malformed secret payload" }
                keyValue[0] to keyValue[1]
            }
            require(parts.size == 3) { "Malformed secret payload" }
            val version = parts["v"]?.toIntOrNull() ?: error("Missing payload version")
            val iv = parts["iv"]?.let(decoder::decode) ?: error("Missing payload IV")
            val ciphertext = parts["ct"]?.let(decoder::decode) ?: error("Missing payload ciphertext")
            EncryptedSecretPayload(version, iv, ciphertext)
        }.getOrNull()
    }
}

data class SecretRecordStatus(
    val reference: String,
    val exists: Boolean
)

class EncryptedSecretVault(
    private val dataStore: DataStore<Preferences>,
    private val cipherForReference: (String) -> SecretCipher
) {
    fun observeStatus(reference: String): Flow<SecretRecordStatus> {
        val normalized = normalizeReference(reference)
        return dataStore.data.map { preferences ->
            SecretRecordStatus(
                reference = normalized,
                exists = preferences[preferenceKey(normalized)] != null
            )
        }
    }

    suspend fun store(reference: String, rawSecret: String) {
        val normalized = normalizeReference(reference)
        require(rawSecret.isNotBlank()) { "Secret must not be blank" }
        val payload = cipherForReference(keyAlias(normalized)).encrypt(rawSecret.encodeToByteArray())

        dataStore.edit { preferences ->
            preferences[preferenceKey(normalized)] = payload.encode()
        }
    }

    /** Returns null for an absent or corrupt entry; it never silently rewrites persisted data. */
    suspend fun read(reference: String): String? {
        val normalized = normalizeReference(reference)
        val encoded = dataStore.data.map { it[preferenceKey(normalized)] }.firstValue()
            ?: return null
        val payload = EncryptedSecretPayload.decode(encoded) ?: return null
        return runCatching {
            cipherForReference(keyAlias(normalized)).decrypt(payload).decodeToString()
        }.getOrNull()
    }

    suspend fun clear(reference: String) {
        val normalized = normalizeReference(reference)
        dataStore.edit { preferences -> preferences.remove(preferenceKey(normalized)) }
        cipherForReference(keyAlias(normalized)).deleteKey()
    }

    companion object {
        private val allowedReference = Regex("[a-z0-9][a-z0-9._-]{0,63}")
        private const val KEY_PREFIX = "ivai.secret.v1."
        private const val KEYSTORE_ALIAS_PREFIX = "ivai.secret.v1."

        fun keyAlias(reference: String): String = "$KEYSTORE_ALIAS_PREFIX${normalizeReference(reference)}"

        private fun preferenceKey(reference: String) =
            stringPreferencesKey("$KEY_PREFIX${normalizeReference(reference)}")

        private fun normalizeReference(reference: String): String {
            require(allowedReference.matches(reference)) { "Invalid secret reference" }
            return reference
        }
    }
}

private suspend fun <T> Flow<T>.firstValue(): T = first()

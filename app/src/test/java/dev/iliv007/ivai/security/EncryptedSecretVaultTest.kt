package dev.iliv007.ivai.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EncryptedSecretVaultTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var cipherFactory: RecordingCipherFactory
    private lateinit var vault: EncryptedSecretVault
    private lateinit var preferencesFile: File

    @Before
    fun setUp() {
        preferencesFile = File(
            ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir,
            "vault-${System.nanoTime()}.preferences_pb"
        )
        dataStore = PreferenceDataStoreFactory.create { preferencesFile }
        cipherFactory = RecordingCipherFactory()
        vault = EncryptedSecretVault(dataStore, cipherFactory::forAlias)
    }

    @After
    fun tearDown() {
        preferencesFile.delete()
    }

    @Test
    fun `vault stores only encrypted envelope and returns plaintext only on read`() = runBlocking {
        val reference = "openrouter"
        val secret = "sk-local-secret-never-persisted"

        vault.store(reference, secret)

        val persisted = dataStore.data.first()[stringPreferencesKey("ivai.secret.v1.$reference")]
        assertTrue(persisted?.startsWith("v=1;iv=") == true)
        assertFalse(persisted.orEmpty().contains(secret))
        assertEquals(secret, vault.read(reference))
        assertTrue(vault.observeStatus(reference).first().exists)
    }

    @Test
    fun `vault rejects corrupt envelope and clear removes record and key`() = runBlocking {
        val reference = "gemini"
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("ivai.secret.v1.$reference")] = "v=1;iv=not*base64;ct=still*bad"
        }

        assertNull(vault.read(reference))

        vault.store(reference, "temporary")
        vault.clear(reference)

        assertNull(dataStore.data.first()[stringPreferencesKey("ivai.secret.v1.$reference")])
        assertTrue(cipherFactory.forAlias(EncryptedSecretVault.keyAlias(reference)).wasDeleted)
        assertFalse(vault.observeStatus(reference).first().exists)
    }

    @Test
    fun `keystore decrypt failure is not reported as a usable credential`() = runBlocking {
        val reference = "openrouter"
        vault.store(reference, "test-only-secret")
        cipherFactory.forAlias(EncryptedSecretVault.keyAlias(reference)).decryptFailure =
            IllegalStateException("key invalidated")

        assertNull(vault.read(reference))
        assertFalse(vault.observeStatus(reference).first().exists)
        assertTrue(dataStore.data.first()[stringPreferencesKey("ivai.secret.v1.$reference")] != null)
    }

    @Test
    fun `vault clear all removes every ciphertext and matching key without decrypting`() = runBlocking {
        vault.store("gemini", "first-secret")
        vault.store("openrouter", "second-secret")
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("ivai.secret.v1.invalid reference")] = "malformed"
        }

        vault.clearAll()

        assertTrue(dataStore.data.first().asMap().isEmpty())
        assertTrue(cipherFactory.forAlias(EncryptedSecretVault.keyAlias("gemini")).wasDeleted)
        assertTrue(cipherFactory.forAlias(EncryptedSecretVault.keyAlias("openrouter")).wasDeleted)
    }

    @Test
    fun `vault rejects invalid references before persistence`() = runBlocking {
        val exception = runCatching { vault.store("Open Router", "secret") }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
        assertTrue(dataStore.data.first().asMap().isEmpty())
    }

    private class RecordingCipherFactory {
        private val ciphers = mutableMapOf<String, RecordingCipher>()

        fun forAlias(alias: String): RecordingCipher = ciphers.getOrPut(alias) { RecordingCipher() }
    }

    private class RecordingCipher : SecretCipher {
        var wasDeleted: Boolean = false
            private set
        var decryptFailure: Throwable? = null

        override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload {
            val iv = "fixed-test-iv".encodeToByteArray()
            val ciphertext = plaintext.reversedArray()
            return EncryptedSecretPayload(EncryptedSecretPayload.CURRENT_VERSION, iv, ciphertext)
        }

        override fun decrypt(payload: EncryptedSecretPayload): ByteArray {
            decryptFailure?.let { throw it }
            return payload.ciphertext.reversedArray()
        }

        override fun deleteKey() {
            wasDeleted = true
        }
    }
}

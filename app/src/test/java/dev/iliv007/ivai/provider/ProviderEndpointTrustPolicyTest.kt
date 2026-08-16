package dev.iliv007.ivai.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProviderEndpointTrustPolicyTest {

    @Test
    fun `remote mode accepts public HTTPS and rejects local or cleartext destinations`() {
        ProviderEndpointPolicy.requireAllowedEndpoint(
            "https://api.example.com/v1",
            ProviderEndpointTrustMode.REMOTE_HTTPS
        )

        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("http://api.example.com/v1", ProviderEndpointTrustMode.REMOTE_HTTPS) }
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://localhost:11434/v1", ProviderEndpointTrustMode.REMOTE_HTTPS) }
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://127.0.0.1:1234/v1", ProviderEndpointTrustMode.REMOTE_HTTPS) }
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://192.168.1.9/v1", ProviderEndpointTrustMode.REMOTE_HTTPS) }
    }

    @Test
    fun `loopback trust accepts only exact HTTPS loopback hosts`() {
        listOf(
            "https://localhost:1234/v1",
            "https://127.0.0.1:1234/v1",
            "https://[::1]:1234/v1"
        ).forEach { endpoint ->
            ProviderEndpointPolicy.requireAllowedEndpoint(endpoint, ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS)
        }

        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("http://localhost:1234/v1", ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS) }
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://localhost.evil.example/v1", ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS) }
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://192.168.1.9/v1", ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS) }
    }

    @Test
    fun `private LAN trust accepts RFC1918 IPv4 over HTTPS only`() {
        listOf(
            "https://10.0.0.7:8000/v1",
            "https://172.16.4.8:8000/v1",
            "https://192.168.1.9:8000/v1"
        ).forEach { endpoint ->
            ProviderEndpointPolicy.requireAllowedEndpoint(endpoint, ProviderEndpointTrustMode.LOCAL_LAN_HTTPS)
        }

        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("http://192.168.1.9:8000/v1", ProviderEndpointTrustMode.LOCAL_LAN_HTTPS) }
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://172.15.4.8:8000/v1", ProviderEndpointTrustMode.LOCAL_LAN_HTTPS) }
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://8.8.8.8/v1", ProviderEndpointTrustMode.LOCAL_LAN_HTTPS) }
    }

    @Test
    fun `endpoint policy rejects parser bypass fields`() {
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://user:pass@api.example.com/v1", ProviderEndpointTrustMode.REMOTE_HTTPS) }
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://api.example.com/v1?key=not-a-secret", ProviderEndpointTrustMode.REMOTE_HTTPS) }
        assertRejected { ProviderEndpointPolicy.requireAllowedEndpoint("https://api.example.com/v1#fragment", ProviderEndpointTrustMode.REMOTE_HTTPS) }
    }

    @Test
    fun `local connections require persisted confirmation while remote connections cannot carry it`() {
        val local = ProviderConnectionDescriptor(
            id = "local-model",
            kind = ProviderKind.CUSTOM_OPENAI_COMPATIBLE,
            displayName = "My local model",
            baseUrl = "https://localhost:1234/v1",
            endpointTrustMode = ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS,
            localTrustConfirmedAtEpochMs = 123L,
            enabled = true
        )
        assertEquals(ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS, local.endpointTrustMode)
        assertRejected {
            ProviderConnectionDescriptor(
                id = "unconfirmed-local",
                kind = ProviderKind.CUSTOM_OPENAI_COMPATIBLE,
                displayName = "Unconfirmed local",
                baseUrl = "https://localhost:1234/v1",
                endpointTrustMode = ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS,
                localTrustConfirmedAtEpochMs = null,
                enabled = true
            )
        }
        assertRejected {
            ProviderConnectionDescriptor(
                id = "remote-with-local-confirmation",
                kind = ProviderKind.CUSTOM_OPENAI_COMPATIBLE,
                displayName = "Remote",
                baseUrl = "https://api.example.com/v1",
                endpointTrustMode = ProviderEndpointTrustMode.REMOTE_HTTPS,
                localTrustConfirmedAtEpochMs = 123L,
                enabled = true
            )
        }
    }

    @Test
    fun `no auth is valid only without a credential reference`() {
        val account = ProviderAccountDescriptor(
            id = "local-account",
            connectionId = "local-model",
            displayName = "No key",
            credentialReference = null,
            authMode = ProviderAccountAuthMode.NONE,
            enabled = true
        )
        assertEquals(ProviderAccountAuthMode.NONE, account.authMode)
        assertTrue(noAuthCredentialMarker(account.id).startsWith("no-auth."))
        assertRejected {
            ProviderAccountDescriptor(
                id = "invalid-no-auth",
                connectionId = "local-model",
                displayName = "Invalid",
                credentialReference = CredentialReference("not-allowed"),
                authMode = ProviderAccountAuthMode.NONE,
                enabled = true
            )
        }
    }

    private fun assertRejected(action: () -> Unit) {
        val failure = runCatching(action).exceptionOrNull()
        assertTrue("Expected policy rejection", failure is IllegalArgumentException)
    }
}

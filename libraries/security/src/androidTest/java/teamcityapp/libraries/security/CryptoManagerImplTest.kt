package teamcityapp.libraries.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoManagerImplTest {

    private val crypto = CryptoManagerImpl()

    @Test
    fun encryptAndDecryptWithPersistedKey() {
        val encrypted = crypto.encrypt("EncryptionFailed")

        assertFalse(crypto.isFailed(encrypted))
        val decrypted = CryptoManagerImpl().decrypt(encrypted)
        assertFalse(crypto.isFailed(decrypted))
        assertArrayEquals("EncryptionFailed".toByteArray(), decrypted)
    }

    @Test
    fun rejectsLegacyAndTamperedCiphertext() {
        val legacy = byteArrayOf(1, 2, 3)
        assertTrue(crypto.isFailed(crypto.decrypt(legacy)))

        val encrypted = crypto.encrypt("password")
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 1).toByte()
        assertTrue(crypto.isFailed(crypto.decrypt(encrypted)))
    }
}

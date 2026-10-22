/*
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcityapp.libraries.accounts

import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.CoreFoundation.*
import platform.Security.*

/** One atomic Keychain item, available only on this unlocked device, stores the account vault. */
@OptIn(ExperimentalForeignApi::class)
class IosKeychainVault(private val dispatcher: CoroutineDispatcher, private val service: String = "app.teamcity.accounts.v1") : SecureVault {
    override suspend fun read(): String? = withContext(dispatcher) {
        query { query ->
            CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
            memScoped {
                val result = alloc<CFTypeRefVar>()
                result.value = null
                val status = SecItemCopyMatching(query, result.ptr)
                if (status == errSecItemNotFound) return@memScoped null
                check(status == errSecSuccess) { "Keychain read failed ($status)" }
                val data = result.value as CFDataRef
                try {
                    val size = CFDataGetLength(data).toInt()
                    if (size == 0) "" else checkNotNull(CFDataGetBytePtr(data)).readBytes(size).decodeToString()
                } finally {
                    CFRelease(data)
                }
            }
        }
    }
    override suspend fun write(value: String) = withContext(dispatcher) {
        val bytes = value.encodeToByteArray()
        bytes.usePinned { pinned ->
            val data = checkNotNull(CFDataCreate(kCFAllocatorDefault, if (bytes.isEmpty()) null else pinned.addressOf(0).reinterpret(), bytes.size.toLong()))
            try {
                query { query ->
                    dictionary { attributes ->
                        CFDictionarySetValue(attributes, kSecValueData, data)
                        var status = SecItemUpdate(query, attributes)
                        if (status == errSecItemNotFound) {
                            CFDictionarySetValue(query, kSecValueData, data)
                            CFDictionarySetValue(query, kSecAttrAccessible, kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
                            CFDictionarySetValue(query, kSecAttrSynchronizable, kCFBooleanFalse)
                            status = SecItemAdd(query, null)
                        }
                        check(status == errSecSuccess) { "Keychain write failed ($status)" }
                    }
                }
            } finally {
                CFRelease(data)
            }
        }
    }
    private fun <T> query(block: (CFMutableDictionaryRef) -> T): T {
        val serviceValue = checkNotNull(CFStringCreateWithCString(kCFAllocatorDefault, service, kCFStringEncodingUTF8))
        val accountValue = checkNotNull(CFStringCreateWithCString(kCFAllocatorDefault, "accounts", kCFStringEncodingUTF8))
        try {
            return dictionary { query ->
                CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
                CFDictionarySetValue(query, kSecAttrService, serviceValue)
                CFDictionarySetValue(query, kSecAttrAccount, accountValue)
                block(query)
            }
        } finally {
            CFRelease(serviceValue)
            CFRelease(accountValue)
        }
    }
    private fun <T> dictionary(block: (CFMutableDictionaryRef) -> T): T = memScoped {
        val dictionary = checkNotNull(CFDictionaryCreateMutable(kCFAllocatorDefault, 0, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr))
        try {
            block(dictionary)
        } finally {
            CFRelease(dictionary)
        }
    }
}

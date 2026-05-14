package com.badrqaba.kbooster.crypto

/**
 * A secure cryptographic service responsible for:
 *
 * • Encrypting and decrypting raw string data.
 * • Persisting encrypted values securely.
 * • Managing the lifecycle of the underlying encryption key.
 *
 * Implementations are expected to:
 * - Use Android Keystore for key storage.
 * - Never expose raw encryption keys.
 * - Store only encrypted values in persistent storage.
 *
 * This interface is typically used for storing sensitive data such as:
 * - Authentication tokens (JWT / refresh tokens)
 * - API keys
 * - User secrets
 *
 * ⚠️ Implementations must ensure:
 * - AES/GCM or equivalent authenticated encryption.
 * - Unique IV per encryption.
 * - Proper key invalidation on logout via [clear].
 */
interface Crypto {

    /**
     * Encrypts a raw string using a secure symmetric encryption algorithm.
     *
     * The returned value is expected to include any required metadata
     * (e.g., IV) necessary for later decryption.
     *
     * @param data The plain-text string to encrypt.
     * @return A Base64-encoded encrypted representation of the input.
     *
     * @throws Exception if encryption fails.
     */
    fun encrypt(data: String): String

    /**
     * Decrypts a previously encrypted string.
     *
     * The input must be a value produced by [encrypt].
     *
     * @param data The encrypted string (including IV if applicable).
     * @return The original decrypted plain-text string.
     *
     * @throws Exception if decryption fails or the key is invalid.
     */
    fun decrypt(data: String): String

    /**
     * Clears cryptographic material associated with the provided key.
     *
     * This should:
     * - Remove the encrypted value from storage.
     * - Optionally delete the underlying keystore key.
     *
     * Typically called during logout to ensure:
     * - Tokens become irrecoverable.
     * - Previously encrypted values cannot be decrypted again.
     *
     * @param key The storage key associated with the encrypted value.
     */
    fun clear(key: String)

    /**
     * Encrypts the provided value and stores it securely.
     *
     * This method must:
     * - Encrypt the value using [encrypt].
     * - Persist only the encrypted result.
     *
     * The raw value must never be stored directly.
     *
     * @param key The storage key.
     * @param value The plain-text value to encrypt and save.
     */
    fun saveAndEncrypt(key: String, value: String)

    /**
     * Loads an encrypted value from storage and decrypts it.
     *
     * This method must:
     * - Retrieve the encrypted value from storage.
     * - Decrypt it using [decrypt].
     *
     * @param key The storage key.
     * @return The decrypted plain-text value, or null if not found.
     */
    fun loadAndDecrypt(key: String): String?
}
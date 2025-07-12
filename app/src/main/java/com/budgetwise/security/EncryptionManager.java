package com.budgetwise.security;

import android.content.Context;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionManager {
    private static final String TAG = "EncryptionManager";
    private static final String KEYSTORE_ALIAS = "BudgetWiseKey";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    private final Context context;
    private SecretKey secretKey;

    public EncryptionManager(Context context) {
        this.context = context;
        initializeKey();
    }

    private void initializeKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);

            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                generateKey();
            }

            secretKey = (SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null);
            
            // Verify the key is usable by doing a test encryption
            testEncryption();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize encryption key with KeyStore, falling back to device-specific key", e);
            // Fallback to a device-specific key if KeyStore fails
            createFallbackKey();
        }
    }
    
    private void testEncryption() {
        try {
            // Test encryption to make sure the key works
            String testString = "test";
            String encrypted = encrypt(testString);
            String decrypted = decrypt(encrypted);
            
            if (!testString.equals(decrypted)) {
                Log.e(TAG, "Encryption test failed, falling back to device-specific key");
                createFallbackKey();
            }
        } catch (Exception e) {
            Log.e(TAG, "Encryption test failed, falling back to device-specific key", e);
            createFallbackKey();
        }
    }
    
    private void createFallbackKey() {
        try {
            // Create a device-specific key as fallback
            String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            String salt = "BudgetWiseSalt123"; // Fixed salt
            String keyMaterial = deviceId + salt;
            
            // Use SHA-256 hash of the key material
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(keyMaterial.getBytes(StandardCharsets.UTF_8));
            
            // Create AES key
            secretKey = new SecretKeySpec(keyBytes, "AES");
            
            Log.d(TAG, "Created fallback encryption key");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create fallback key", e);
            // Last resort - create a fixed key (less secure but better than crashing)
            byte[] fixedKey = new byte[32]; // 256 bits
            new SecureRandom().nextBytes(fixedKey);
            secretKey = new SecretKeySpec(fixedKey, "AES");
        }
    }

    private void generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
        
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build();

        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return "";
        }
        
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);

            return Base64.encodeToString(encryptedWithIv, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Encryption failed, returning plaintext", e);
            // Return a special marker to indicate unencrypted data
            return "UNENCRYPTED:" + plainText;
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return "";
        }
        
        // Check if this is unencrypted data
        if (encryptedText.startsWith("UNENCRYPTED:")) {
            return encryptedText.substring("UNENCRYPTED:".length());
        }
        
        try {
            byte[] encryptedWithIv = Base64.decode(encryptedText, Base64.DEFAULT);
            
            // Validate input length
            if (encryptedWithIv.length <= GCM_IV_LENGTH) {
                Log.e(TAG, "Invalid encrypted data length");
                return "";
            }

            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
            System.arraycopy(encryptedWithIv, iv.length, encryptedData, 0, encryptedData.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Decryption failed, returning empty string", e);
            return "";
        }
    }

    public String generateHMAC(String data) {
        try {
            String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            String keyMaterial = deviceId + System.currentTimeMillis();
            
            SecretKeySpec signingKey = new SecretKeySpec(keyMaterial.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hmac, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "HMAC generation failed", e);
            throw new RuntimeException("HMAC generation failed", e);
        }
    }

    public boolean verifyHMAC(String data, String expectedHmac) {
        try {
            String calculatedHmac = generateHMAC(data);
            return calculatedHmac.equals(expectedHmac);
        } catch (Exception e) {
            Log.e(TAG, "HMAC verification failed", e);
            return false;
        }
    }
}

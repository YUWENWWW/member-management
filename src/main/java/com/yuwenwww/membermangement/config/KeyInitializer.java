package com.yuwenwww.membermangement.config;

import com.yuwenwww.membermangement.entity.KeyMaterial;
import com.yuwenwww.membermangement.repository.KeyMaterialRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Optional;

@Configuration
public class KeyInitializer {

    // 靜態區塊，確保 Bouncy Castle Provider 在任何密碼學操作前被註冊
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            System.out.println("Bouncy Castle Provider registered for KeyInitializer.");
        }
    }

    /**
     * 創建一個 CommandLineRunner Bean，在應用程式啟動後執行。
     * 檢查 PII 加密金鑰是否存在，如果不存在則生成並儲存。
     * @param keyMaterialRepository KeyMaterialRepository 的實例，用於資料庫操作
     * @return CommandLineRunner 實例
     */
    @Bean
    public CommandLineRunner initPiiKey(KeyMaterialRepository keyMaterialRepository) {
        return args -> {
            final String PII_KEY_LABEL = "pii_aes_key";
            final int KEY_SIZE = 256; // AES-256 位元金鑰

            Optional<KeyMaterial> existingKey = keyMaterialRepository.findByKeyLabel(PII_KEY_LABEL);

            if (existingKey.isEmpty()) {
                System.out.println("Initializing PII encryption key: " + PII_KEY_LABEL);
                try {
                    // 1. 生成 AES SecretKey
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
                    keyGen.init(KEY_SIZE, new SecureRandom()); // 使用安全亂數生成
                    SecretKey secretKey = keyGen.generateKey();
                    byte[] keyValue = secretKey.getEncoded(); // 獲取金鑰的原始位元組

                    // 2. 生成一個 IV (用於 KeyMaterial 自身的元數據 IV，雖然本場景不直接用於金鑰加密)
                    byte[] ivBytes = new byte[16]; // AES 區塊大小為 16 位元組 (128 位元)
                    new SecureRandom().nextBytes(ivBytes); // 使用安全亂數生成 IV

                    // 3. 創建 KeyMaterial 實體
                    KeyMaterial piiKeyMaterial = new KeyMaterial();
                    piiKeyMaterial.setKeyLabel(PII_KEY_LABEL);
                    piiKeyMaterial.setKeyValue(keyValue);
                    piiKeyMaterial.setIv(ivBytes);
                    piiKeyMaterial.setKeySize(KEY_SIZE);

                    // 4. 儲存到資料庫
                    keyMaterialRepository.save(piiKeyMaterial);
                    System.out.println("PII encryption key '" + PII_KEY_LABEL + "' generated and stored successfully.");

                } catch (Exception e) {
                    System.err.println("Error generating or storing PII encryption key: " + e.getMessage());
                    e.printStackTrace();
                    // 在生產環境中，這裡應該有更健壯的錯誤處理，例如應用程式啟動失敗或發送告警
                }
            } else {
                System.out.println("PII encryption key '" + PII_KEY_LABEL + "' already exists. Skipping initialization.");
            }
        };
    }
}

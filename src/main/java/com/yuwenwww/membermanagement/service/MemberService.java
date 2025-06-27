package com.yuwenwww.membermanagement.service;

import com.yuwenwww.membermanagement.entity.Member;
import com.yuwenwww.membermanagement.entity.KeyMaterial; // 引入 KeyMaterial
import com.yuwenwww.membermanagement.repository.MemberRepository;
import com.yuwenwww.membermanagement.repository.KeyMaterialRepository; // 引入 KeyMaterialRepository

import org.bouncycastle.jce.provider.BouncyCastleProvider; // 引入 Bouncy Castle
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 引入 PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 引入 Transactional

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Spring Security 的密碼編碼器

    @Autowired
    private KeyMaterialRepository keyMaterialRepository; // 用於管理加密 PII 的金鑰

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    // 靜態區塊，用於在類別載入時註冊 Bouncy Castle Provider
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            System.out.println("Bouncy Castle Provider registered for MemberService.");
        }
    }

    /**
     * 註冊新會員。
     * 密碼會被雜湊，敏感資訊（email, phone）會被加密。
     * @param member 要註冊的會員資訊
     * @param piiKeyLabel 用於加密個人身份資訊的金鑰標籤
     * @return 註冊成功的會員實體
     * @throws RuntimeException 如果用戶名已存在或加密失敗
     */
    @Transactional
    public Member registerNewMember(Member member, String piiKeyLabel) {
        if (memberRepository.findByUsername(member.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + member.getUsername());
        }

        // 1. 雜湊密碼
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        // 2. 加密敏感個人資訊 (PII)
        try {
            KeyMaterial piiKeyMaterial = keyMaterialRepository.findByKeyLabel(piiKeyLabel)
                    .orElseThrow(() -> new RuntimeException("PII encryption key not found: " + piiKeyLabel));

            SecretKey piiSecretKey = new SecretKeySpec(piiKeyMaterial.getKeyValue(), AES_ALGORITHM);

            // 加密 Email
            if (member.getEmail() != null) {
                IvParameterSpec emailIv = generateIv();
                byte[] encryptedEmail = encrypt(new String(member.getEmail(), "UTF-8"), piiSecretKey, emailIv);
                member.setEmail(encryptedEmail);
                member.setEmailIv(emailIv.getIV());
            }

            // 加密 Phone Number
            if (member.getPhoneNumber() != null) {
                IvParameterSpec phoneIv = generateIv();
                byte[] encryptedPhone = encrypt(new String(member.getPhoneNumber(), "UTF-8"), piiSecretKey, phoneIv);
                member.setPhoneNumber(encryptedPhone);
                member.setPhoneIv(phoneIv.getIV());
            }
            member.setEncryptionKeyLabel(piiKeyLabel); // 設定使用的金鑰標籤

        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt PII: " + e.getMessage(), e);
        }

        return memberRepository.save(member);
    }

    /**
     * 驗證會員登入。
     * @param username 用戶名
     * @param rawPassword 原始密碼
     * @return 如果驗證成功返回 Member，否則返回 Optional.empty()
     */
    public Optional<Member> validateMember(String username, String rawPassword) {
        return memberRepository.findByUsername(username)
                .filter(member -> passwordEncoder.matches(rawPassword, member.getPassword()));
    }

    /**
     * 根據 ID 查找會員並解密其敏感資訊。
     * @param id 會員 ID
     * @return 解密後的會員實體
     * @throws RuntimeException 如果會員未找到或解密失敗
     */
    public Member getMemberByIdAndDecryptPii(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + id));

        // 解密敏感個人資訊
        try {
            if (member.getEncryptionKeyLabel() != null) {
                KeyMaterial piiKeyMaterial = keyMaterialRepository.findByKeyLabel(member.getEncryptionKeyLabel())
                        .orElseThrow(() -> new RuntimeException("PII decryption key not found: " + member.getEncryptionKeyLabel()));

                SecretKey piiSecretKey = new SecretKeySpec(piiKeyMaterial.getKeyValue(), AES_ALGORITHM);

                // 解密 Email
                if (member.getEmail() != null && member.getEmailIv() != null) {
                    IvParameterSpec emailIv = new IvParameterSpec(member.getEmailIv());
                    byte[] decryptedEmailBytes = decrypt(member.getEmail(), piiSecretKey, emailIv);
                    member.setEmail(decryptedEmailBytes); // 直接將 byte[] 設置回去，方便後續轉為 String
                }

                // 解密 Phone Number
                if (member.getPhoneNumber() != null && member.getPhoneIv() != null) {
                    IvParameterSpec phoneIv = new IvParameterSpec(member.getPhoneIv());
                    byte[] decryptedPhoneBytes = decrypt(member.getPhoneNumber(), piiSecretKey, phoneIv);
                    member.setPhoneNumber(decryptedPhoneBytes); // 直接將 byte[] 設置回去
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt PII for member ID " + id + ": " + e.getMessage(), e);
        }
        return member;
    }

    // --- 加密相關輔助方法 (可從 EncryptionService 複製過來或調整) ---
    private IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    private byte[] encrypt(String plainText, SecretKey secretKey, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        return cipher.doFinal(plainText.getBytes("UTF-8"));
    }

    private byte[] decrypt(byte[] cipherText, SecretKey secretKey, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION, "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        return cipher.doFinal(cipherText);
    }

    // 將 byte[] 轉為 String (用於顯示或處理解密後的 PII)
    public String bytesToString(byte[] bytes) {
        if (bytes == null) return null;
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    // 如果需要，可以添加更新會員資訊的方法，其中也需要考慮 PII 的加密
}

package com.yuwenwww.membermanagement.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255) // 儲存雜湊後的密碼
    private String password;

    @Column(name = "email") // 加密後的 email
    private byte[] email;

    @Column(name = "phone_number") // 加密後的電話號碼
    private byte[] phoneNumber;

    @Column(name = "email_iv", length = 16) // email 加密使用的 IV
    private byte[] emailIv;

    @Column(name = "phone_iv", length = 16) // phone_number 加密使用的 IV
    private byte[] phoneIv;

    @Column(name = "encryption_key_label", length = 50) // 參考用於 PII 加密的金鑰標籤
    private String encryptionKeyLabel;

    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    // Constructors
    public Member() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getEmail() {
        return email;
    }

    public void setEmail(byte[] email) {
        this.email = email;
    }

    public byte[] getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(byte[] phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public byte[] getEmailIv() {
        return emailIv;
    }

    public void setEmailIv(byte[] emailIv) {
        this.emailIv = emailIv;
    }

    public byte[] getPhoneIv() {
        return phoneIv;
    }

    public void setPhoneIv(byte[] phoneIv) {
        this.phoneIv = phoneIv;
    }

    public String getEncryptionKeyLabel() {
        return encryptionKeyLabel;
    }

    public void setEncryptionKeyLabel(String encryptionKeyLabel) {
        this.encryptionKeyLabel = encryptionKeyLabel;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}

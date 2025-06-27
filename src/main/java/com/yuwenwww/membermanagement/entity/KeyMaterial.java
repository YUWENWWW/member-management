package com.yuwenwww.membermanagement.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "key_material")
public class KeyMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_label", nullable = false, unique = true, length = 50)
    private String keyLabel;

    @Column(name = "key_value", nullable = false)
    private byte[] keyValue;

    @Column(name = "iv", nullable = false, length = 16)
    private byte[] iv;


    @Column(name = "key_size", nullable = false)
    private int keySize;

    @Column(name = "usage_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private long usageCount;

    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    // 建構子
    public KeyMaterial() {}

    // 更新建構子以包含 keySize 和 usageCount (生成金鑰時使用)
    public KeyMaterial(String keyLabel, byte[] keyValue, byte[] iv, int keySize) {
        this.keyLabel = keyLabel;
        this.keyValue = keyValue;
        this.iv = iv;
        this.keySize = keySize;
        this.usageCount = 0; // 新金鑰初始化使用次數為 0
    }

    // 包含所有欄位的建構子 (用於從資料庫映射或完整創建)
    public KeyMaterial(Long id, String keyLabel, byte[] keyValue, byte[] iv, int keySize, long usageCount, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.keyLabel = keyLabel;
        this.keyValue = keyValue;
        this.iv = iv;
        this.keySize = keySize;
        this.usageCount = usageCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // Getter / Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyLabel() {
        return keyLabel;
    }

    public void setKeyLabel(String keyLabel) {
        this.keyLabel = keyLabel;
    }

    public byte[] getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(byte[] keyValue) {
        this.keyValue = keyValue;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(long usageCount) {
        this.usageCount = usageCount;
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

-- src/main/resources/schema.sql
-- 會員表格
CREATE TABLE IF NOT EXISTS members (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- 儲存雜湊後的密碼
    email VARBINARY(255),          -- 儲存加密後的電子郵件
    phone_number VARBINARY(255),   -- 儲存加密後的電話號碼
    email_iv VARBINARY(16),        -- 加密 email 使用的 IV
    phone_iv VARBINARY(16),        -- 加密 phone_number 使用的 IV
    encryption_key_label VARCHAR(50), -- 參考用於加密 PII 的金鑰標籤
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- 金鑰材料表格 (沿用 encryptiondemo 中的設計)
CREATE TABLE IF NOT EXISTS key_material (
                                            id INT AUTO_INCREMENT PRIMARY KEY,
                                            key_label VARCHAR(50) NOT NULL UNIQUE, -- 金鑰標籤，唯一
    key_value VARBINARY(32) NOT NULL,      -- AES-256 金鑰為 32 位元組
    iv VARBINARY(16) NOT NULL,             -- 金鑰材料的 IV
    key_size INT NOT NULL DEFAULT 256,     -- 金鑰大小
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

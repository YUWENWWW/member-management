package com.yuwenwww.membermangement.controller;

import com.yuwenwww.membermangement.entity.Member;
import com.yuwenwww.membermangement.service.MemberService;
import com.yuwenwww.membermangement.dto.RegisterRequest; // 引入新的 DTO
import com.yuwenwww.membermangement.dto.MemberProfileResponse; // 引入新的 DTO
import jakarta.validation.Valid; // 引入驗證註解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize; // 引入 PreAuthorize

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * 註冊新會員。
     * 路徑：/api/members/register
     * @param request 註冊請求 DTO
     * @return 註冊成功的回應
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerMember(@Valid @RequestBody RegisterRequest request) {
        try {
            Member newMember = new Member();
            newMember.setUsername(request.getUsername());
            newMember.setPassword(request.getPassword()); // 服務層會雜湊
            newMember.setEmail(request.getEmail() != null ? request.getEmail().getBytes() : null); // 將 String 轉為 byte[]
            newMember.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().getBytes() : null); // 將 String 轉為 byte[]

            // 假設 PII 加密金鑰的標籤為 "pii_aes_key"。這需要提前生成。
            // 您可以在啟動時或通過其他 API 生成這個金鑰並儲存。
            String piiKeyLabel = "pii_aes_key";
            Member registeredMember = memberService.registerNewMember(newMember, piiKeyLabel);

            return ResponseEntity.status(HttpStatus.CREATED).body("Member registered successfully with ID: " + registeredMember.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("An error occurred during registration.");
        }
    }

    /**
     * 獲取會員資料 (需要登入)。
     * 路徑：/api/members/{id}
     * @param id 會員 ID
     * @return 會員資料 DTO (包含解密後的敏感資訊)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // 只有登入用戶或管理員才能訪問
    public ResponseEntity<MemberProfileResponse> getMemberProfile(@PathVariable Long id) {
        try {
            Member member = memberService.getMemberByIdAndDecryptPii(id);
            MemberProfileResponse response = new MemberProfileResponse(
                    member.getId(),
                    member.getUsername(),
                    memberService.bytesToString(member.getEmail()), // 解密後的 email
                    memberService.bytesToString(member.getPhoneNumber()), // 解密後的 phone
                    member.getCreatedAt(),
                    member.getUpdatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 或者返回錯誤訊息 DTO
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 您可以根據需要添加其他 API，例如：
    // - PUT /api/members/{id} (更新會員資料)
    // - DELETE /api/members/{id} (刪除會員)
    // - POST /api/members/login (登入，但通常由 Spring Security 處理)
}

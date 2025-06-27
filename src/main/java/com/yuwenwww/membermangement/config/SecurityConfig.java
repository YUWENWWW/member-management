package com.yuwenwww.membermangement.config;


import com.yuwenwww.membermangement.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private MemberRepository memberRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 首先嘗試從資料庫載入用戶
            Optional<com.yuwenwww.membermangement.entity.Member> memberOptional = memberRepository.findByUsername(username);
            if (memberOptional.isPresent()) {
                com.yuwenwww.membermangement.entity.Member member = memberOptional.get();
                return User.builder()
                        .username(member.getUsername())
                        .password(member.getPassword())
                        .roles("USER") // 從資料庫載入的會員預設為 USER 角色
                        .build();
            }

            // 如果資料庫中沒有找到，提供一個硬編碼的測試用戶 (僅供開發/測試環境)
            // 在生產環境中，應移除此硬編碼用戶，所有認證應透過資料庫或外部身份提供者
            if ("testuser".equals(username)) {
                // IMPORTANT: In a real application, never hardcode passwords like this.
                // Use a proper PasswordEncoder to encode the password if it's stored.
                // For demonstration, using BCryptPasswordEncoder.encode("testpass") here.
                return User.withUsername("testuser")
                        .password(passwordEncoder().encode("testpass")) // 使用編碼後的密碼
                        .roles("USER", "ADMIN") // 給予測試用戶 ADMIN 角色，以便測試權限
                        .build();
            }

            throw new UsernameNotFoundException("User not found: " + username);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/members/register").permitAll() // 允許所有用戶訪問註冊接口
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // 允許訪問 Swagger UI
                        .anyRequest().authenticated() // 其他所有請求都需要身份驗證
                )
                .httpBasic(withDefaults()); // 使用 HTTP Basic 認證

        // 允許 H2-Console 或其他內嵌框架的 frame
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}

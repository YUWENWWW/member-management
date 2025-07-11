package com.yuwenwww.membermanagement.repository;

import com.yuwenwww.membermanagement.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username); // 根據用戶名查找會員
}


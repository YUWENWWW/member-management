package com.yuwenwww.membermangement.repository;

import com.yuwenwww.membermangement.entity.KeyMaterial; // 引入正確的 KeyMaterial 實體
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeyMaterialRepository extends JpaRepository<KeyMaterial, Long> {
    Optional<KeyMaterial> findByKeyLabel(String keyLabel);
}

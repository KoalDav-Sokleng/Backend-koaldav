package com.example.project.repository;

import com.example.project.Entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
        Optional<PasswordResetOtp> findTopByEmailAndPurposeAndUsedFalseOrderByIdDesc(String email, String purpose);

        Optional<PasswordResetOtp> findTopByEmailIgnoreCaseAndPurposeAndUsedFalseOrderByIdDesc(String email,
                        String purpose);

        @Transactional
        @Modifying
        @Query("UPDATE PasswordResetOtp otp SET otp.used = true "
                        + "WHERE LOWER(otp.email) = LOWER(:email) AND otp.purpose = :purpose AND otp.used = false")
        void invalidateActiveOtps(@Param("email") String email, @Param("purpose") String purpose);
}

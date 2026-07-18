package br.edu.ufape.enzitech.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ufape.enzitech.model.PasswordResetToken;
import br.edu.ufape.enzitech.model.User;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByUser_Id(UUID userId);

    Optional<PasswordResetToken> findByTokenAndUser_Email(String token, String email);

}
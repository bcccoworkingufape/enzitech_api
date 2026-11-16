package br.edu.ufape.enzitech.repository;

import br.edu.ufape.enzitech.model.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatar, UUID> {
}

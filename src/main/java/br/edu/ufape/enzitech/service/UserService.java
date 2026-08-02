package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.UserRequestDTO;
import br.edu.ufape.enzitech.exception.EmailAlreadyRegisteredException;
import br.edu.ufape.enzitech.exception.RoleNotAllowedException;
import br.edu.ufape.enzitech.exception.UserNotFoundException;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.model.enums.Role;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.TreatmentRepository;
import br.edu.ufape.enzitech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExperimentRepository experimentRepository;
    private final TreatmentRepository treatmentRepository;

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public User create(UserRequestDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public User update(UUID id, UserRequestDTO dto) {
        User user = findById(id);

        userRepository.findByEmail(dto.email())
                .filter(existingUser -> !existingUser.getId().equals(id))
                .ifPresent(existingUser -> {
                    throw new EmailAlreadyRegisteredException();
                });

        user.setName(dto.name());
        user.setEmail(dto.email());
        
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = findById(id);
        eraseUserData(user);
    }

    @Transactional
    public void deleteOwnAccount(User user) {
        eraseUserData(user);
    }

    private void eraseUserData(User user) {
        experimentRepository.findByUserId(user.getId()).forEach(experimentRepository::delete);
        treatmentRepository.findByUserId(user.getId()).forEach(treatmentRepository::delete);

        user.setName("Usuário excluído");
        user.setEmail("deleted-" + user.getId() + "@enzitech.local");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        userRepository.saveAndFlush(user);

        userRepository.delete(user);
    }

    @Transactional
    public User promoteToAdmin(String email, User requester) {
        if (requester.getRole() != Role.ADMIN) {
            throw new RoleNotAllowedException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        user.setRole(Role.ADMIN);

        return userRepository.save(user);
    }
}
package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.UserRequestDTO;
import br.edu.ufape.enzitech.exception.EmailAlreadyRegisteredException;
import br.edu.ufape.enzitech.exception.UserNotFoundException;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        return userRepository.save(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}
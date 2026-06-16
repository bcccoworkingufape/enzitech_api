package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.LoginRequestDTO;
import br.edu.ufape.enzitech.dto.response.AuthResponseDTO;
import br.edu.ufape.enzitech.dto.response.UserResponseDTO;
import br.edu.ufape.enzitech.exception.InvalidCredentialsException;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.UserRepository;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import br.edu.ufape.enzitech.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthResponseDTO login(LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(dto.email()).orElseThrow();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponseDTO(jwtToken, UserResponseDTO.fromEntity(user));
    }
}
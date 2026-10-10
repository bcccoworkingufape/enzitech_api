package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.controller.api.EnzymeApi;
import br.edu.ufape.enzitech.controller.api.UserApi;
import br.edu.ufape.enzitech.dto.request.EnzymeRequestDTO;
import br.edu.ufape.enzitech.model.Enzyme;
import br.edu.ufape.enzitech.service.EnzymeService;
import br.edu.ufape.enzitech.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(AdminOnlyEndpointsTest.Config.class)
class AdminOnlyEndpointsTest {

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean
        UserService userService() {
            UserService service = Mockito.mock(UserService.class);
            when(service.findAll(any())).thenReturn(Page.empty());
            return service;
        }

        @Bean
        EnzymeService enzymeService() {
            EnzymeService service = Mockito.mock(EnzymeService.class);
            when(service.create(any())).thenReturn(new Enzyme());
            when(service.findAll(any())).thenReturn(Page.empty());
            return service;
        }

        @Bean
        UserController userController(UserService userService) {
            return new UserController(userService);
        }

        @Bean
        EnzymeController enzymeController(EnzymeService enzymeService) {
            return new EnzymeController(enzymeService);
        }
    }

    @Autowired private UserApi userController;
    @Autowired private EnzymeApi enzymeController;

    private final EnzymeRequestDTO enzyme = new EnzymeRequestDTO("Urease", 1.0, 0.5, "Urease");

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void commonUserCannotListUsers() {
        authenticateAs("ROLE_USER");
        assertThatThrownBy(() -> userController.getAllUsers(Pageable.unpaged())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void commonUserCannotReadOrDeleteUsers() {
        authenticateAs("ROLE_USER");
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> userController.getUserById(id)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> userController.deleteUser(id)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void commonUserCannotWriteEnzymes() {
        authenticateAs("ROLE_USER");
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> enzymeController.createEnzyme(enzyme)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> enzymeController.updateEnzyme(id, enzyme)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> enzymeController.deleteEnzyme(id)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void commonUserCanStillReadEnzymes() {
        authenticateAs("ROLE_USER");
        assertThatCode(() -> enzymeController.getAllEnzymes(Pageable.unpaged())).doesNotThrowAnyException();
    }

    @Test
    void adminCanUseRestrictedEndpoints() {
        authenticateAs("ROLE_ADMIN");
        assertThatCode(() -> userController.getAllUsers(Pageable.unpaged())).doesNotThrowAnyException();
        assertThatCode(() -> enzymeController.createEnzyme(enzyme)).doesNotThrowAnyException();
    }

    private static void authenticateAs(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, List.of(new SimpleGrantedAuthority(role))));
    }
}

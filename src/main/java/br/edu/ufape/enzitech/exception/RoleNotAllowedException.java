package br.edu.ufape.enzitech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class RoleNotAllowedException extends RuntimeException {
    public RoleNotAllowedException() {
        super("Usuário não possui a permissão necessária.");
    }
}
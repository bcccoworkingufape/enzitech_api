package br.edu.ufape.enzitech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPermissionException extends RuntimeException {
    public InvalidPermissionException() {
        super("Nível de permissão inválido.");
    }
}
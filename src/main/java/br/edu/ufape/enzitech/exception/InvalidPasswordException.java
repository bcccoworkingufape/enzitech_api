package br.edu.ufape.enzitech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("As senhas não são iguais.");
    }
}
package br.edu.ufape.enzitech.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditAction {

    NAO_AUTORIZADO("AUTH.NAO.AUTORIZADO", "NEGADO"),
    ACESSO_NEGADO("AUTH.ACESSO.NEGADO", "NEGADO"),
    LOGIN_FALHOU("AUTH.LOGIN.FALHOU", "NEGADO"),
    ERRO("SISTEMA.ERRO", "ERRO");

    private final String code;
    private final String result;
}

package br.edu.ufape.enzitech.dto.response;

import java.util.List;

public record TotalResultProcessDTO(
        ProcessInfoDTO process,
        List<TotalResultDataDTO> results
) {}
package br.edu.ufape.enzitech.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Registro histórico e imutável de cada assinatura (hash SHA-256) gerada ao fechar um
 * experimento. Nunca é editado ou apagado: cada fechamento gera uma nova linha, preservando
 * a prova de integridade de fechamentos anteriores mesmo que o experimento seja reaberto
 * (novos tratamentos/enzimas) e fechado novamente depois.
 */
@Entity
@Getter
@Setter
@Table(name = "experiment_signatures")
public class ExperimentSignature extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false)
    private Experiment experiment;

    @Column(name = "results_hash", nullable = false, length = 64)
    private String resultsHash;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @PrePersist
    protected void onCreate() {
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }
}

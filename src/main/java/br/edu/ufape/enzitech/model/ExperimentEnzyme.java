package br.edu.ufape.enzitech.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "experiments_enzymes")
public class ExperimentEnzyme extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false)
    private Experiment experiment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enzyme_id", nullable = false)
    private Enzyme enzyme;

    @Column(name = "variable_a")
    private Double variableA;

    @Column(name = "variable_b")
    private Double variableB;

    @Column(name = "custom_formula_curve", columnDefinition = "TEXT")
    private String customFormulaCurve;

    @Column(nullable = false)
    private Double duration;
    
    @Column(name = "weight_sample", nullable = false)
    private Double weightSample;
    
    @Column(name = "weight_ground", nullable = false)
    private Double weightGround;
    
    @Column(nullable = false)
    private Double size;

    
    @PrePersist
    protected void onCreate() {
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        this.setUpdatedAt(LocalDateTime.now());
    }
}
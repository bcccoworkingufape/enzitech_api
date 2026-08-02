package br.edu.ufape.enzitech.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@Table(name = "experiments_enzymes")
@SQLDelete(sql = "UPDATE experiments_enzymes SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at is null")
public class ExperimentEnzyme extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false)
    private Experiment experiment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enzyme_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Enzyme enzyme;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "formula_curve", columnDefinition = "TEXT")
    private String formulaCurve;

    @Column(name = "formula_calculation", columnDefinition = "TEXT")
    private String formulaCalculation;

    @Column(name = "variable_a")
    private Double variableA;

    @Column(name = "variable_b")
    private Double variableB;

    @Column(name = "custom_formula_curve", columnDefinition = "TEXT")
    private String customFormulaCurve;

    @Column(name = "custom_formula_calculation", columnDefinition = "TEXT")
    private String customFormulaCalculation;

    @Column(nullable = false)
    private Double duration;
    
    @Column(name = "weight_sample", nullable = false)
    private Double weightSample;
    
    @Column(name = "weight_ground", nullable = false)
    private Double weightGround;
    
    @Column(nullable = false)
    private Double size;

    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private Boolean active = true;


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
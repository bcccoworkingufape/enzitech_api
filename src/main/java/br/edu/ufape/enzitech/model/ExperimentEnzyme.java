package br.edu.ufape.enzitech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @JoinColumn(name = "enzyme_id", nullable = false)
    private Enzyme enzyme;

    @Column(name = "variable_a")
    private Double variableA;

    @Column(name = "variable_b")
    private Double variableB;

    @Column(nullable = false)
    private Double duration;

    @Column(name = "weight_sample", nullable = false)
    private Double weightSample;

    @Column(name = "weight_ground", nullable = false)
    private Double weightGround;

    @Column(nullable = false)
    private Double size;
}
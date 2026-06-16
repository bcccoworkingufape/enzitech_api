package br.edu.ufape.enzitech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@Table(name = "experiments_results")
@SQLDelete(sql = "UPDATE experiments_results SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at is null")
public class ResultExperiment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false) 
    private Treatment treatment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enzyme_id", nullable = false)
    private Enzyme enzyme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false)
    private Experiment experiment;

    @Column(nullable = false)
    private Double result;

    @Column(nullable = false)
    private Double sample;

    @Column(name = "white_sample", nullable = false)
    private Double whiteSample;

    @Column(name = "difference_between_samples", nullable = false)
    private Double differenceBetweenSamples;

    @Column(nullable = false)
    private Double curve;
}
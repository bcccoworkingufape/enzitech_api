package br.edu.ufape.enzitech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private ExperimentTreatment treatment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enzyme_id", nullable = false)
    private ExperimentEnzyme enzyme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false)
    private Experiment experiment;

    @Column(name = "repetition_number", nullable = false)
    private Integer repetitionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepetitionStatus status = RepetitionStatus.PENDING;

    private Double result;

    private Double sample;

    @Column(name = "white_sample")
    private Double whiteSample;

    @Column(name = "difference_between_samples")
    private Double differenceBetweenSamples;

    private Double curve;


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
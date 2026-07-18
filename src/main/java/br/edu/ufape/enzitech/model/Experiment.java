package br.edu.ufape.enzitech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "experiments")
@SQLDelete(sql = "UPDATE experiments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at is null")
public class Experiment extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultExperiment> results = new ArrayList<>();

    @OneToMany(mappedBy = "experiment")
    private List<ExperimentEnzyme> experimentEnzymes;
    
    @Column(nullable = false)
        private Integer repetitions;

        @Column(columnDefinition = "NUMERIC(5,2) DEFAULT 0.00")
        private Double progress;

    @ManyToMany
    @JoinTable(
        name = "experiments_processes",
        joinColumns = @JoinColumn(name = "experimentId", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "processId", referencedColumnName = "id")
    )
    private List<Treatment> processes;

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
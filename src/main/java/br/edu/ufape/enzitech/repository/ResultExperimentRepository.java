package br.edu.ufape.enzitech.repository;

import br.edu.ufape.enzitech.model.ResultExperiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResultExperimentRepository extends JpaRepository<ResultExperiment, UUID> {
    
    boolean existsByTreatmentId(UUID treatmentId);

    List<ResultExperiment> findByExperimentId(UUID experimentId);
}
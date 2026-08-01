package br.edu.ufape.enzitech.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ufape.enzitech.model.ExperimentTreatment;

@Repository
public interface ExperimentTreatmentRepository extends JpaRepository<ExperimentTreatment, UUID> {

    List<ExperimentTreatment> findByExperimentId(UUID experimentId);

    Optional<ExperimentTreatment> findByIdAndExperimentId(UUID id, UUID experimentId);
}

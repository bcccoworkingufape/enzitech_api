package br.edu.ufape.enzitech.repository;

import br.edu.ufape.enzitech.model.RepetitionStatus;
import br.edu.ufape.enzitech.model.ResultExperiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResultExperimentRepository extends JpaRepository<ResultExperiment, UUID> {

    List<ResultExperiment> findByExperimentId(UUID experimentId);

    List<ResultExperiment> findByExperimentIdAndTreatmentIdAndEnzymeId(UUID experimentId, UUID treatmentId, UUID enzymeId);

    Optional<ResultExperiment> findByTreatmentIdAndEnzymeIdAndRepetitionNumber(
            UUID treatmentId, UUID enzymeId, Integer repetitionNumber);

    List<ResultExperiment> findByTreatmentId(UUID treatmentId);

    List<ResultExperiment> findByEnzymeId(UUID enzymeId);

    boolean existsByTreatmentIdAndStatus(UUID treatmentId, RepetitionStatus status);

    boolean existsByEnzymeIdAndStatus(UUID enzymeId, RepetitionStatus status);

    long countByExperimentId(UUID experimentId);

    long countByExperimentIdAndStatus(UUID experimentId, RepetitionStatus status);

    @Query("SELECT r.enzyme.id FROM ResultExperiment r WHERE r.experiment.id = :experimentId AND r.treatment.id = :treatmentId")
    List<UUID> findCalculatedEnzymeIds(UUID experimentId, UUID treatmentId);
}
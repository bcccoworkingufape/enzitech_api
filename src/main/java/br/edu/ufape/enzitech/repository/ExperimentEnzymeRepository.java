package br.edu.ufape.enzitech.repository;

import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExperimentEnzymeRepository extends JpaRepository<ExperimentEnzyme, UUID> {
    
    Optional<ExperimentEnzyme> findByExperimentIdAndEnzymeId(UUID experimentId, UUID enzymeId);
}
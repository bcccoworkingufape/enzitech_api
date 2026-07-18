package br.edu.ufape.enzitech.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ufape.enzitech.model.ExperimentEnzyme;

@Repository
public interface ExperimentEnzymeRepository extends JpaRepository<ExperimentEnzyme, UUID> {
    
    List<ExperimentEnzyme> findByExperimentId(UUID experimentId);
    
    Optional<ExperimentEnzyme> findByExperimentIdAndEnzymeId(UUID experimentId, UUID enzymeId);
}
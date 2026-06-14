package br.edu.ufape.enzitech.repository;

import br.edu.ufape.enzitech.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, UUID> {
    
    List<Experiment> findByUserId(UUID userId);
}
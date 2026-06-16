package br.edu.ufape.enzitech.repository;

import br.edu.ufape.enzitech.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.UUID;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, UUID> {
    
    Page<Experiment> findByUserId(UUID userId, Pageable pageable);
}
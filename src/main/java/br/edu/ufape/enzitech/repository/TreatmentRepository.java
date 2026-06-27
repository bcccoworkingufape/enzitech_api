package br.edu.ufape.enzitech.repository;

import br.edu.ufape.enzitech.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, UUID> {
    
    List<Treatment> findByExperimentId(UUID experimentId);
    
    List<Treatment> findByUserId(UUID userId);
}
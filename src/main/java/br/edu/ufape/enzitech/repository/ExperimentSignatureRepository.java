package br.edu.ufape.enzitech.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ufape.enzitech.model.ExperimentSignature;

@Repository
public interface ExperimentSignatureRepository extends JpaRepository<ExperimentSignature, UUID> {

    List<ExperimentSignature> findByExperimentIdOrderBySignedAtDesc(UUID experimentId);
}

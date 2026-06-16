package br.edu.ufape.enzitech.repository;

import br.edu.ufape.enzitech.model.Enzyme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EnzymeRepository extends JpaRepository<Enzyme, UUID> {
}
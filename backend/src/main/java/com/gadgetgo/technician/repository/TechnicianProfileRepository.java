package com.gadgetgo.technician.repository;

import com.gadgetgo.technician.domain.TechnicianProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Long> {
    boolean existsByAccount_IdAndActiveTrue(Long accountId);

    @EntityGraph(attributePaths = {"account", "skills", "skills.category"})
    Optional<TechnicianProfile> findByAccount_Id(Long accountId);

    @EntityGraph(attributePaths = {"account", "skills", "serviceModes"})
    java.util.List<TechnicianProfile> findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(com.gadgetgo.technician.domain.TechnicianAvailability availability);
}

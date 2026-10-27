package com.gadgetgo.catalog.repository;

import com.gadgetgo.catalog.domain.DeviceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceCategoryRepository extends JpaRepository<DeviceCategory, Long> {

    List<DeviceCategory> findAllByActiveTrueOrderByNameAsc();
    Optional<DeviceCategory> findByIdAndActiveTrue(Long id);
}

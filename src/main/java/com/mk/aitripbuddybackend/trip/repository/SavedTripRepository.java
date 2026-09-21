package com.mk.aitripbuddybackend.trip.repository;

import com.mk.aitripbuddybackend.trip.entity.SavedTrip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedTripRepository extends JpaRepository<SavedTrip, Long> {
    List<SavedTrip> findByUserIdOrderByCreatedAtDesc(Long userId);
}
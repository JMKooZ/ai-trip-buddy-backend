package com.mk.aitripbuddybackend.trip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.aitripbuddybackend.trip.dto.TripPlanResponse;
import com.mk.aitripbuddybackend.trip.entity.SavedTrip;
import com.mk.aitripbuddybackend.trip.repository.SavedTripRepository;
import com.mk.aitripbuddybackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedTripService {

    private final SavedTripRepository savedTripRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long save(Long userId, TripPlanResponse plan) {
        try {
            SavedTrip saved = savedTripRepository.save(SavedTrip.builder()
                    .user(userRepository.getReferenceById(userId))
                    .destination(plan.destination())
                    .durationLabel(plan.durationLabel())
                    .planJson(objectMapper.writeValueAsString(plan))
                    .build());
            return saved.getId();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("여행 계획 저장에 실패했습니다.", e);
        }
    }

    public List<SavedTripSummary> findMine(Long userId) {
        return savedTripRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(t -> new SavedTripSummary(t.getId(), t.getDestination(), t.getDurationLabel(), t.getCreatedAt()))
                .toList();
    }

    public SavedTripDetail findDetail(Long userId, Long tripId) {
        SavedTrip trip = getOwnedTrip(userId, tripId);
        return new SavedTripDetail(
                trip.getId(),
                readPlan(trip.getPlanJson()),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }

    @Transactional
    public void update(Long userId, Long tripId, TripPlanResponse plan) {
        SavedTrip trip = getOwnedTrip(userId, tripId);
        try {
            trip.update(plan.destination(), plan.durationLabel(), objectMapper.writeValueAsString(plan));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("여행 계획 수정에 실패했습니다.", e);
        }
    }

    @Transactional
    public void delete(Long userId, Long tripId) {
        savedTripRepository.delete(getOwnedTrip(userId, tripId));
    }

    private SavedTrip getOwnedTrip(Long userId, Long tripId) {
        SavedTrip trip = savedTripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("저장된 여행 계획을 찾을 수 없습니다."));
        if (!trip.isOwnedBy(userId)) {
            throw new SecurityException("본인의 여행 계획만 접근할 수 있습니다.");
        }
        return trip;
    }

    private TripPlanResponse readPlan(String planJson) {
        try {
            return objectMapper.readValue(planJson, TripPlanResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("저장된 여행 계획을 불러오지 못했습니다.", e);
        }
    }

    public record SavedTripSummary(Long id, String destination, String durationLabel, LocalDateTime createdAt) {}
    public record SavedTripDetail(Long id, TripPlanResponse plan, LocalDateTime createdAt, LocalDateTime updatedAt) {}
}
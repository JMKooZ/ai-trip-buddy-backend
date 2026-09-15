package com.mk.aitripbuddybackend.trip.controller;

import com.mk.aitripbuddybackend.trip.dto.TripPlanRequest;
import com.mk.aitripbuddybackend.trip.dto.TripPlanResponse;
import com.mk.aitripbuddybackend.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripPlanResponse> createTripPlan(@RequestBody TripPlanRequest request) {
        return ResponseEntity.ok(tripService.createTripPlan(request));
    }
}
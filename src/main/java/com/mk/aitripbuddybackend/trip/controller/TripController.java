package com.mk.aitripbuddybackend.trip.controller;

import com.mk.aitripbuddybackend.trip.dto.PlaceSearchResponse;
import com.mk.aitripbuddybackend.trip.dto.TripPlanRequest;
import com.mk.aitripbuddybackend.trip.dto.TripPlanResponse;
import com.mk.aitripbuddybackend.trip.service.NaverPlaceService;
import com.mk.aitripbuddybackend.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final NaverPlaceService naverPlaceService;

    @PostMapping
    public ResponseEntity<TripPlanResponse> createTripPlan(
            @RequestBody TripPlanRequest request
    ) {
        return ResponseEntity.ok(
                tripService.createTripPlan(request)
        );
    }

    @GetMapping("/search/places")
    public ResponseEntity<PlaceSearchResponse> searchPlaces(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(
                naverPlaceService.searchLocalPlaces(query)
        );
    }
}

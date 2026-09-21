package com.mk.aitripbuddybackend.trip.controller;

import com.mk.aitripbuddybackend.trip.dto.TripPlanResponse;
import com.mk.aitripbuddybackend.trip.service.SavedTripService;
import com.mk.aitripbuddybackend.user.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/trips/saved")
@RequiredArgsConstructor
public class SavedTripController {

    private final SavedTripService savedTripService;

    @PostMapping
    public ResponseEntity<?> save(@AuthenticationPrincipal CustomOAuth2User principal, @RequestBody TripPlanResponse plan) {
        if (principal == null) return unauthorized();
        return ResponseEntity.ok(Map.of("id", savedTripService.save(principal.getUserId(), plan)));
    }

    @GetMapping
    public ResponseEntity<?> myTrips(@AuthenticationPrincipal CustomOAuth2User principal) {
        if (principal == null) return unauthorized();
        return ResponseEntity.ok(savedTripService.findMine(principal.getUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@AuthenticationPrincipal CustomOAuth2User principal, @PathVariable Long id) {
        if (principal == null) return unauthorized();
        try {
            return ResponseEntity.ok(savedTripService.findDetail(principal.getUserId(), id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal CustomOAuth2User principal, @PathVariable Long id, @RequestBody TripPlanResponse plan) {
        if (principal == null) return unauthorized();
        try {
            savedTripService.update(principal.getUserId(), id, plan);
            return ResponseEntity.ok(Map.of("id", id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal CustomOAuth2User principal, @PathVariable Long id) {
        if (principal == null) return unauthorized();
        try {
            savedTripService.delete(principal.getUserId(), id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        }
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
    }
}
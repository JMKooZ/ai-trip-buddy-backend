package com.mk.aitripbuddybackend.trip.dto;

import java.util.List;

public record TripPlanRequest(
        String destination,
        String duration,
        Integer nights,
        List<String> travelStyles,
        String request
) {
}
package com.mk.aitripbuddybackend.trip.dto;

import java.util.List;

public record TripPlanResponse(
        String destination,
        String durationLabel,
        String summary,
        List<TripDayResponse> days
) {

    public record TripDayResponse(
            Integer day,
            String title,
            List<TripPlaceResponse> places
    ) {
    }

    public record TripPlaceResponse(
            String id,
            Integer day,
            Integer order,
            String name,
            String category,
            String description,
            Double lat,
            Double lng,
            Integer stayMinutes
    ) {
    }
}
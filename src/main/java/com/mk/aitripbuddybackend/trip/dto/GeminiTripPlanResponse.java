package com.mk.aitripbuddybackend.trip.dto;

import java.util.List;

public record GeminiTripPlanResponse(
        String destination,
        String durationLabel,
        String summary,
        List<GeminiDayResponse> days
) {

    public record GeminiDayResponse(
            Integer day,
            String title,
            List<GeminiPlaceResponse> places
    ) {
    }

    public record GeminiPlaceResponse(
            String name,
            String address,
            String category,
            String description,
            Integer stayMinutes
    ) {
    }
}
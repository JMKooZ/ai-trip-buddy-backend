package com.mk.aitripbuddybackend.trip.dto;

import java.util.List;

public record PlaceSearchResponse(
        String query,
        int total,
        List<PlaceSearchItem> items
) {

    public record PlaceSearchItem(
            String name,
            String link,
            String category,
            String description,
            String telephone,
            String address,
            String roadAddress,
            Double lat,
            Double lng
    ) {
    }
}

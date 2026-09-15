package com.mk.aitripbuddybackend.trip.dto;

import java.util.List;

public record NaverGeocodingResponse(
        String status,
        Meta meta,
        List<Address> addresses,
        String errorMessage
) {

    public record Meta(
            Integer totalCount,
            Integer page,
            Integer count
    ) {
    }

    public record Address(
            String roadAddress,
            String jibunAddress,
            String englishAddress,
            List<AddressElement> addressElements,
            String x,
            String y,
            Double distance
    ) {
    }

    public record AddressElement(
            List<String> types,
            String longName,
            String shortName,
            String code
    ) {
    }
}
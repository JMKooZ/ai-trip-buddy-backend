package com.mk.aitripbuddybackend.trip.service;

import com.mk.aitripbuddybackend.trip.dto.GeminiTripPlanResponse;
import com.mk.aitripbuddybackend.trip.dto.NaverGeocodingResponse;
import com.mk.aitripbuddybackend.trip.dto.TripPlanRequest;
import com.mk.aitripbuddybackend.trip.dto.TripPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final GeminiTripPlanService geminiTripPlanService;
    private final NaverPlaceService naverPlaceService;

    public TripPlanResponse createTripPlan(TripPlanRequest request) {
        int totalDays = calculateTotalDays(request);

        GeminiTripPlanResponse geminiResponse = geminiTripPlanService.generateTripPlan(request, totalDays);

        return convertToTripPlanResponse(geminiResponse, totalDays);
    }

    private int calculateTotalDays(TripPlanRequest request) {
        if ("day".equalsIgnoreCase(request.duration())) {
            return 1;
        }

        int nights = request.nights() == null ? 1 : request.nights();

        return nights + 1;
    }

    private TripPlanResponse convertToTripPlanResponse(GeminiTripPlanResponse geminiResponse, int totalDays) {
        List<TripPlanResponse.TripDayResponse> days = new ArrayList<>();

        for (int day = 1; day <= totalDays; day++) {
            GeminiTripPlanResponse.GeminiDayResponse geminiDay = findDay(geminiResponse.days(), day);

            if (geminiDay == null) {
                days.add(createEmptyDay(day));
                continue;
            }

            List<TripPlanResponse.TripPlaceResponse> places = convertPlaces(geminiDay, day);

            days.add(new TripPlanResponse.TripDayResponse(day, geminiDay.title(), places));
        }

        return new TripPlanResponse(geminiResponse.destination(), geminiResponse.durationLabel(), geminiResponse.summary(), days);
    }

    private GeminiTripPlanResponse.GeminiDayResponse findDay(List<GeminiTripPlanResponse.GeminiDayResponse> days, int targetDay) {
        if (days == null) {
            return null;
        }

        return days.stream().filter(day -> day.day() != null && day.day() == targetDay).findFirst().orElse(null);
    }

    private List<TripPlanResponse.TripPlaceResponse> convertPlaces(GeminiTripPlanResponse.GeminiDayResponse geminiDay, int day) {
        if (geminiDay.places() == null) {
            return List.of();
        }

        List<TripPlanResponse.TripPlaceResponse> places = new ArrayList<>();

        int order = 1;

        for (GeminiTripPlanResponse.GeminiPlaceResponse place : geminiDay.places()) {

            NaverGeocodingResponse.Address naverAddress = naverPlaceService.geocode(place.address());

            places.add(createTripPlace(place, naverAddress, day, order));

            order++;
        }

        return places;
    }

    private TripPlanResponse.TripPlaceResponse createTripPlace(GeminiTripPlanResponse.GeminiPlaceResponse place, NaverGeocodingResponse.Address naverAddress, int day, int order) {
        if (naverAddress == null) {
            return new TripPlanResponse.TripPlaceResponse("gemini-" + day + "-" + order, day, order, place.name(), place.category(), place.description(), null, null, place.stayMinutes(), null);
        }

        Double lat = parseCoordinate(naverAddress.y());

        Double lng = parseCoordinate(naverAddress.x());

        TripPlanResponse.NaverPlaceResponse naverPlace =
                new TripPlanResponse.NaverPlaceResponse(
                        place.category(),
                        naverAddress.jibunAddress(),
                        naverAddress.roadAddress(),
                        null,
                        null
                );

        return new TripPlanResponse.TripPlaceResponse(
                "naver-" + day + "-" + order,
                day,
                order,
                place.name(),
                place.category(),
                place.description(),
                lat,
                lng,
                place.stayMinutes(),
                naverPlace
        );
    }

    private Double parseCoordinate(String coordinate) {
        if (coordinate == null || coordinate.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(coordinate);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private TripPlanResponse.TripDayResponse createEmptyDay(int day) {
        return new TripPlanResponse.TripDayResponse(day, "DAY " + day + " 여행", List.of());
    }
}
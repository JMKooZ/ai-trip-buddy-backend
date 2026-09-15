package com.mk.aitripbuddybackend.trip.service;

import com.mk.aitripbuddybackend.trip.dto.TripPlanRequest;
import com.mk.aitripbuddybackend.trip.dto.TripPlanResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripService {

    public TripPlanResponse createTripPlan(TripPlanRequest request) {
        return new TripPlanResponse(
                request.destination(),
                createDurationLabel(request),
                "여행 조건을 바탕으로 생성한 테스트 일정입니다.",
                List.of(
                        createDay(1),
                        createDay(2),
                        createDay(3)
                )
        );
    }

    private String createDurationLabel(TripPlanRequest request) {
        if ("day".equals(request.duration())) {
            return "당일치기";
        }

        int nights = request.nights() == null ? 1 : request.nights();
        return nights + "박 " + (nights + 1) + "일";
    }

    private TripPlanResponse.TripDayResponse createDay(int day) {
        return new TripPlanResponse.TripDayResponse(
                day,
                "DAY " + day + " 여행",
                List.of(
                        new TripPlanResponse.TripPlaceResponse(
                                "mock-" + day + "-1",
                                day,
                                1,
                                "추천 장소 " + day + "-1",
                                "관광",
                                "여행 일정에 포함된 테스트 장소입니다.",
                                33.4996,
                                126.5312,
                                60
                        ),
                        new TripPlanResponse.TripPlaceResponse(
                                "mock-" + day + "-2",
                                day,
                                2,
                                "추천 장소 " + day + "-2",
                                "맛집",
                                "여행 일정에 포함된 테스트 장소입니다.",
                                33.5050,
                                126.5160,
                                90
                        )
                )
        );
    }
}
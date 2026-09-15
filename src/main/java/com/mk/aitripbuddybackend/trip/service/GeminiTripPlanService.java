package com.mk.aitripbuddybackend.trip.service;

import com.mk.aitripbuddybackend.trip.dto.GeminiTripPlanResponse;
import com.mk.aitripbuddybackend.trip.dto.TripPlanRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiTripPlanService {

    private final ChatClient.Builder chatClientBuilder;

    public GeminiTripPlanResponse generateTripPlan(TripPlanRequest request, int totalDays) {
        ChatClient chatClient = chatClientBuilder.build();

        String prompt = createPrompt(request, totalDays);

        GeminiTripPlanResponse response = chatClient.prompt().user(prompt).call().entity(GeminiTripPlanResponse.class);

        if (response == null) {
            throw new IllegalStateException("Gemini 여행 일정 생성 결과가 없습니다.");
        }

        return response;
    }

    private String createPrompt(TripPlanRequest request, int totalDays) {
        String travelStyles = request.travelStyles() == null ? "특별한 여행 스타일 없음" : String.join(", ", request.travelStyles());

        String userRequest = request.request() == null ? "특별한 요청 없음" : request.request();

        return """
                당신은 여행 일정 전문 AI입니다.

                사용자가 입력한 여행 조건을 바탕으로 실제 여행자가
                바로 활용할 수 있는 여행 일정을 생성하세요.

                [여행 정보]
                - 여행지: %s
                - 여행 형태: %s
                - 숙박 일수: %s
                - 총 여행 일수: %d일
                - 여행 스타일: %s
                - 추가 요청: %s

                [일정 생성 규칙]
                1. 반드시 DAY 1부터 DAY %d까지 모든 날짜를 생성하세요.
                2. 하루에 2~4개의 장소를 추천하세요.
                3. 같은 장소를 여러 날짜에 중복해서 추천하지 마세요.
                4. 여행지에 실제로 존재하는 장소를 우선적으로 추천하세요.
                5. 여행 스타일을 일정 전체에 반영하세요.
                6. 하루 일정이 지나치게 빡빡하지 않도록 구성하세요.
                7. 각 장소의 예상 체류시간을 분 단위 정수로 작성하세요.
                8. 각 장소의 실제 주소를 함께 작성하세요.
                9. 주소는 장소가 위치한 실제 도로명 주소 또는 지번 주소를 작성하세요.
                10. 장소명과 주소를 임의로 조합해서 만들어내지 마세요.
                11. 주소를 확실하게 알 수 없는 경우에도 가능한 한 실제 장소의 정확한 주소를 작성하세요.
                12. 위도와 경도는 반환하지 마세요.
                13. 아래 JSON 구조를 반드시 지켜주세요.
                14. JSON 이외의 설명은 작성하지 마세요.

                [응답 JSON 구조]
                {
                  "destination": "여행지",
                  "durationLabel": "여행 기간",
                  "summary": "여행 일정 요약",
                  "days": [
                    {
                      "day": 1,
                      "title": "DAY 1 제목",
                      "places": [
                        {
                          "name": "장소명",
                          "address": "장소의 실제 주소",
                          "category": "카테고리",
                          "description": "장소 설명",
                          "stayMinutes": 60
                        }
                      ]
                    }
                  ]
                }

                [카테고리 예시]
                - 관광
                - 맛집
                - 카페
                - 자연
                - 휴식
                - 액티비티

                여행 일정은 실제 사용자가 바로 확인할 수 있을 정도로
                구체적이고 현실적으로 작성하세요.
                """.formatted(
                request.destination(),
                request.duration(),
                request.nights() == null
                        ? "없음"
                        : request.nights() + "박",
                totalDays,
                travelStyles,
                userRequest,
                totalDays
        );
    }
}
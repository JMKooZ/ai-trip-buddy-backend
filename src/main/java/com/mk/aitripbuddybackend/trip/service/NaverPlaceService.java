package com.mk.aitripbuddybackend.trip.service;

import com.mk.aitripbuddybackend.config.NaverApiConfig;
import com.mk.aitripbuddybackend.trip.dto.NaverGeocodingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverPlaceService {

    private static final String GEOCODING_HOST = "maps.apigw.ntruss.com";

    private static final String GEOCODING_PATH = "/map-geocode/v2/geocode";

    private final NaverApiConfig naverApiConfig;
    private final RestClient restClient;

    public NaverGeocodingResponse.Address geocode(String address) {
        if (address == null || address.isBlank()) {
            log.warn("Naver Geocoding 주소가 비어 있습니다.");
            return null;
        }

        try {
            NaverGeocodingResponse response = restClient
                    .get()
                    .uri(uriBuilder ->
                            uriBuilder.scheme("https")
                                    .host(GEOCODING_HOST)
                                    .path(GEOCODING_PATH)
                                    .queryParam("query", address)
                                    .queryParam("count", 1)
                                    .build())
                    .header("x-ncp-apigw-api-key-id", naverApiConfig.clientId())
                    .header("x-ncp-apigw-api-key", naverApiConfig.clientSecret())
                    .header("Accept", "application/json")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> log.error("Naver Geocoding API 오류. status={}, address={}", res.getStatusCode(), address))
                    .body(NaverGeocodingResponse.class);

            if (response == null) {
                log.warn("Naver Geocoding 응답이 없습니다. address={}", address);

                return null;
            }

            log.info("Naver Geocoding 응답. status={}, totalCount={}, address={}", response.status(), response.meta() == null ? null : response.meta().totalCount(), address);

            if (response.addresses() == null || response.addresses().isEmpty()) {

                log.warn("Naver Geocoding 검색 결과가 없습니다. address={}", address);

                return null;
            }

            return response.addresses().get(0);

        } catch (Exception e) {
            log.error("Naver Geocoding 호출 중 예외가 발생했습니다. address={}", address, e);
            return null;
        }
    }
}
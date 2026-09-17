package com.mk.aitripbuddybackend.trip.service;

import com.mk.aitripbuddybackend.config.NaverApiHubConfig;
import com.mk.aitripbuddybackend.config.NaverMapsApiConfig;
import com.mk.aitripbuddybackend.trip.dto.NaverGeocodingResponse;
import com.mk.aitripbuddybackend.trip.dto.NaverLocalSearchResponse;
import com.mk.aitripbuddybackend.trip.dto.PlaceSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverPlaceService {

    private static final String GEOCODING_HOST = "maps.apigw.ntruss.com";
    private static final String GEOCODING_PATH = "/map-geocode/v2/geocode";

    private static final String LOCAL_SEARCH_HOST = "naverapihub.apigw.ntruss.com";
    private static final String LOCAL_SEARCH_PATH = "/search/v1/local";

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&(?:amp|lt|gt|quot|#39);", Pattern.CASE_INSENSITIVE);

    private final NaverMapsApiConfig naverMapsApiconfig;
    private final NaverApiHubConfig naverApiHubConfig;
    private final RestClient restClient;

    public NaverGeocodingResponse.Address geocode(String address) {
        if (address == null || address.isBlank()) {
            log.warn("Naver Geocoding 주소가 비어 있습니다.");
            return null;
        }

        try {
            NaverGeocodingResponse response = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(GEOCODING_HOST)
                            .path(GEOCODING_PATH)
                            .queryParam("query", address)
                            .queryParam("count", 1)
                            .build())
                    .header("x-ncp-apigw-api-key-id", naverMapsApiconfig.clientId())
                    .header("x-ncp-apigw-api-key", naverMapsApiconfig.clientSecret())
                    .header("Accept", "application/json")
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (req, res) -> log.error(
                                    "Naver Geocoding API 오류. status={}, address={}",
                                    res.getStatusCode(),
                                    address
                            )
                    )
                    .body(NaverGeocodingResponse.class);

            if (response == null) {
                log.warn("Naver Geocoding 응답이 없습니다. address={}", address);
                return null;
            }

            log.info(
                    "Naver Geocoding 응답. status={}, totalCount={}, address={}",
                    response.status(),
                    response.meta() == null ? null : response.meta().totalCount(),
                    address
            );

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

    public PlaceSearchResponse searchLocalPlaces(String query) {
        if (query == null || query.isBlank()) {
            return new PlaceSearchResponse("", 0, List.of());
        }

        String normalizedQuery = query.trim();

        List<PlaceSearchResponse.PlaceSearchItem> combined = new java.util.ArrayList<>();

        // ⭐ 1순위: 지오코딩 - 주소/랜드마크 DB 기반이라 "경복궁" 같은 공식 지명을 정확히 잡아줌
        NaverGeocodingResponse.Address geocoded = geocode(normalizedQuery);
        if (geocoded != null) {
            Double lat = parseCoordinate(geocoded.y());
            Double lng = parseCoordinate(geocoded.x());

            if (lat != null && lng != null) {
                combined.add(new PlaceSearchResponse.PlaceSearchItem(
                        normalizedQuery,
                        "",
                        "주소/장소",
                        "",
                        "",
                        safeText(geocoded.jibunAddress()),
                        safeText(geocoded.roadAddress()),
                        lat,
                        lng
                ));
            }
        }

        // 2순위: 지역검색 - 음식점/카페 등 업체명 검색은 이쪽이 강함
        try {
            NaverLocalSearchResponse response = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(LOCAL_SEARCH_HOST)
                            .path(LOCAL_SEARCH_PATH)
                            .queryParam("query", normalizedQuery)
                            .queryParam("display", 5)
                            .queryParam("start", 1)
                            .queryParam("sort", "random")
                            .queryParam("format", "json")
                            .build())
                    .header("X-NCP-APIGW-API-KEY-ID", naverApiHubConfig.clientId())
                    .header("X-NCP-APIGW-API-KEY", naverApiHubConfig.clientSecret())
                    .header("Accept", "application/json")
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (req, res) -> log.error(
                                    "Naver Local Search API 오류. status={}, query={}",
                                    res.getStatusCode(),
                                    normalizedQuery
                            )
                    )
                    .body(NaverLocalSearchResponse.class);

            if (response != null && response.items() != null) {
                response.items().stream()
                        .map(this::toPlaceSearchItem)
                        .filter(Objects::nonNull)
                        .forEach(combined::add);
            }
        } catch (Exception e) {
            log.error("Naver Local Search 호출 중 예외가 발생했습니다. query={}", normalizedQuery, e);
        }

        log.info("장소 검색 완료. query={}, geocodeHit={}, total={}", normalizedQuery, geocoded != null, combined.size());

        return new PlaceSearchResponse(normalizedQuery, combined.size(), combined);
    }

    private PlaceSearchResponse.PlaceSearchItem toPlaceSearchItem(NaverLocalSearchResponse.Item item) {
        if (item == null) {
            return null;
        }

        return new PlaceSearchResponse.PlaceSearchItem(
                cleanText(item.title()),
                safeText(item.link()),
                safeText(item.category()),
                cleanText(item.description()),
                safeText(item.telephone()),
                safeText(item.address()),
                safeText(item.roadAddress()),
                parseLocalSearchCoordinate(item.mapy()),
                parseLocalSearchCoordinate(item.mapx())
        );
    }

    private Double parseCoordinate(String coordinate) {
        if (coordinate == null || coordinate.isBlank()) return null;
        try {
            return Double.parseDouble(coordinate);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseLocalSearchCoordinate(String coordinate) {
        if (coordinate == null || coordinate.isBlank()) return null;
        try {
            return Double.parseDouble(coordinate) / 10_000_000.0;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String cleanText(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = HTML_TAG_PATTERN.matcher(value).replaceAll("");

        return HTML_ENTITY_PATTERN.matcher(cleaned)
                .replaceAll(match -> switch (match.group().toLowerCase()) {
                    case "&amp;" -> "&";
                    case "&lt;" -> "<";
                    case "&gt;" -> ">";
                    case "&quot;" -> "\"";
                    case "&#39;" -> "'";
                    default -> match.group();
                })
                .trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
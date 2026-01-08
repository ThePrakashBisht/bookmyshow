package com.bookmyshow.bookingservice.service.external;

import com.bookmyshow.bookingservice.dto.external.ShowInfo;
import com.bookmyshow.bookingservice.dto.external.ShowSeatInfo;
import com.bookmyshow.bookingservice.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.event-service.url}")
    private String eventServiceUrl;

    /**
     * Gets show details from Event Service.
     */
    public ShowInfo getShowDetails(Long showId) {
        log.info("Fetching show details for showId: {}", showId);

        String url = eventServiceUrl + "/api/shows/" + showId;

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) body.get("data");

                if (data != null) {
                    return mapToShowInfo(data);
                }
            }

            throw new ServiceUnavailableException("Event Service");

        } catch (RestClientException e) {
            log.error("Error calling Event Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Event Service");
        }
    }

    /**
     * Gets seat details for specific seats.
     */
    public List<ShowSeatInfo> getShowSeats(Long showId, List<Long> showSeatIds) {
        log.info("Fetching {} seat details for showId: {}", showSeatIds.size(), showId);

        // For now, we'll fetch all seats and filter
        // In production, you'd have a batch endpoint
        String url = eventServiceUrl + "/api/shows/" + showId + "/seats";

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) body.get("data");

                if (data != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, List<Map<String, Object>>> seatsByRow =
                            (Map<String, List<Map<String, Object>>>) data.get("seatsByRow");

                    return seatsByRow.values().stream()
                            .flatMap(List::stream)
                            .map(this::mapToShowSeatInfo)
                            .filter(seat -> showSeatIds.contains(seat.getId()))
                            .toList();
                }
            }

            throw new ServiceUnavailableException("Event Service");

        } catch (RestClientException e) {
            log.error("Error calling Event Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Event Service");
        }
    }

    /**
     * Confirms booking with Event Service (updates seat status to BOOKED).
     */
    public boolean confirmSeatsBooked(Long showId, List<Long> showSeatIds,
                                      Long userId, String bookingId) {
        log.info("Confirming {} seats booked for show {} with booking {}",
                showSeatIds.size(), showId, bookingId);

        String url = eventServiceUrl + "/api/shows/" + showId + "/seats/confirm";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", userId.toString());
            headers.set("X-Booking-Id", bookingId);

            HttpEntity<List<Long>> request = new HttpEntity<>(showSeatIds, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (RestClientException e) {
            log.error("Error confirming seats with Event Service: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Releases seats in Event Service (on cancellation).
     */
    public boolean releaseSeats(Long showId, List<Long> showSeatIds, Long userId) {
        log.info("Releasing {} seats for show {}", showSeatIds.size(), showId);

        String url = eventServiceUrl + "/api/shows/" + showId + "/seats/release";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", userId.toString());

            HttpEntity<List<Long>> request = new HttpEntity<>(showSeatIds, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (RestClientException e) {
            log.error("Error releasing seats with Event Service: {}", e.getMessage());
            return false;
        }
    }

    // ============ Mapper Methods ============

    private ShowInfo mapToShowInfo(Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        Map<String, Object> event = (Map<String, Object>) data.get("event");

        @SuppressWarnings("unchecked")
        Map<String, Object> venue = (Map<String, Object>) data.get("venue");

        return ShowInfo.builder()
                .id(getLong(data, "id"))
                .showTime(parseDateTime(data.get("showTime")))
                .endTime(parseDateTime(data.get("endTime")))
                .status((String) data.get("status"))
                .basePrice(getDouble(data, "basePrice"))
                .availableSeats(getInt(data, "availableSeats"))
                .totalSeats(getInt(data, "totalSeats"))
                .eventId(event != null ? getLong(event, "id") : null)
                .eventTitle(event != null ? (String) event.get("title") : null)
                .eventDurationMinutes(event != null ? getInt(event, "durationMinutes") : null)
                .venueId(venue != null ? getLong(venue, "id") : null)
                .venueName(venue != null ? (String) venue.get("name") : null)
                .venueAddress(venue != null ? (String) venue.get("address") : null)
                .cityName(venue != null ? (String) venue.get("cityName") : null)
                .build();
    }

    private ShowSeatInfo mapToShowSeatInfo(Map<String, Object> data) {
        return ShowSeatInfo.builder()
                .id(getLong(data, "id"))
                .seatId(getLong(data, "seatId"))
                .showId(getLong(data, "showId"))
                .seatRow((String) data.get("seatRow"))
                .seatNumber(getInt(data, "seatNumber"))
                .seatLabel((String) data.get("seatLabel"))
                .category((String) data.get("category"))
                .status((String) data.get("status"))
                .price(getDouble(data, "price"))
                .available(Boolean.TRUE.equals(data.get("available")))
                .build();
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private java.time.LocalDateTime parseDateTime(Object value) {
        if (value == null) return null;
        try {
            return java.time.LocalDateTime.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
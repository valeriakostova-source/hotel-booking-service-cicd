package service.booking.reviewapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3307/Booking-service", //Only for booking test to be able to connect to its db, or it crashes
        "spring.datasource.username=booking_user", //Only for booking test to be able to connect to its db, or it crashes
        "spring.datasource.password=booking_password", //Only for booking test to be able to connect to its db, or it crashes
        "REVIEW_DB_CLIENT_URL=http://localhost:8083",
        "JWT_SECRET=bu5HenKK9pCurkUUic604aWzpvY4XruaVZsIkArn0EE9M9GFfTX1vq3vSubM1gwyrZB0Fs22yv5XsWt8jxbT8h"})
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createNewReview_ShouldSaveToDatabase_WhenAuthenticated() throws Exception {
        // 1. Generate a valid JWT token locally (bypassing the offline login service)
        byte[] secretKeyBytes = "bu5HenKK9pCurkUUic604aWzpvY4XruaVZsIkArn0EE9M9GFfTX1vq3vSubM1gwyrZB0Fs22yv5XsWt8jxbT8h".getBytes(StandardCharsets.UTF_8);

        String token = Jwts.builder()
                .subject("1")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(secretKeyBytes))
                .compact();

        String reviewPayload = """
        {
            "roomId": 102,
            "reviewContent": "Integration test review content",
            "reviewScore": 5
        }
        """;

        // 2. Pass the token directly in the Authorization header to test the review endpoint
        mockMvc.perform(post("/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isOk());
    }

    @Test
    void createNewReview_ShouldReturnForbidden_WhenUnauthenticated() throws Exception {

        String jsonPayload = """
                {
                    "roomId": 102,
                    "reviewContent": "Integration test review content",
                    "reviewScore": 5
                }
                """;

        // Without .with(user("1")), the request is unauthenticated
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isForbidden());
    }
}
package service.booking.reservation.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import service.booking.customerapi.client.CustomerClient;
import service.booking.roomapi.entity.Room;
import service.booking.roomapi.repository.RoomRepository;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationRestControllerTest {

    @MockitoBean
    private CustomerClient customerClient;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MockMvc mockMvc;

    @Value("${JWT_SECRET}")
    private String SECRET_KEY;

    @BeforeEach
    public void cleanupDb() {
        roomRepository.deleteAll();
    }

    @Test
    public void createReservationTest() throws Exception {
        Room room = roomRepository.save(new Room(999, "Test Room", BigDecimal.TEN, 2, false));

        when(customerClient.customerExists(any())).thenReturn(true);

        String payload = """
                         {
                           "roomId": {roomId},
                           "checkIn": "2028-07-23",
                           "checkOut": "2028-07-29",
                           "guests": 1
                         }
                """.replace("{roomId}", room.getId().toString());

        mockMvc.perform(post("/api/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", "Bearer " + generateToken(1L))
                )
                .andExpect(status().isCreated());

        verify(customerClient).customerExists(any());
    }

    public String generateToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + 600000))  // +10min
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] bytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}

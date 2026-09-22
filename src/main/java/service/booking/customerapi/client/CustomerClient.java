package service.booking.customerapi.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import service.booking.exceptionhandler.customexeptions.ExternalServiceConnectionException;

@Component
public class CustomerClient {

    private final RestClient restClient;

    public CustomerClient(@Value("${CUSTOMER_CLIENT_URL:http://customer-service:8081}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public record CustomerExistsResponse(Boolean exists) {}

    public boolean customerExists(String token) {
        //ToDo After customer service will be done check if url is correct
        System.err.println("Customer exists: 1");

        try {
            Boolean response = restClient.get()
                    .uri("/api/customers/does-customer-exist")
                    .header("Authorization", formatBearerToken(token))
                    .retrieve()
                    .body(Boolean.class);

            System.err.println("Customer exists: 2" + response);
            return Boolean.TRUE.equals(response);
        } catch (Exception e) {
            throw new ExternalServiceConnectionException("No connection with customer service");
        }
    }

    private String formatBearerToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token;
        }
        return "Bearer " + token;
    }
}
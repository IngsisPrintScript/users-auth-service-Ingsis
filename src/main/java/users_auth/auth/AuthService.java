package users_auth.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import users_auth.dto.UserResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

  @Value("${AUTH0_AUDIENCE}")
  private String domain;

  @Value("${AUTH0_MANAGEMENT_TOKEN}")
  private String managementToken;

  private final RestTemplate restTemplate = new RestTemplate();

  public boolean userExists(String userId) {
    String url = String.format("https://%s/api/v2/users/%s", domain, userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(managementToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error querying Auth0", e);
        }
    }

    public List<UserResult> search(String search) {
        String url = String.format(
                "https://%s/api/v2/users?q=name:*%s* OR email:*%s* OR nickname:*%s*&search_engine=v3",
                domain, search, search, search
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(managementToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    List.class
            );

            List<?> users = response.getBody();
            if (users == null) return List.of();

            return users.stream()
                    .map(obj -> (Map<?, ?>) obj)
                    .map(map -> new UserResult(
                            (String) map.get("user_id"),
                            map.get("name") != null ? (String) map.get("name") :
                                    (map.get("nickname") != null ? (String) map.get("nickname") : "Unknown")
                    ))
                    .collect(Collectors.toList());

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error searching users in Auth0: " + e.getResponseBodyAsString(), e);
        }
    }
}

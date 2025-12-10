package users_auth.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import users_auth.dto.PaginatedUsers;
import users_auth.dto.UserResult;

@Service
public class AuthService {

    @Value("${AUTH0_DOMAIN}")
    private String domain;

    @Value("${AUTH0_MGMT_CLIENT_ID}")
    private String clientId;

    @Value("${AUTH0_MGMT_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${AUTH0_MGMT_AUDIENCE}")
    private String audience;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getManagementToken() {
        String url = "https://" + domain + "/oauth/token";

        Map<String, String> body = Map.of("client_id", clientId, "client_secret", clientSecret, "audience",
                "https://" + domain + "/api/v2/", "grant_type", "client_credentials");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new RuntimeException("Failed to obtain management token");
        }

        return (String) response.getBody().get("access_token");
    }

    public boolean userExists(String userId) {
        String token = getManagementToken();
        String url = String.format("https://%s/api/v2/users/%s", domain, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

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

    public List<UserResult> getUsersByName(String name) {
        String token = getManagementToken();
        String url = String.format("https://%s/api/v2/users?q=name:\"%s\"&search_engine=v3", domain, name);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);

            List<Map<String, Object>> rawUsers = response.getBody();
            if (rawUsers == null)
                return List.of();

            List<UserResult> results = new ArrayList<>();

            for (Map<String, Object> user : rawUsers) {
                results.add(new UserResult((String) user.get("user_id"),
                        user.getOrDefault("name", user.getOrDefault("nickname", "Unknown")).toString()));
            }

            return results;
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error searching users in Auth0: " + e.getResponseBodyAsString(), e);
        }
    }

    public UserResult getUserById(String userId) {
        String token = getManagementToken();
        String url = String.format("https://%s/api/v2/users/%s", domain, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Map<?, ?> user = response.getBody();
            if (user == null)
                return null;

            return new UserResult((String) user.get("user_id"),
                    user.get("name") != null
                            ? (String) user.get("name")
                            : (user.get("nickname") != null ? (String) user.get("nickname") : "Unknown"));
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error getting user from Auth0: " + e.getResponseBodyAsString(), e);
        }
    }

    public PaginatedUsers getUsers(String name, int page, int pageSize) {
        List<UserResult> allUsers;
        if (name == null || name.isBlank()) {
            allUsers = getUsersByName("*");
        } else {
            allUsers = getUsersByName(name);
        }

        int total = allUsers.size();
        int fromIndex = Math.max(0, page * pageSize);
        if (fromIndex >= total) {
            return new PaginatedUsers(page, pageSize, total, List.of());
        }
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<UserResult> paginated = allUsers.subList(fromIndex, toIndex);
        return new PaginatedUsers(page, pageSize, total, paginated);
    }
}
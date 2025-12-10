package users_auth.dto;

import java.util.List;

public record PaginatedUsers(int page, int page_size, int count, List<UserResult> users) {
}

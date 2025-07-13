package springbootbasic.tmdbmoviecollector.dto;

import lombok.Data;

import java.util.List;

@Data
public class GenreListResponse {
    private List<GenreResponse> genres;
}

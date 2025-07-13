package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MoviePageResponse {
    private Integer page;

    @JsonProperty("total_results")
    private Integer totalResults;

    @JsonProperty("total_pages")
    private Integer totalPages;

    private List<MovieResponse> results;
}

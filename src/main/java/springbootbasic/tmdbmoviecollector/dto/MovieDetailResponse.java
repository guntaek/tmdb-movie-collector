package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MovieDetailResponse extends MovieResponse {
    private Long budget;
    private Long revenue;
    private Integer runtime;
    private String status;
    private String tagline;
    private String homepage;

    @JsonProperty("imdb_id")
    private String imdbId;

    private List<GenreResponse> genres;

    @JsonProperty("production_companies")
    private List<ProductionCompanyResponse> productionCompanies;
}

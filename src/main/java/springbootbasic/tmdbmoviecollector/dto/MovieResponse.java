package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MovieResponse {
    private Long id;
    private String title;

    @JsonProperty("original_title")
    private String originalTitle;

    private String overview;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    private Double popularity;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("original_language")
    private String originalLanguage;

    private Boolean adult;

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;
}
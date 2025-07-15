package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PersonDetailResponse {
    private Long id;
    private String name;

    @JsonProperty("profile_path")
    private String profilePath;

    private String biography;
    private LocalDate birthday;
    private LocalDate deathday;

    @JsonProperty("place_of_birth")
    private String placeOfBirth;

    private Double popularity;

    @JsonProperty("also_known_as")
    private List<String> alsoKnownAs;

    @JsonProperty("imdb_id")
    private String imdbId;

    private String homepage;
    private Integer gender;

    @JsonProperty("known_for_department")
    private String knownForDepartment;
}

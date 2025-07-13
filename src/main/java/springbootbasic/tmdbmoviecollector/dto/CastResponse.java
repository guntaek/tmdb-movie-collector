package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CastResponse {
    @JsonProperty("cast_id")
    private Integer castId;

    private String character;

    @JsonProperty("credit_id")
    private String creditId;

    private Integer gender;
    private Long id;
    private String name;
    private Integer order;

    @JsonProperty("profile_path")
    private String profilePath;
}

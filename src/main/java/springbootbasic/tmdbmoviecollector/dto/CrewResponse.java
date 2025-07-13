package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CrewResponse {
    @JsonProperty("credit_id")
    private String creditId;

    private String department;
    private Integer gender;
    private Long id;
    private String job;
    private String name;

    @JsonProperty("profile_path")
    private String profilePath;
}

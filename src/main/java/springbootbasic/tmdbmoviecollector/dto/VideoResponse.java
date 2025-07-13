package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VideoResponse {
    private String id;

    @JsonProperty("iso_639_1")
    private String iso6391;

    @JsonProperty("iso_3166_1")
    private String iso31661;

    private String key;
    private String name;
    private String site;
    private Integer size;
    private String type;
    private Boolean official;

    @JsonProperty("published_at")
    private String publishedAt;
}

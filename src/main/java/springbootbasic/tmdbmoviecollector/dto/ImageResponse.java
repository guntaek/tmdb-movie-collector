package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ImageResponse {
    @JsonProperty("aspect_ratio")
    private Double aspectRatio;

    @JsonProperty("file_path")
    private String filePath;

    private Integer height;

    @JsonProperty("iso_639_1")
    private String iso6391;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    private Integer width;
}

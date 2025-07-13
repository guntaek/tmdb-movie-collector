package springbootbasic.tmdbmoviecollector.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImagesResponse {
    private Long id;
    private List<ImageResponse> backdrops;
    private List<ImageResponse> posters;
}

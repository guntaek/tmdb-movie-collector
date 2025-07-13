package springbootbasic.tmdbmoviecollector.dto;

import lombok.Data;

import java.util.List;

@Data
public class VideosResponse {
    private Long id;
    private List<VideoResponse> results;
}

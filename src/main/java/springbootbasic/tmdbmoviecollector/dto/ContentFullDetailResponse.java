package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ContentFullDetailResponse extends ContentDetailResponse {
    // 기본 상세 정보는 ContentDetailResponse에서 상속

    // 추가 정보들
    private CreditsResponse credits;
    private ImagesResponse images;
    private VideosResponse videos;

    @JsonProperty("watch/providers")
    private WatchProvidersResponse watchProviders;
}

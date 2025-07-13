package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WatchProviderResults {
    @JsonProperty("KR")
    private WatchProviderCountry kr;

    @JsonProperty("US")
    private WatchProviderCountry us;

    // 필요한 다른 국가들 추가 가능
}

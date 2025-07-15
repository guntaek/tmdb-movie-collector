package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProviderResponse {
    @JsonProperty("provider_id")
    private Integer providerId;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("logo_path")
    private String logoPath;

    @JsonProperty("display_priority")
    private Integer displayPriority;
}
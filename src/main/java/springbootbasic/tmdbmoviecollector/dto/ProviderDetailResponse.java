package springbootbasic.tmdbmoviecollector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProviderDetailResponse {
    @JsonProperty("provider_id")
    private Integer providerId;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("logo_path")
    private String logoPath;

    @JsonProperty("display_priority")
    private Integer displayPriority;

    @JsonProperty("origin_country")
    private String originCountry;

    @JsonProperty("headquarters")
    private String headquarters;

    @JsonProperty("homepage")
    private String homepage;

    @JsonProperty("parent_company")
    private String parentCompany;
}

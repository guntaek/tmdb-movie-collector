package springbootbasic.tmdbmoviecollector.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProviderListResponse {
    private List<ProviderResponse> results;
}

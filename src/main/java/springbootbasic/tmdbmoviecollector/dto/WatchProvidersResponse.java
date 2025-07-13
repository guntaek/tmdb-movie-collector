package springbootbasic.tmdbmoviecollector.dto;

import lombok.Data;

@Data
public class WatchProvidersResponse {
    private Long id;
    private WatchProviderResults results;
}
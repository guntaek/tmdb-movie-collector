package springbootbasic.tmdbmoviecollector.dto;

import lombok.Data;

import java.util.List;

@Data
public class WatchProviderCountry {
    private String link;
    private List<WatchProviderInfo> flatrate;
    private List<WatchProviderInfo> rent;
    private List<WatchProviderInfo> buy;
}

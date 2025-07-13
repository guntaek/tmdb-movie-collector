package springbootbasic.tmdbmoviecollector.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreditsResponse {
    private Long id;
    private List<CastResponse> cast;
    private List<CrewResponse> crew;
}

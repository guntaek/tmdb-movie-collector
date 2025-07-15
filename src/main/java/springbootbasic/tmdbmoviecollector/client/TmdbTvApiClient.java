package springbootbasic.tmdbmoviecollector.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import springbootbasic.tmdbmoviecollector.dto.*;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class TmdbTvApiClient {

    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.max-retries}")
    private int maxRetries;

    @Value("${tmdb.api.retry-delay}")
    private long retryDelay;

    public Mono<ContentPageResponse> getOnTheAirTvs(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/on_the_air")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(ContentPageResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching on the air tvs page {}: {}", page, error.getMessage()));
    }

    public Mono<ContentPageResponse> getPopularTvs(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(ContentPageResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching popular tvs page {}: {}", page, error.getMessage()));
    }

    public Mono<ContentPageResponse> getTopRatedTvs(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/top_rated")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(ContentPageResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching top rated tvs page {}: {}", page, error.getMessage()));
    }

    public Mono<GenreListResponse> getGenres() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/genre/tv/list")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(GenreListResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching genres: {}", error.getMessage()));
    }

    public Mono<ContentDetailResponse> getTvDetail(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{tv_id}")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .build(movieId))
                .retrieve()
                .bodyToMono(ContentDetailResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching tv detail for {}: {}", movieId, error.getMessage()));
    }

    // Credits (Cast & Crew)
    public Mono<CreditsResponse> getTvCredits(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{tv_id}/credits")
                        .queryParam("api_key", apiKey)

                        .build(movieId))
                .retrieve()
                .bodyToMono(CreditsResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching credits for tv {}: {}", movieId, error.getMessage()));
    }

    // Images
    public Mono<ImagesResponse> getTvImages(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{tv_id}/images")
                        .queryParam("api_key", apiKey)
                        .queryParam("include_image_language", "ko,null")
                        .build(movieId))
                .retrieve()
                .bodyToMono(ImagesResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching images for tv {}: {}", movieId, error.getMessage()));
    }

    // Videos
    public Mono<VideosResponse> getTvVideos(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{tv_id}/videos")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .build(movieId))
                .retrieve()
                .bodyToMono(VideosResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching videos for tv {}: {}", movieId, error.getMessage()));
    }

    // Watch Providers
    public Mono<WatchProvidersResponse> getTvWatchProviders(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{tv_id}/watch/providers")
                        .queryParam("api_key", apiKey)
                        .build(movieId))
                .retrieve()
                .bodyToMono(WatchProvidersResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching watch providers for tv {}: {}", movieId, error.getMessage()));
    }

    public Mono<PersonDetailResponse> getPersonDetail(Long personId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/person/{person_id}")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .build(personId))
                .retrieve()
                .bodyToMono(PersonDetailResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching person detail for {}: {}", personId, error.getMessage()));
    }

    // 모든 TV Provider 목록 가져오기
    public Mono<ProviderListResponse> getAllTvProviders() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/watch/providers/tv")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .queryParam("watch_region", "KR")
                        .build())
                .retrieve()
                .bodyToMono(ProviderListResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching tv providers: {}", error.getMessage()));
    }

    // Provider 상세 정보
    public Mono<ProviderDetailResponse> getProviderDetail(Integer providerId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/watch/provider/{provider_id}")
                        .queryParam("api_key", apiKey)
                        .build(providerId))
                .retrieve()
                .bodyToMono(ProviderDetailResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching provider detail for {}: {}", providerId, error.getMessage()));
    }

    public Mono<ContentFullDetailResponse> getTvFullDetail(Long tvId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{tv_id}")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .queryParam("append_to_response", "credits,images,videos,watch/providers")
                        .build(tvId))
                .retrieve()
                .bodyToMono(ContentFullDetailResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching full tv detail for {}: {}", tvId, error.getMessage()));
    }
}

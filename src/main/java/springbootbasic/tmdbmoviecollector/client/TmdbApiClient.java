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
public class TmdbApiClient {

    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.max-retries}")
    private int maxRetries;

    @Value("${tmdb.api.retry-delay}")
    private long retryDelay;

    public Mono<MoviePageResponse> getPopularMovies(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(MoviePageResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching popular movies page {}: {}", page, error.getMessage()));
    }

    public Mono<MoviePageResponse> getTopRatedMovies(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/top_rated")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(MoviePageResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching top rated movies page {}: {}", page, error.getMessage()));
    }

    public Mono<MoviePageResponse> getUpcomingMovies(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/upcoming")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(MoviePageResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching upcoming movies page {}: {}", page, error.getMessage()));
    }

    public Mono<GenreListResponse> getGenres() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/genre/movie/list")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(GenreListResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching genres: {}", error.getMessage()));
    }

    public Mono<MovieDetailResponse> getMovieDetail(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .build(movieId))
                .retrieve()
                .bodyToMono(MovieDetailResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching movie detail for {}: {}", movieId, error.getMessage()));
    }

    // Credits (Cast & Crew)
    public Mono<CreditsResponse> getMovieCredits(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}/credits")
                        .queryParam("api_key", apiKey)
                        .build(movieId))
                .retrieve()
                .bodyToMono(CreditsResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching credits for movie {}: {}", movieId, error.getMessage()));
    }

    // Images
    public Mono<ImagesResponse> getMovieImages(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}/images")
                        .queryParam("api_key", apiKey)
                        .queryParam("include_image_language", "ko,null")
                        .build(movieId))
                .retrieve()
                .bodyToMono(ImagesResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching images for movie {}: {}", movieId, error.getMessage()));
    }

    // Videos
    public Mono<VideosResponse> getMovieVideos(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}/videos")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .build(movieId))
                .retrieve()
                .bodyToMono(VideosResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching videos for movie {}: {}", movieId, error.getMessage()));
    }

    // Watch Providers
    public Mono<WatchProvidersResponse> getMovieWatchProviders(Long movieId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}/watch/providers")
                        .queryParam("api_key", apiKey)
                        .build(movieId))
                .retrieve()
                .bodyToMono(WatchProvidersResponse.class)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryDelay)))
                .doOnError(error -> log.error("Error fetching watch providers for movie {}: {}", movieId, error.getMessage()));
    }
}

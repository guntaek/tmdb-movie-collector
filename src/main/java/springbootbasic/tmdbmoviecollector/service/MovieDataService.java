package springbootbasic.tmdbmoviecollector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springbootbasic.tmdbmoviecollector.client.TmdbApiClient;
import springbootbasic.tmdbmoviecollector.dto.GenreResponse;
import springbootbasic.tmdbmoviecollector.dto.MoviePageResponse;
import springbootbasic.tmdbmoviecollector.dto.MovieResponse;
import springbootbasic.tmdbmoviecollector.entity.Genre;
import springbootbasic.tmdbmoviecollector.entity.Movie;
import springbootbasic.tmdbmoviecollector.repository.GenreRepository;
import springbootbasic.tmdbmoviecollector.repository.MovieRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieDataService {

    private final TmdbApiClient tmdbApiClient;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    @Transactional
    public void syncGenres() {
        log.info("Starting genre synchronization...");

        tmdbApiClient.getGenres()
                .doOnNext(response -> log.info("Fetched {} genres", response.getGenres().size()))
                .flatMapMany(response -> Flux.fromIterable(response.getGenres()))
                .map(this::convertToGenreEntity)
                .collectList()
                .doOnNext(genres -> {
                    genreRepository.saveAll(genres);
                    log.info("Saved {} genres to database", genres.size());
                })
                .block();
    }

    @Transactional
    public void syncPopularMovies(int maxPages) {
        log.info("Starting popular movies synchronization for {} pages...", maxPages);
        syncMovies(tmdbApiClient::getPopularMovies, maxPages, "popular");
    }

    @Transactional
    public void syncTopRatedMovies(int maxPages) {
        log.info("Starting top-rated movies synchronization for {} pages...", maxPages);
        syncMovies(tmdbApiClient::getTopRatedMovies, maxPages, "top-rated");
    }

    @Transactional
    public void syncUpcomingMovies(int maxPages) {
        log.info("Starting upcoming movies synchronization for {} pages...", maxPages);
        syncMovies(tmdbApiClient::getUpcomingMovies, maxPages, "upcoming");
    }

    private void syncMovies(java.util.function.Function<Integer, Mono<MoviePageResponse>> apiCall,
                            int maxPages, String movieType) {
        List<Movie> allMovies = new ArrayList<>();

        Flux.range(1, maxPages)
                .concatMap(page -> {
                    log.info("Fetching {} movies page {}/{}", movieType, page, maxPages);
                    return apiCall.apply(page)
                            .delayElement(Duration.ofMillis(250)); // API rate limiting
                })
                .doOnNext(response -> {
                    log.info("Received {} movies from page {}",
                            response.getResults().size(), response.getPage());
                })
                .flatMapIterable(MoviePageResponse::getResults)
                .map(this::convertToMovieEntity)
                .buffer(100) // Batch processing
                .doOnNext(movies -> {
                    movieRepository.saveAll(movies);
                    allMovies.addAll(movies);
                    log.info("Saved batch of {} movies, total: {}", movies.size(), allMovies.size());
                })
                .doOnComplete(() -> log.info("Completed {} movies sync. Total movies saved: {}",
                        movieType, allMovies.size()))
                .doOnError(error -> log.error("Error during {} movies sync: {}", movieType, error.getMessage()))
                .blockLast();
    }

    @Transactional
    public void syncAllMovieData(int pagesPerCategory) {
        // 1. 장르 동기화
        syncGenres();

        // 2. 영화 데이터 동기화
        syncPopularMovies(pagesPerCategory);
        syncTopRatedMovies(pagesPerCategory);
        syncUpcomingMovies(pagesPerCategory);

        log.info("All movie data synchronization completed!");
    }

    private Movie convertToMovieEntity(MovieResponse dto) {
        Movie movie = new Movie();
        movie.setId(dto.getId());
        movie.setTitle(dto.getTitle());
        movie.setOriginalTitle(dto.getOriginalTitle());
        movie.setOverview(dto.getOverview());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setPopularity(dto.getPopularity());
        movie.setVoteAverage(dto.getVoteAverage());
        movie.setVoteCount(dto.getVoteCount());
        movie.setPosterPath(dto.getPosterPath());
        movie.setBackdropPath(dto.getBackdropPath());
        movie.setOriginalLanguage(dto.getOriginalLanguage());
        movie.setAdult(dto.getAdult());

        if (dto.getGenreIds() != null) {
            movie.setGenreIds(new HashSet<>(dto.getGenreIds()));
        }

        return movie;
    }

    private Genre convertToGenreEntity(GenreResponse dto) {
        Genre genre = new Genre();
        genre.setId(dto.getId());
        genre.setName(dto.getName());
        return genre;
    }
}

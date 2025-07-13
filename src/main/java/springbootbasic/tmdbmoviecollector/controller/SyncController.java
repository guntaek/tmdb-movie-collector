package springbootbasic.tmdbmoviecollector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbootbasic.tmdbmoviecollector.repository.MovieRepository;
import springbootbasic.tmdbmoviecollector.service.MovieDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final MovieDataService movieDataService;
    private final MovieRepository movieRepository;

    @PostMapping("/genres")
    public ResponseEntity<String> syncGenres() {
        movieDataService.syncGenres();
        return ResponseEntity.ok("Genre synchronization started");
    }

    @PostMapping("/movies/popular")
    public ResponseEntity<String> syncPopularMovies(@RequestParam(defaultValue = "10") int pages) {
        movieDataService.syncPopularMovies(pages);
        return ResponseEntity.ok("Popular movies synchronization started for " + pages + " pages");
    }

    @PostMapping("/movies/all")
    public ResponseEntity<Map<String, Object>> syncAllMovies(
            @RequestParam(defaultValue = "50") int pagesPerCategory,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All movies synchronization started");
        response.put("pagesPerCategory", pagesPerCategory);
        response.put("includeDetails", includeDetails);
        response.put("estimatedMovies", pagesPerCategory * 20 * 3); // 20 movies per page, 3 categories

        // 비동기로 실행
        new Thread(() -> {
            movieDataService.syncAllMovieData(pagesPerCategory, includeDetails);
        }).start();

        return ResponseEntity.ok(response);
    }

    // 특정 영화들의 상세 정보만 동기화
    @PostMapping("/movies/details")
    public ResponseEntity<String> syncMovieDetails(@RequestBody List<Long> movieIds) {
        movieDataService.syncMovieDetails(movieIds);
        return ResponseEntity.ok("Movie details synchronization started for " + movieIds.size() + " movies");
    }

    // 특정 영화들의 출연진/제작진 정보만 동기화
    @PostMapping("/movies/credits")
    public ResponseEntity<String> syncMovieCredits(@RequestBody List<Long> movieIds) {
        movieDataService.syncMovieCredits(movieIds);
        return ResponseEntity.ok("Movie credits synchronization started for " + movieIds.size() + " movies");
    }

    // 특정 영화들의 이미지 정보만 동기화
    @PostMapping("/movies/images")
    public ResponseEntity<String> syncMovieImages(@RequestBody List<Long> movieIds) {
        movieDataService.syncMovieImages(movieIds);
        return ResponseEntity.ok("Movie images synchronization started for " + movieIds.size() + " movies");
    }

    // 특정 영화들의 비디오 정보만 동기화
    @PostMapping("/movies/videos")
    public ResponseEntity<String> syncMovieVideos(@RequestBody List<Long> movieIds) {
        movieDataService.syncMovieVideos(movieIds);
        return ResponseEntity.ok("Movie videos synchronization started for " + movieIds.size() + " movies");
    }

    // 특정 영화들의 스트리밍 제공자 정보만 동기화
    @PostMapping("/movies/providers")
    public ResponseEntity<String> syncWatchProviders(@RequestBody List<Long> movieIds) {
        movieDataService.syncWatchProviders(movieIds);
        return ResponseEntity.ok("Watch providers synchronization started for " + movieIds.size() + " movies");
    }

    // DB에 저장된 모든 영화의 상세 정보 동기화
    @PostMapping("/movies/all-details")
    public ResponseEntity<Map<String, Object>> syncAllMovieDetails() {
        List<Long> allMovieIds = movieRepository.findAllMovieIds();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All movie details synchronization started");
        response.put("totalMovies", allMovieIds.size());

        // 비동기로 실행
        new Thread(() -> {
            movieDataService.syncMovieDetails(allMovieIds);
            movieDataService.syncMovieCredits(allMovieIds);
            movieDataService.syncMovieImages(allMovieIds);
            movieDataService.syncMovieVideos(allMovieIds);
            movieDataService.syncWatchProviders(allMovieIds);
        }).start();

        return ResponseEntity.ok(response);
    }

    // 동기화 상태 확인
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalMovies", movieRepository.count());
        status.put("movieIds", movieRepository.findAllMovieIds().size());

        return ResponseEntity.ok(status);
    }
}

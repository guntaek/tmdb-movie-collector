package springbootbasic.tmdbmoviecollector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbootbasic.tmdbmoviecollector.repository.ContentRepository;
import springbootbasic.tmdbmoviecollector.service.ContentDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final ContentDataService contentDataService;
    private final ContentRepository contentRepository;

    @PostMapping("/genres")
    public ResponseEntity<String> syncGenres() {
        contentDataService.syncGenres();
        return ResponseEntity.ok("Genre synchronization started");
    }

    @PostMapping("/movies/popular")
    public ResponseEntity<String> syncPopular(@RequestParam(defaultValue = "10") int pages) {
        contentDataService.syncPopular(pages);
        return ResponseEntity.ok("Popular movies synchronization started for " + pages + " pages");
    }

    @PostMapping("/movies/all")
    public ResponseEntity<Map<String, Object>> syncAll(
            @RequestParam(defaultValue = "50") int pagesPerCategory,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All movies synchronization started");
        response.put("pagesPerCategory", pagesPerCategory);
        response.put("includeDetails", includeDetails);
        response.put("estimatedMovies", pagesPerCategory * 20 * 3); // 20 movies per page, 3 categories

        // 비동기로 실행
        new Thread(() -> {
            contentDataService.syncAllContentData(pagesPerCategory, includeDetails);
        }).start();

        return ResponseEntity.ok(response);
    }

    // 특정 영화들의 상세 정보만 동기화
    @PostMapping("/movies/details")
    public ResponseEntity<String> syncMovieDetails(@RequestBody List<Long> movieIds) {
        contentDataService.syncMovieDetails(movieIds);
        return ResponseEntity.ok("Movie details synchronization started for " + movieIds.size() + " movies");
    }

    @PostMapping("/tvs/details")
    public ResponseEntity<String> syncTvDetails(@RequestBody List<Long> tvIds) {
        contentDataService.syncTvDetails(tvIds);
        return ResponseEntity.ok("Tv details synchronization started for " + tvIds.size() + " tvs");
    }

    // 특정 영화들의 출연진/제작진 정보만 동기화
    @PostMapping("/movies/credits")
    public ResponseEntity<String> syncMovieCredits(@RequestBody List<Long> movieIds) {
        contentDataService.syncMovieCredits(movieIds);
        return ResponseEntity.ok("Movie credits synchronization started for " + movieIds.size() + " movies");
    }

    @PostMapping("/tvs/credits")
    public ResponseEntity<String> syncTvCredits(@RequestBody List<Long> tvIds) {
        contentDataService.syncTvCredits(tvIds);
        return ResponseEntity.ok("Tv credits synchronization started for " + tvIds.size() + " tvs");
    }

    // 특정 영화들의 이미지 정보만 동기화
    @PostMapping("/movies/images")
    public ResponseEntity<String> syncMovieImages(@RequestBody List<Long> movieIds) {
        contentDataService.syncMovieImages(movieIds);
        return ResponseEntity.ok("Movie images synchronization started for " + movieIds.size() + " movies");
    }

    @PostMapping("/tvs/images")
    public ResponseEntity<String> syncTvImages(@RequestBody List<Long> tvIds) {
        contentDataService.syncTvImages(tvIds);
        return ResponseEntity.ok("Tv images synchronization started for " + tvIds.size() + " tvs");
    }

    // 특정 영화들의 비디오 정보만 동기화
    @PostMapping("/movies/videos")
    public ResponseEntity<String> syncMovieVideos(@RequestBody List<Long> movieIds) {
        contentDataService.syncMovieVideos(movieIds);
        return ResponseEntity.ok("Movie videos synchronization started for " + movieIds.size() + " movies");
    }

    @PostMapping("/tvs/videos")
    public ResponseEntity<String> syncTvVideos(@RequestBody List<Long> tvIds) {
        contentDataService.syncTvVideos(tvIds);
        return ResponseEntity.ok("Tv videos synchronization started for " + tvIds.size() + " tvs");
    }

    // 특정 영화들의 스트리밍 제공자 정보만 동기화
    @PostMapping("/movies/providers")
    public ResponseEntity<String> syncWatchMovieProviders(@RequestBody List<Long> movieIds) {
        contentDataService.syncWatchMovieProviders(movieIds);
        return ResponseEntity.ok("Movie watch providers synchronization started for " + movieIds.size() + " movies");
    }

    @PostMapping("/tvs/providers")
    public ResponseEntity<String> syncWatchTvProviders(@RequestBody List<Long> tvIds) {
        contentDataService.syncWatchTvProviders(tvIds);
        return ResponseEntity.ok("Tv watch providers synchronization started for " + tvIds.size() + " tvs");
    }

    // DB에 저장된 모든 영화의 상세 정보 동기화
    @PostMapping("/movies/all-details")
    public ResponseEntity<Map<String, Object>> syncAllContentDetails() {
        List<Long> allMovieIds = contentRepository.findAllMovieIds();
        List<Long> allTvsIds = contentRepository.findAllTvIds();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All content details synchronization started");
        response.put("totalContents", allMovieIds.size() + allTvsIds.size());

        // 비동기로 실행
        new Thread(() -> {
            contentDataService.syncMovieDetails(allMovieIds);
            contentDataService.syncTvDetails(allTvsIds);
            contentDataService.syncMovieCredits(allMovieIds);
            contentDataService.syncTvCredits(allTvsIds);
            contentDataService.syncMovieImages(allMovieIds);
            contentDataService.syncTvImages(allTvsIds);
            contentDataService.syncMovieVideos(allMovieIds);
            contentDataService.syncTvVideos(allTvsIds);
            contentDataService.syncWatchMovieProviders(allMovieIds);
            contentDataService.syncWatchTvProviders(allTvsIds);
        }).start();

        return ResponseEntity.ok(response);
    }

    // 동기화 상태 확인
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalContents", contentRepository.count());
        status.put("contentIds", contentRepository.findAllMovieIds().size());

        return ResponseEntity.ok(status);
    }
}

package springbootbasic.tmdbmoviecollector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springbootbasic.tmdbmoviecollector.service.MovieDataService;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final MovieDataService movieDataService;

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
    public ResponseEntity<String> syncAllMovies(@RequestParam(defaultValue = "50") int pagesPerCategory) {
        movieDataService.syncAllMovieData(pagesPerCategory);
        return ResponseEntity.ok("All movies synchronization started");
    }
}

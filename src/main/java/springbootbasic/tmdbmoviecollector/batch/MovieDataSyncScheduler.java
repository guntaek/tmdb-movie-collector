package springbootbasic.tmdbmoviecollector.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springbootbasic.tmdbmoviecollector.service.MovieDataService;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieDataSyncScheduler {

    private final MovieDataService movieDataService;

    // 매일 새벽 2시에 실행
    @Scheduled(cron = "0 0 2 * * *")
    public void syncMovieData() {
        log.info("Starting scheduled movie data sync...");
        try {
            movieDataService.syncAllMovieData(100); // 각 카테고리별 100페이지
        } catch (Exception e) {
            log.error("Error during scheduled sync: ", e);
        }
    }
}

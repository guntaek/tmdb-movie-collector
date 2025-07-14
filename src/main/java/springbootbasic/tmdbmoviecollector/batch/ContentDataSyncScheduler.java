package springbootbasic.tmdbmoviecollector.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springbootbasic.tmdbmoviecollector.service.ContentDataService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentDataSyncScheduler {

    private final ContentDataService contentDataService;

    // 매일 새벽 2시에 실행
    @Scheduled(cron = "0 0 2 * * *")
    public void syncContentData() {
        log.info("Starting scheduled movie data sync...");
        try {
            contentDataService.syncAllContentData(100, true); // 각 카테고리별 100페이지
        } catch (Exception e) {
            log.error("Error during scheduled sync: ", e);
        }
    }
}

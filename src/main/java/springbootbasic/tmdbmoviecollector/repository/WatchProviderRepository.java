package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.WatchProvider;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;

@Repository
public interface WatchProviderRepository extends JpaRepository<WatchProvider, Long> {
    void deleteByContent_Id(ContentId contentId);
}

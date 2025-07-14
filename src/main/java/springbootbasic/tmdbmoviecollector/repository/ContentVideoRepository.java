package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.ContentVideo;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;

@Repository
public interface ContentVideoRepository extends JpaRepository<ContentVideo, String> {
    void deleteByContent_Id(ContentId contentId);
}

package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.ContentImage;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;

@Repository
public interface ContentImageRepository extends JpaRepository<ContentImage, Long> {
    void deleteByContent_Id(ContentId contentId);
}

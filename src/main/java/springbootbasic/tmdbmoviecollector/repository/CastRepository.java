package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.Cast;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;

@Repository
public interface CastRepository extends JpaRepository<Cast, Long> {
    void deleteByContent_id(ContentId contentId);
}
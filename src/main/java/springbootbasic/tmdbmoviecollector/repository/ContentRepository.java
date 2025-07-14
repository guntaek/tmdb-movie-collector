package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.Content;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, ContentId> {
    @Query("SELECT c.id.id FROM Content c WHERE c.id.type = 'movie'")
    List<Long> findAllMovieIds();

    @Query("SELECT c.id.id FROM Content c WHERE c.id.type = 'tv'")
    List<Long> findAllTvIds();
}


package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.MovieVideo;

@Repository
public interface MovieVideoRepository extends JpaRepository<MovieVideo, String> {
    void deleteByMovieId(Long movieId);
}

package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.MovieImage;

@Repository
public interface MovieImageRepository extends JpaRepository<MovieImage, Long> {
    void deleteByMovieId(Long movieId);
}

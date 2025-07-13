package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.Cast;

@Repository
public interface CastRepository extends JpaRepository<Cast, Long> {
    void deleteByMovieId(Long movieId);
}
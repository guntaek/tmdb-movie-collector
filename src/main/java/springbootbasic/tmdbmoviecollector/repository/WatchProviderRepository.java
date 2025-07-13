package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.WatchProvider;

@Repository
public interface WatchProviderRepository extends JpaRepository<WatchProvider, Long> {
    void deleteByMovieId(Long movieId);
}

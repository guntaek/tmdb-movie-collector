package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.Crew;

@Repository
public interface CrewRepository extends JpaRepository<Crew, Long> {
    void deleteByMovieId(Long movieId);
}

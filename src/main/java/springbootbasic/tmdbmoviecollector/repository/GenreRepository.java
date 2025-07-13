package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
}

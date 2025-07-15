package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.Actor;
import springbootbasic.tmdbmoviecollector.entity.Content;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;

import java.util.List;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {
    List<Actor> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c.actor FROM Cast c WHERE c.content.id = :contentId")
    List<Actor> findByContentId(@Param("contentId") ContentId contentId);

    @Query("SELECT c.content FROM Cast c WHERE c.actor.id = :actorId")
    List<Content> findContentsByActorId(@Param("actorId") Long actorId);
}

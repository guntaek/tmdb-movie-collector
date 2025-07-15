package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.Content;
import springbootbasic.tmdbmoviecollector.entity.CrewMember;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;

import java.util.List;

@Repository
public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {
    List<CrewMember> findByNameContainingIgnoreCase(String name);
    List<CrewMember> findByKnownForDepartment(String department);

    @Query("SELECT c.crewMember FROM Crew c WHERE c.content.id = :contentId")
    List<CrewMember> findByContentId(@Param("contentId") ContentId contentId);

    @Query("SELECT c.crewMember FROM Crew c WHERE c.content.id = :contentId AND c.job = :job")
    List<CrewMember> findByContentIdAndJob(@Param("contentId") ContentId contentId, @Param("job") String job);

    @Query("SELECT c.content FROM Crew c WHERE c.crewMember.id = :crewMemberId")
    List<Content> findContentsByCrewMemberId(@Param("crewMemberId") Long crewMemberId);
}

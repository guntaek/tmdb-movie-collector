package springbootbasic.tmdbmoviecollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springbootbasic.tmdbmoviecollector.entity.Content;
import springbootbasic.tmdbmoviecollector.entity.Provider;
import springbootbasic.tmdbmoviecollector.entity.WatchProvider;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;
import springbootbasic.tmdbmoviecollector.entity.type.ProviderType;

import java.util.List;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Integer> {
    List<Provider> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT w.provider FROM WatchProvider w WHERE w.content.id = :contentId")
    List<Provider> findByContentId(@Param("contentId") ContentId contentId);

    @Query("SELECT DISTINCT w.provider FROM WatchProvider w WHERE w.content.id = :contentId AND w.type = :type")
    List<Provider> findByContentIdAndType(@Param("contentId") ContentId contentId, @Param("type") ProviderType type);

    @Query("SELECT w.content FROM WatchProvider w WHERE w.provider.id = :providerId")
    List<Content> findContentsByProviderId(@Param("providerId") Integer providerId);

    @Query("SELECT w.content FROM WatchProvider w WHERE w.provider.id = :providerId AND w.country = :country")
    List<Content> findContentsByProviderIdAndCountry(@Param("providerId") Integer providerId, @Param("country") String country);

    @Query("SELECT p FROM Provider p WHERE p.id IN " +
            "(SELECT DISTINCT w.provider.id FROM WatchProvider w WHERE w.country = :country) " +
            "ORDER BY p.name")
    List<Provider> findProvidersByCountry(@Param("country") String country);

    @Query("SELECT COUNT(DISTINCT w.content) FROM WatchProvider w WHERE w.provider.id = :providerId")
    Long countContentsByProviderId(@Param("providerId") Integer providerId);

    @Query("SELECT COUNT(DISTINCT w.content) FROM WatchProvider w " +
            "WHERE w.provider.id = :providerId AND w.type = :type")
    Long countContentsByProviderIdAndType(@Param("providerId") Integer providerId, @Param("type") ProviderType type);

    // WatchProviderRepository에 추가
    @Query("SELECT w FROM WatchProvider w " +
            "WHERE w.content.id = :contentId AND w.country = :country " +
            "ORDER BY w.type, w.displayPriority")
    List<WatchProvider> findByContentIdAndCountryOrderByTypeAndPriority(
            @Param("contentId") ContentId contentId,
            @Param("country") String country);
}
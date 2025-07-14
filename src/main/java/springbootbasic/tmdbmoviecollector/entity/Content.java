package springbootbasic.tmdbmoviecollector.entity;

import jakarta.persistence.*;
import jdk.jfr.ContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"casts", "crews", "images", "videos", "watchProviders"})
public class Content {

    @EmbeddedId
    private ContentId id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "rating_count")
    private Long ratingCount;

    @Column(name = "rating_average")
    private BigDecimal ratingAverage;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(name = "trailer_path")
    private String trailerPath;

    @ElementCollection
    @CollectionTable(
            name = "content_genre_ids",
            joinColumns = {
                    @JoinColumn(name = "content_id",   referencedColumnName = "id"),
                    @JoinColumn(name = "content_type", referencedColumnName = "type")
            }
    )
    @Column(name = "genre_id")
    private Set<Integer> genreIds = new HashSet<>();

    // 관계 매핑
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cast> casts = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Crew> crews = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentVideo> videos = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WatchProvider> watchProviders = new ArrayList<>();
}

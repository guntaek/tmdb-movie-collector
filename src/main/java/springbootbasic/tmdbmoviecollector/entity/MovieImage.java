package springbootbasic.tmdbmoviecollector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movie_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(name = "file_path")
    private String filePath;

    @Enumerated(EnumType.STRING)
    private ImageType type; // POSTER, BACKDROP

    private Integer width;
    private Integer height;

    @Column(name = "aspect_ratio")
    private Double aspectRatio;

    @Column(name = "vote_average")
    private Double voteAverage;

    @Column(name = "iso_639_1")
    private String iso6391;
}


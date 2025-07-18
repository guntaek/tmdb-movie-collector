package springbootbasic.tmdbmoviecollector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "casts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id")
    private Long personId;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(length = 500)
    private String name;

    @Column(name = "character_name", length = 1000)
    private String character;

    @Column(name = "cast_order")
    private Integer order;

    @Column(name = "profile_path", length = 500)
    private String profilePath;
}

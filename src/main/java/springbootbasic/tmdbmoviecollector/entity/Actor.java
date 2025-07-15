package springbootbasic.tmdbmoviecollector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"castRoles"})
public class Actor {
    @Id
    private Long id; // TMDB person ID

    @Column(nullable = false, length = 500)
    private String name;

    @Column(name = "profile_path", length = 500)
    private String profilePath;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "deathday")
    private LocalDate deathday;

    @Column(name = "place_of_birth", length = 500)
    private String placeOfBirth;

    @Column(name = "popularity")
    private Double popularity;

    @Column(name = "gender")
    private Integer gender; // 0: Not specified, 1: Female, 2: Male, 3: Non-binary

    // 양방향 관계
    @OneToMany(mappedBy = "actor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cast> castRoles = new ArrayList<>();
}

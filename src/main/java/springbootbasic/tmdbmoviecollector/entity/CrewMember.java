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
@Table(name = "crew_member")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"crewRoles"})
public class CrewMember {
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

    @Column(name = "known_for_department", length = 100)
    private String knownForDepartment;

    @Column(name = "gender")
    private Integer gender;

    // 양방향 관계
    @OneToMany(mappedBy = "crewMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Crew> crewRoles = new ArrayList<>();
}

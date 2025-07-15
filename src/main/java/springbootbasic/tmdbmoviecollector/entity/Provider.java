package springbootbasic.tmdbmoviecollector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "provider")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"watchProviders"})
public class Provider {
    @Id
    private Integer id; // TMDB provider ID

    @Column(nullable = false, length = 500)
    private String name;

    @Column(name = "logo_path", length = 500)
    private String logoPath;

    @Column(name = "origin_country", length = 10)
    private String originCountry;

    // 양방향 관계
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WatchProvider> watchProviders = new ArrayList<>();
}

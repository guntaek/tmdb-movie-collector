package springbootbasic.tmdbmoviecollector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "watch_providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(name = "provider_id")
    private Integer providerId;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "logo_path")
    private String logoPath;

    @Enumerated(EnumType.STRING)
    private ProviderType type; // STREAM, RENT, BUY

    @Column(name = "display_priority")
    private Integer displayPriority;

    private String country;
}


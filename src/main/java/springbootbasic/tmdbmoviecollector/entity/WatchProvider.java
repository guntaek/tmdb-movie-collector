package springbootbasic.tmdbmoviecollector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import springbootbasic.tmdbmoviecollector.entity.type.ProviderType;

@Entity
@Table(name = "watch_provider")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "content_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "content_type", referencedColumnName = "type", nullable = false)
    })
    private Content content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProviderType type; // STREAM, RENT, BUY

    @Column(name = "display_priority")
    private Integer displayPriority;

    @Column(length = 10)
    private String country;
}

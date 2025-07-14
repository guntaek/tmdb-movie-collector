package springbootbasic.tmdbmoviecollector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_video")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentVideo {
    @Id
    private String id; // YouTube video ID

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "content_id",  referencedColumnName = "id",   nullable = false),
            @JoinColumn(name = "content_type", referencedColumnName = "type", nullable = false)
    })
    private Content content;

    private String name;

    @Column(name = "video_key")
    private String key;

    private String site;
    private Integer size;
    private String type; // Trailer, Teaser, Clip, etc.
    private Boolean official;

    @Column(name = "published_at")
    private String publishedAt;
}
package springbootbasic.tmdbmoviecollector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movie_videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieVideo {
    @Id
    private String id; // YouTube video ID

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

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
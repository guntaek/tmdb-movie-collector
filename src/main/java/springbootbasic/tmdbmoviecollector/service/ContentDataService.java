package springbootbasic.tmdbmoviecollector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springbootbasic.tmdbmoviecollector.client.TmdbMovieApiClient;
import springbootbasic.tmdbmoviecollector.client.TmdbTvApiClient;
import springbootbasic.tmdbmoviecollector.dto.*;
import springbootbasic.tmdbmoviecollector.entity.*;
import springbootbasic.tmdbmoviecollector.entity.key.ContentId;
import springbootbasic.tmdbmoviecollector.entity.type.ImageType;
import springbootbasic.tmdbmoviecollector.entity.type.ProviderType;
import springbootbasic.tmdbmoviecollector.repository.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentDataService {

    private final TmdbTvApiClient tmdbTvApiClient;
    private final TmdbMovieApiClient tmdbMovieApiClient;
    private final ContentRepository contentRepository;
    private final GenreRepository genreRepository;
    private final CastRepository castRepository;
    private final CrewRepository crewRepository;
    private final ActorRepository actorRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final ContentImageRepository imageRepository;
    private final ContentVideoRepository videoRepository;
    private final WatchProviderRepository watchProviderRepository;

    @Transactional
    public void syncGenres() {
        log.info("Starting genre synchronization...");

        tmdbMovieApiClient.getGenres()
                .doOnNext(response -> log.info("Fetched {} Movie genres", response.getGenres().size()))
                .flatMapMany(response -> Flux.fromIterable(response.getGenres()))
                .map(this::convertToGenreEntity)
                .collectList()
                .doOnNext(genres -> {
                    genreRepository.saveAll(genres);
                    log.info("Saved {} Movie genres to database", genres.size());
                })
                .block();

        tmdbTvApiClient.getGenres()
                .doOnNext(response -> log.info("Fetched {} TV genres", response.getGenres().size()))
                .flatMapMany(response -> Flux.fromIterable(response.getGenres()))
                .map(this::convertToGenreEntity)
                .collectList()
                .doOnNext(genres -> {
                    genreRepository.saveAll(genres);
                    log.info("Saved {} TV genres to database", genres.size());
                })
                .block();
    }

    @Transactional
    public void syncNowPlayingMovies(int maxPages) {
        log.info("Starting now playing movies synchronization for {} pages...", maxPages);
        syncMovies(tmdbMovieApiClient::getNowPlayingMovies, maxPages, "now playing");
    }

    @Transactional
    public void syncUpcomingMovies(int maxPages) {
        log.info("Starting upcoming movies synchronization for {} pages...", maxPages);
        syncMovies(tmdbMovieApiClient::getUpcomingMovies, maxPages, "upcoming");
    }

    @Transactional
    public void syncOnTheAirTvs(int maxPages) {
        log.info("Starting now playing movies synchronization for {} pages...", maxPages);
        syncTvs(tmdbTvApiClient::getOnTheAirTvs, maxPages, "now playing");
    }

    @Transactional
    public void syncPopular(int maxPages) {
        log.info("Starting popular movies synchronization for {} pages...", maxPages);
        syncMovies(tmdbMovieApiClient::getPopularMovies, maxPages, "popular");

        log.info("Starting popular tvs synchronization for {} pages...", maxPages);
        syncTvs(tmdbTvApiClient::getPopularTvs, maxPages, "popular");
    }

    @Transactional
    public void syncTopRated(int maxPages) {
        log.info("Starting top-rated movies synchronization for {} pages...", maxPages);
        syncMovies(tmdbMovieApiClient::getTopRatedMovies, maxPages, "top-rated");

        log.info("Starting top-rated tvs synchronization for {} pages...", maxPages);
        syncTvs(tmdbTvApiClient::getTopRatedTvs, maxPages, "top-rated");
    }

    private void syncMovies(java.util.function.Function<Integer, Mono<ContentPageResponse>> apiCall,
                            int maxPages, String type) {
        List<Content> allMovies = new ArrayList<>();

        Flux.range(1, maxPages)
                .concatMap(page -> {
                    log.info("Fetching {} movies page {}/{}", type, page, maxPages);
                    return apiCall.apply(page)
                            .delayElement(Duration.ofMillis(250)); // API rate limiting
                })
                .doOnNext(response -> {
                    log.info("Received {} movies from page {}",
                            response.getResults().size(), response.getPage());
                })
                .flatMapIterable(ContentPageResponse::getResults)
                .map(this::convertToMovieEntity)
                .buffer(100) // Batch processing
                .doOnNext(movies -> {
                    contentRepository.saveAll(movies);
                    allMovies.addAll(movies);
                    log.info("Saved batch of {} movies, total: {}", movies.size(), allMovies.size());
                })
                .doOnComplete(() -> log.info("Completed {} movies sync. Total movies saved: {}",
                        type, allMovies.size()))
                .doOnError(error -> log.error("Error during {} movies sync: {}", type, error.getMessage()))
                .blockLast();
    }

    private void syncTvs(java.util.function.Function<Integer, Mono<ContentPageResponse>> apiCall,
                            int maxPages, String type) {
        List<Content> allMovies = new ArrayList<>();

        Flux.range(1, maxPages)
                .concatMap(page -> {
                    log.info("Fetching {} tvs page {}/{}", type, page, maxPages);
                    return apiCall.apply(page)
                            .delayElement(Duration.ofMillis(250)); // API rate limiting
                })
                .doOnNext(response -> {
                    log.info("Received {} tvs from page {}",
                            response.getResults().size(), response.getPage());
                })
                .flatMapIterable(ContentPageResponse::getResults)
                .map(this::convertToTvEntity)
                .buffer(100) // Batch processing
                .doOnNext(tvs -> {
                    contentRepository.saveAll(tvs);
                    allMovies.addAll(tvs);
                    log.info("Saved batch of {} tvs, total: {}", tvs.size(), allMovies.size());
                })
                .doOnComplete(() -> log.info("Completed {} tvs sync. Total tvs saved: {}",
                        type, allMovies.size()))
                .doOnError(error -> log.error("Error during {} tvs sync: {}", type, error.getMessage()))
                .blockLast();
    }

    // 영화 상세 정보 동기화
    @Transactional
    public void syncMovieDetails(List<Long> movieIds) {
        log.info("Starting movie details sync for {} movies", movieIds.size());

        Flux.fromIterable(movieIds)
                .concatMap(movieId -> {
                    log.info("Fetching details for movie ID: {}", movieId);
                    return tmdbMovieApiClient.getMovieDetail(movieId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching details for movie {}: {}", movieId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(detail -> {
                    updateContentWithDetails(detail, "movie");
                    log.info("Updated movie: {} with detailed info", detail.getTitle());
                })
                .blockLast();
    }

    // 영화 상세 정보 동기화
    @Transactional
    public void syncTvDetails(List<Long> tvIds) {
        log.info("Starting tv details sync for {} tvs", tvIds.size());

        Flux.fromIterable(tvIds)
                .concatMap(tvId -> {
                    log.info("Fetching details for tv ID: {}", tvId);
                    return tmdbTvApiClient.getTvDetail(tvId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching details for tv {}: {}", tvId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(detail -> {
                    updateContentWithDetails(detail, "tv");
                    log.info("Updated tv: {} with detailed info", detail.getTitle());
                })
                .blockLast();
    }

    // 출연진/제작진 정보 동기화
    @Transactional
    public void syncMovieCredits(List<Long> movieIds) {
        log.info("Starting credits sync for {} movies", movieIds.size());

        Flux.fromIterable(movieIds)
                .concatMap(movieId -> {
                    log.info("Fetching credits for movie ID: {}", movieId);
                    return tmdbMovieApiClient.getMovieCredits(movieId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching credits for movie {}: {}", movieId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(credits -> {
                    saveCredits(credits, "movie");
                    log.info("Saved credits for movie ID: {}", credits.getId());
                })
                .blockLast();
    }

    // 출연진/제작진 정보 동기화
    @Transactional
    public void syncTvCredits(List<Long> tvIds) {
        log.info("Starting credits sync for {} tvs", tvIds.size());

        Flux.fromIterable(tvIds)
                .concatMap(tvId -> {
                    log.info("Fetching credits for tv ID: {}", tvId);
                    return tmdbTvApiClient.getTvCredits(tvId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching credits for tv {}: {}", tvId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(credits -> {
                    saveCredits(credits, "tv");
                    log.info("Saved credits for tv ID: {}", credits.getId());
                })
                .blockLast();
    }

    // 이미지 정보 동기화
    @Transactional
    public void syncMovieImages(List<Long> movieIds) {
        log.info("Starting images sync for {} movies", movieIds.size());

        Flux.fromIterable(movieIds)
                .concatMap(movieId -> {
                    return tmdbMovieApiClient.getMovieImages(movieId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching images for movie {}: {}", movieId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(images -> {
                    saveImages(images, "movie");
                    log.info("Saved images for movie ID: {}", images.getId());
                })
                .blockLast();
    }

    // 이미지 정보 동기화
    @Transactional
    public void syncTvImages(List<Long> tvIds) {
        log.info("Starting images sync for {} tvs", tvIds.size());

        Flux.fromIterable(tvIds)
                .concatMap(tvId -> {
                    return tmdbTvApiClient.getTvImages(tvId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching images for tv {}: {}", tvId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(images -> {
                    saveImages(images, "tv");
                    log.info("Saved images for tv ID: {}", images.getId());
                })
                .blockLast();
    }

    // 비디오 정보 동기화
    @Transactional
    public void syncMovieVideos(List<Long> movieIds) {
        log.info("Starting videos sync for {} movies", movieIds.size());

        Flux.fromIterable(movieIds)
                .concatMap(movieId -> {
                    return tmdbMovieApiClient.getMovieVideos(movieId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching videos for movie {}: {}", movieId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(videos -> {
                    saveVideos(videos, "movie");
                    log.info("Saved videos for movie ID: {}", videos.getId());
                })
                .blockLast();
    }

    // 비디오 정보 동기화
    @Transactional
    public void syncTvVideos(List<Long> tvIds) {
        log.info("Starting videos sync for {} tvs", tvIds.size());

        Flux.fromIterable(tvIds)
                .concatMap(tvId -> {
                    return tmdbTvApiClient.getTvVideos(tvId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching videos for tv {}: {}", tvId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(videos -> {
                    saveVideos(videos, "tv");
                    log.info("Saved videos for tv ID: {}", videos.getId());
                })
                .blockLast();
    }

    // 스트리밍 제공자 정보 동기화
    @Transactional
    public void syncWatchMovieProviders(List<Long> movieIds) {
        log.info("Starting watch providers sync for {} movies", movieIds.size());

        Flux.fromIterable(movieIds)
                .concatMap(movieId -> {
                    return tmdbMovieApiClient.getMovieWatchProviders(movieId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching watch providers for movie {}: {}", movieId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(providers -> {
                    saveWatchProviders(providers, "movie");
                    log.info("Saved watch providers for movie ID: {}", providers.getId());
                })
                .blockLast();
    }

    // 스트리밍 제공자 정보 동기화
    @Transactional
    public void syncWatchTvProviders(List<Long> tvIds) {
        log.info("Starting watch providers sync for {} tvs", tvIds.size());

        Flux.fromIterable(tvIds)
                .concatMap(movieId -> {
                    return tmdbTvApiClient.getTvWatchProviders(movieId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching watch providers for tv {}: {}", movieId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(providers -> {
                    saveWatchProviders(providers, "tv");
                    log.info("Saved watch providers for tv ID: {}", providers.getId());
                })
                .blockLast();
    }

    // 전체 데이터 동기화 (기본 + 상세 정보)
    @Transactional
    public void syncAllContentData(int pagesPerCategory, boolean includeDetails) {
        // 1. 장르 동기화
        syncGenres();

        // 2. 기본 데이터 동기화
        syncNowPlayingMovies(pagesPerCategory);
        syncUpcomingMovies(pagesPerCategory);
        syncOnTheAirTvs(pagesPerCategory);
        syncPopular(pagesPerCategory);
        syncTopRated(pagesPerCategory);

        if (includeDetails) {
            // 3. 저장된 모든 영화 ID 가져오기
            List<Long> allMovieIds = contentRepository.findAllMovieIds();
            log.info("Found {} movies in database. Starting detailed sync...", allMovieIds.size());

            // 4. 상세 정보 동기화 (배치로 처리)
            int batchSize = 100;
            for (int i = 0; i < allMovieIds.size(); i += batchSize) {
                List<Long> batch = allMovieIds.subList(i, Math.min(i + batchSize, allMovieIds.size()));
                log.info("Processing batch {}/{}", (i/batchSize) + 1, (allMovieIds.size()/batchSize) + 1);

                syncMovieDetails(batch);
                syncMovieCredits(batch);
                syncMovieImages(batch);
                syncMovieVideos(batch);
                syncWatchMovieProviders(batch);
            }

            List<Long> allTvIds = contentRepository.findAllTvIds();
            log.info("Found {} tvs in database. Starting detailed sync...", allTvIds.size());

            for (int i = 0; i < allTvIds.size(); i += batchSize) {
                List<Long> batch = allTvIds.subList(i, Math.min(i + batchSize, allTvIds.size()));
                log.info("Processing batch {}/{}", (i/batchSize) + 1, (allTvIds.size()/batchSize) + 1);

                syncTvDetails(batch);
                syncTvCredits(batch);
                syncTvImages(batch);
                syncTvVideos(batch);
                syncWatchTvProviders(batch);
            }
        }

        log.info("All content data synchronization completed!");
    }

    // Helper methods
    private void updateContentWithDetails(ContentDetailResponse detail, String type) {
        ContentId contentId = new ContentId(detail.getId(), type);

        contentRepository.findById(contentId).ifPresent(content -> {
            content.setRuntime(detail.getRuntime());
            contentRepository.save(content);
        });
    }

    private void saveCredits(CreditsResponse credits, String type) {
        ContentId contentId = new ContentId(credits.getId(), type);

        Content content = contentRepository.findById(contentId).orElse(null);
        if (content == null) return;

        // 기존 데이터 삭제
        castRepository.deleteByContent_id(contentId);
        crewRepository.deleteByContent_Id(contentId);

        // Cast 저장
        List<Cast> casts = credits.getCast().stream()
                .limit(20) // 주요 출연진만
                .map(castDto -> {
                    // Actor 조회 또는 생성
                    Actor actor = actorRepository.findById(castDto.getId())
                            .orElseGet(() -> {
                                Actor newActor = new Actor();
                                newActor.setId(castDto.getId());
                                newActor.setName(castDto.getName());
                                newActor.setProfilePath(castDto.getProfilePath());
                                newActor.setGender(castDto.getGender());
                                return actorRepository.save(newActor);
                            });

                    Cast cast = new Cast();
                    cast.setActor(actor);
                    cast.setContent(content);
                    // 캐릭터 이름 길이 체크
                    String character = castDto.getCharacter();
                    if (character != null && character.length() > 1000) {
                        character = character.substring(0, 997) + "...";
                    }
                    cast.setCharacter(character);
                    cast.setOrder(castDto.getOrder());
                    return cast;
                })
                .collect(Collectors.toList());
        castRepository.saveAll(casts);

        // Crew 저장 (감독, 제작자 등 주요 인물만)
        List<Crew> crews = credits.getCrew().stream()
                .filter(crew -> Arrays.asList("Director", "Producer", "Writer", "Screenplay",
                                "Executive Producer", "Director of Photography", "Editor", "Original Music Composer")
                        .contains(crew.getJob()))
                .map(crewDto -> {
                    // CrewMember 조회 또는 생성
                    CrewMember crewMember = crewMemberRepository.findById(crewDto.getId())
                            .orElseGet(() -> {
                                CrewMember newCrewMember = new CrewMember();
                                newCrewMember.setId(crewDto.getId());
                                newCrewMember.setName(crewDto.getName());
                                newCrewMember.setProfilePath(crewDto.getProfilePath());
                                newCrewMember.setGender(crewDto.getGender());
                                newCrewMember.setKnownForDepartment(crewDto.getDepartment());
                                return crewMemberRepository.save(newCrewMember);
                            });

                    Crew crew = new Crew();
                    crew.setCrewMember(crewMember);
                    crew.setContent(content);
                    crew.setJob(crewDto.getJob());
                    crew.setDepartment(crewDto.getDepartment());
                    return crew;
                })
                .collect(Collectors.toList());
        crewRepository.saveAll(crews);
    }

    private void saveImages(ImagesResponse images, String type) {
        ContentId contentId = new ContentId(images.getId(), type);

        Content content = contentRepository.findById(contentId).orElse(null);
        if (content == null) return;

        imageRepository.deleteByContent_Id(contentId);

        List<ContentImage> contentImages = new ArrayList<>();

        // Posters (상위 5개만)
        images.getPosters().stream().limit(5).forEach(img -> {
            ContentImage contentImage = new ContentImage();
            contentImage.setContent(content);
            contentImage.setFilePath(img.getFilePath());
            contentImage.setType(ImageType.POSTER);
            contentImage.setWidth(img.getWidth());
            contentImage.setHeight(img.getHeight());
            contentImage.setAspectRatio(img.getAspectRatio());
            contentImage.setVoteAverage(img.getVoteAverage());
            contentImage.setIso6391(img.getIso6391());
            contentImages.add(contentImage);
        });

        // Backdrops (상위 5개만)
        images.getBackdrops().stream().limit(5).forEach(img -> {
            ContentImage contentImage = new ContentImage();
            contentImage.setContent(content);
            contentImage.setFilePath(img.getFilePath());
            contentImage.setType(ImageType.BACKDROP);
            contentImage.setWidth(img.getWidth());
            contentImage.setHeight(img.getHeight());
            contentImage.setAspectRatio(img.getAspectRatio());
            contentImage.setVoteAverage(img.getVoteAverage());
            contentImage.setIso6391(img.getIso6391());
            contentImages.add(contentImage);
        });

        imageRepository.saveAll(contentImages);
    }

    private void saveVideos(VideosResponse videos, String type) {
        ContentId contentId = new ContentId(videos.getId(), type);

        Content content = contentRepository.findById(contentId).orElse(null);
        if (content == null) return;

        videoRepository.deleteByContent_Id(contentId);

        List<ContentVideo> contentVideos = videos.getResults().stream()
                .filter(v -> "YouTube".equals(v.getSite()))
                .limit(10) // 상위 10개만
                .map(video -> {
                    ContentVideo contentVideo = new ContentVideo();
                    contentVideo.setId(video.getId());
                    contentVideo.setContent(content);
                    contentVideo.setName(video.getName());
                    contentVideo.setKey(video.getKey());
                    contentVideo.setSite(video.getSite());
                    contentVideo.setSize(video.getSize());
                    contentVideo.setType(video.getType());
                    contentVideo.setOfficial(video.getOfficial());
                    contentVideo.setPublishedAt(video.getPublishedAt());
                    return contentVideo;
                })
                .collect(Collectors.toList());

        videoRepository.saveAll(contentVideos);
    }

    private void saveWatchProviders(WatchProvidersResponse providers, String type) {
        ContentId contentId = new ContentId(providers.getId(), type);

        Content content = contentRepository.findById(contentId).orElse(null);
        if (content == null) return;

        watchProviderRepository.deleteByContent_Id(contentId);

        List<WatchProvider> watchProviders = new ArrayList<>();

        // 한국 제공자
        if (providers.getResults() != null && providers.getResults().getKr() != null) {
            WatchProviderCountry kr = providers.getResults().getKr();

            // 스트리밍
            if (kr.getFlatrate() != null) {
                kr.getFlatrate().forEach(p -> {
                    WatchProvider provider = createWatchProvider(content, p, ProviderType.STREAM, "KR");
                    watchProviders.add(provider);
                });
            }

            // 대여
            if (kr.getRent() != null) {
                kr.getRent().forEach(p -> {
                    WatchProvider provider = createWatchProvider(content, p, ProviderType.RENT, "KR");
                    watchProviders.add(provider);
                });
            }

            // 구매
            if (kr.getBuy() != null) {
                kr.getBuy().forEach(p -> {
                    WatchProvider provider = createWatchProvider(content, p, ProviderType.BUY, "KR");
                    watchProviders.add(provider);
                });
            }
        }

        watchProviderRepository.saveAll(watchProviders);
    }

    @Transactional
    public void syncActorDetails(List<Long> actorIds) {
        log.info("Starting actor details sync for {} actors", actorIds.size());

        Flux.fromIterable(actorIds)
                .concatMap(actorId -> {
                    log.info("Fetching details for actor ID: {}", actorId);
                    return tmdbTvApiClient.getPersonDetail(actorId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching details for actor {}: {}", actorId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(detail -> {
                    updateActorWithDetails(detail);
                    log.info("Updated actor: {} with detailed info", detail.getName());
                })
                .blockLast();
    }

    @Transactional
    public void syncCrewMemberDetails(List<Long> crewMemberIds) {
        log.info("Starting crew member details sync for {} crew members", crewMemberIds.size());

        Flux.fromIterable(crewMemberIds)
                .concatMap(crewMemberId -> {
                    log.info("Fetching details for crew member ID: {}", crewMemberId);
                    return tmdbTvApiClient.getPersonDetail(crewMemberId)
                            .delayElement(Duration.ofMillis(250))
                            .onErrorResume(error -> {
                                log.error("Error fetching details for crew member {}: {}", crewMemberId, error.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnNext(detail -> {
                    updateCrewMemberWithDetails(detail);
                    log.info("Updated crew member: {} with detailed info", detail.getName());
                })
                .blockLast();
    }

    private void updateActorWithDetails(PersonDetailResponse detail) {
        actorRepository.findById(detail.getId()).ifPresent(actor -> {
            actor.setBiography(detail.getBiography());
            actor.setBirthday(detail.getBirthday());
            actor.setDeathday(detail.getDeathday());
            actor.setPlaceOfBirth(detail.getPlaceOfBirth());
            actor.setPopularity(detail.getPopularity());
            actorRepository.save(actor);
        });
    }

    private void updateCrewMemberWithDetails(PersonDetailResponse detail) {
        crewMemberRepository.findById(detail.getId()).ifPresent(crewMember -> {
            crewMember.setBiography(detail.getBiography());
            crewMember.setBirthday(detail.getBirthday());
            crewMember.setDeathday(detail.getDeathday());
            crewMember.setPlaceOfBirth(detail.getPlaceOfBirth());
            crewMemberRepository.save(crewMember);
        });
    }

    private WatchProvider createWatchProvider(Content content, WatchProviderInfo info, ProviderType type, String country) {
        WatchProvider provider = new WatchProvider();
        provider.setContent(content);
        provider.setProviderId(info.getProviderId());
        provider.setProviderName(info.getProviderName());
        provider.setLogoPath(info.getLogoPath());
        provider.setType(type);
        provider.setDisplayPriority(info.getDisplayPriority());
        provider.setCountry(country);
        return provider;
    }

    private Content convertToMovieEntity(ContentResponse dto) {
        Content content = new Content();
        ContentId contentId = new ContentId(dto.getId(), "movie");
        content.setId(contentId);
        content.setTitle(dto.getTitle());
        content.setOverview(dto.getOverview());
        content.setReleaseDate(dto.getReleaseDate());
        content.setRatingCount(0L);
        content.setRatingAverage(BigDecimal.valueOf(0));
        content.setPosterPath(dto.getPosterPath());
        content.setBackdropPath(dto.getBackdropPath());

        if (dto.getGenreIds() != null) {
            content.setGenreIds(new HashSet<>(dto.getGenreIds()));
        }

        return content;
    }
    private Content convertToTvEntity(ContentResponse dto) {
        Content content = new Content();
        ContentId contentId = new ContentId(dto.getId(), "tv");
        content.setId(contentId);
        content.setTitle(dto.getName());
        content.setOverview(dto.getOverview());
        content.setReleaseDate(dto.getReleaseDate());
        content.setRatingCount(0L);
        content.setRatingAverage(BigDecimal.valueOf(0));
        content.setPosterPath(dto.getPosterPath());
        content.setBackdropPath(dto.getBackdropPath());

        if (dto.getGenreIds() != null) {
            content.setGenreIds(new HashSet<>(dto.getGenreIds()));
        }

        return content;
    }

    private Genre convertToGenreEntity(GenreResponse dto) {
        Genre genre = new Genre();
        genre.setId(dto.getId());
        genre.setName(dto.getName());
        return genre;
    }
}
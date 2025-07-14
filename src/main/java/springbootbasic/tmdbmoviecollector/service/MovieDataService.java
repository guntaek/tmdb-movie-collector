//package springbootbasic.tmdbmoviecollector.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import springbootbasic.tmdbmoviecollector.client.TmdbMovieApiClient;
//import springbootbasic.tmdbmoviecollector.dto.*;
//import springbootbasic.tmdbmoviecollector.entity.*;
//import springbootbasic.tmdbmoviecollector.repository.*;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class MovieDataService {
//
//    private final TmdbMovieApiClient tmdbMovieApiClient;
//    private final MovieRepository movieRepository;
//    private final GenreRepository genreRepository;
//    private final CastRepository castRepository;
//    private final CrewRepository crewRepository;
//    private final ContentImageRepository imageRepository;
//    private final ContentVideoRepository videoRepository;
//    private final WatchProviderRepository watchProviderRepository;
//
//    @Transactional
//    public void syncGenres() {
//        log.info("Starting genre synchronization...");
//
//        tmdbMovieApiClient.getGenres()
//                .doOnNext(response -> log.info("Fetched {} genres", response.getGenres().size()))
//                .flatMapMany(response -> Flux.fromIterable(response.getGenres()))
//                .map(this::convertToGenreEntity)
//                .collectList()
//                .doOnNext(genres -> {
//                    genreRepository.saveAll(genres);
//                    log.info("Saved {} genres to database", genres.size());
//                })
//                .block();
//    }
//
//    @Transactional
//    public void syncPopularMovies(int maxPages) {
//        log.info("Starting popular movies synchronization for {} pages...", maxPages);
//        syncMovies(tmdbMovieApiClient::getPopularMovies, maxPages, "popular");
//    }
//
//    @Transactional
//    public void syncTopRatedMovies(int maxPages) {
//        log.info("Starting top-rated movies synchronization for {} pages...", maxPages);
//        syncMovies(tmdbMovieApiClient::getTopRatedMovies, maxPages, "top-rated");
//    }
//
//    @Transactional
//    public void syncUpcomingMovies(int maxPages) {
//        log.info("Starting upcoming movies synchronization for {} pages...", maxPages);
//        syncMovies(tmdbMovieApiClient::getUpcomingMovies, maxPages, "upcoming");
//    }
//
//    private void syncMovies(java.util.function.Function<Integer, Mono<ContentPageResponse>> apiCall,
//                            int maxPages, String movieType) {
//        List<Movie> allMovies = new ArrayList<>();
//
//        Flux.range(1, maxPages)
//                .concatMap(page -> {
//                    log.info("Fetching {} movies page {}/{}", movieType, page, maxPages);
//                    return apiCall.apply(page)
//                            .delayElement(Duration.ofMillis(250)); // API rate limiting
//                })
//                .doOnNext(response -> {
//                    log.info("Received {} movies from page {}",
//                            response.getResults().size(), response.getPage());
//                })
//                .flatMapIterable(ContentPageResponse::getResults)
//                .map(this::convertToMovieEntity)
//                .buffer(100) // Batch processing
//                .doOnNext(movies -> {
//                    movieRepository.saveAll(movies);
//                    allMovies.addAll(movies);
//                    log.info("Saved batch of {} movies, total: {}", movies.size(), allMovies.size());
//                })
//                .doOnComplete(() -> log.info("Completed {} movies sync. Total movies saved: {}",
//                        movieType, allMovies.size()))
//                .doOnError(error -> log.error("Error during {} movies sync: {}", movieType, error.getMessage()))
//                .blockLast();
//    }
//
//    // 영화 상세 정보 동기화
//    @Transactional
//    public void syncMovieDetails(List<Long> movieIds) {
//        log.info("Starting movie details sync for {} movies", movieIds.size());
//
//        Flux.fromIterable(movieIds)
//                .concatMap(movieId -> {
//                    log.info("Fetching details for movie ID: {}", movieId);
//                    return tmdbMovieApiClient.getMovieDetail(movieId)
//                            .delayElement(Duration.ofMillis(250))
//                            .onErrorResume(error -> {
//                                log.error("Error fetching details for movie {}: {}", movieId, error.getMessage());
//                                return Mono.empty();
//                            });
//                })
//                .doOnNext(detail -> {
//                    updateMovieWithDetails(detail);
//                    log.info("Updated movie: {} with detailed info", detail.getTitle());
//                })
//                .blockLast();
//    }
//
//    // 출연진/제작진 정보 동기화
//    @Transactional
//    public void syncMovieCredits(List<Long> movieIds) {
//        log.info("Starting credits sync for {} movies", movieIds.size());
//
//        Flux.fromIterable(movieIds)
//                .concatMap(movieId -> {
//                    log.info("Fetching credits for movie ID: {}", movieId);
//                    return tmdbMovieApiClient.getMovieCredits(movieId)
//                            .delayElement(Duration.ofMillis(250))
//                            .onErrorResume(error -> {
//                                log.error("Error fetching credits for movie {}: {}", movieId, error.getMessage());
//                                return Mono.empty();
//                            });
//                })
//                .doOnNext(credits -> {
//                    saveCredits(credits);
//                    log.info("Saved credits for movie ID: {}", credits.getId());
//                })
//                .blockLast();
//    }
//
//    // 이미지 정보 동기화
//    @Transactional
//    public void syncMovieImages(List<Long> movieIds) {
//        log.info("Starting images sync for {} movies", movieIds.size());
//
//        Flux.fromIterable(movieIds)
//                .concatMap(movieId -> {
//                    return tmdbMovieApiClient.getMovieImages(movieId)
//                            .delayElement(Duration.ofMillis(250))
//                            .onErrorResume(error -> {
//                                log.error("Error fetching images for movie {}: {}", movieId, error.getMessage());
//                                return Mono.empty();
//                            });
//                })
//                .doOnNext(images -> {
//                    saveImages(images);
//                    log.info("Saved images for movie ID: {}", images.getId());
//                })
//                .blockLast();
//    }
//
//    // 비디오 정보 동기화
//    @Transactional
//    public void syncMovieVideos(List<Long> movieIds) {
//        log.info("Starting videos sync for {} movies", movieIds.size());
//
//        Flux.fromIterable(movieIds)
//                .concatMap(movieId -> {
//                    return tmdbMovieApiClient.getMovieVideos(movieId)
//                            .delayElement(Duration.ofMillis(250))
//                            .onErrorResume(error -> {
//                                log.error("Error fetching videos for movie {}: {}", movieId, error.getMessage());
//                                return Mono.empty();
//                            });
//                })
//                .doOnNext(videos -> {
//                    saveVideos(videos);
//                    log.info("Saved videos for movie ID: {}", videos.getId());
//                })
//                .blockLast();
//    }
//
//    // 스트리밍 제공자 정보 동기화
//    @Transactional
//    public void syncWatchProviders(List<Long> movieIds) {
//        log.info("Starting watch providers sync for {} movies", movieIds.size());
//
//        Flux.fromIterable(movieIds)
//                .concatMap(movieId -> {
//                    return tmdbMovieApiClient.getMovieWatchProviders(movieId)
//                            .delayElement(Duration.ofMillis(250))
//                            .onErrorResume(error -> {
//                                log.error("Error fetching watch providers for movie {}: {}", movieId, error.getMessage());
//                                return Mono.empty();
//                            });
//                })
//                .doOnNext(providers -> {
//                    saveWatchProviders(providers);
//                    log.info("Saved watch providers for movie ID: {}", providers.getId());
//                })
//                .blockLast();
//    }
//
//    // 전체 데이터 동기화 (기본 + 상세 정보)
//    @Transactional
//    public void syncAllMovieData(int pagesPerCategory, boolean includeDetails) {
//        // 1. 장르 동기화
//        syncGenres();
//
//        // 2. 영화 기본 데이터 동기화
//        syncPopularMovies(pagesPerCategory);
//        syncTopRatedMovies(pagesPerCategory);
//        syncUpcomingMovies(pagesPerCategory);
//
//        if (includeDetails) {
//            // 3. 저장된 모든 영화 ID 가져오기
//            List<Long> allMovieIds = movieRepository.findAllMovieIds();
//            log.info("Found {} movies in database. Starting detailed sync...", allMovieIds.size());
//
//            // 4. 상세 정보 동기화 (배치로 처리)
//            int batchSize = 100;
//            for (int i = 0; i < allMovieIds.size(); i += batchSize) {
//                List<Long> batch = allMovieIds.subList(i, Math.min(i + batchSize, allMovieIds.size()));
//                log.info("Processing batch {}/{}", (i/batchSize) + 1, (allMovieIds.size()/batchSize) + 1);
//
//                syncMovieDetails(batch);
//                syncMovieCredits(batch);
//                syncMovieImages(batch);
//                syncMovieVideos(batch);
//                syncWatchProviders(batch);
//            }
//        }
//
//        log.info("All movie data synchronization completed!");
//    }
//
//    // Helper methods
//    private void updateMovieWithDetails(MovieDetailResponse detail) {
//        movieRepository.findById(detail.getId()).ifPresent(movie -> {
//            movie.setBudget(detail.getBudget());
//            movie.setRevenue(detail.getRevenue());
//            movie.setRuntime(detail.getRuntime());
//            movie.setStatus(detail.getStatus());
//            movie.setTagline(detail.getTagline());
//            movie.setHomepage(detail.getHomepage());
//            movie.setImdbId(detail.getImdbId());
//            movieRepository.save(movie);
//        });
//    }
//
//    private void saveCredits(CreditsResponse credits) {
//        Movie movie = movieRepository.findById(credits.getId()).orElse(null);
//        if (movie == null) return;
//
//        // 기존 데이터 삭제
//        castRepository.deleteByMovieId(credits.getId());
//        crewRepository.deleteByMovieId(credits.getId());
//
//        // Cast 저장
//        List<Cast> casts = credits.getCast().stream()
//                .limit(20) // 주요 출연진만
//                .map(castDto -> {
//                    Cast cast = new Cast();
//                    cast.setPersonId(castDto.getId());
//                    cast.setMovie(movie);
//                    cast.setName(castDto.getName());
//                    cast.setCharacter(castDto.getCharacter());
//                    cast.setOrder(castDto.getOrder());
//                    cast.setProfilePath(castDto.getProfilePath());
//                    return cast;
//                })
//                .collect(Collectors.toList());
//        castRepository.saveAll(casts);
//
//        // Crew 저장 (감독, 제작자 등 주요 인물만)
//        List<Crew> crews = credits.getCrew().stream()
//                .filter(crew -> Arrays.asList("Director", "Producer", "Writer", "Screenplay")
//                        .contains(crew.getJob()))
//                .map(crewDto -> {
//                    Crew crew = new Crew();
//                    crew.setPersonId(crewDto.getId());
//                    crew.setMovie(movie);
//                    crew.setName(crewDto.getName());
//                    crew.setJob(crewDto.getJob());
//                    crew.setDepartment(crewDto.getDepartment());
//                    crew.setProfilePath(crewDto.getProfilePath());
//                    return crew;
//                })
//                .collect(Collectors.toList());
//        crewRepository.saveAll(crews);
//    }
//
//    private void saveImages(ImagesResponse images) {
//        Movie movie = movieRepository.findById(images.getId()).orElse(null);
//        if (movie == null) return;
//
//        imageRepository.deleteByMovieId(images.getId());
//
//        List<MovieImage> movieImages = new ArrayList<>();
//
//        // Posters (상위 5개만)
//        images.getPosters().stream().limit(5).forEach(img -> {
//            MovieImage movieImage = new MovieImage();
//            movieImage.setMovie(movie);
//            movieImage.setFilePath(img.getFilePath());
//            movieImage.setType(ImageType.POSTER);
//            movieImage.setWidth(img.getWidth());
//            movieImage.setHeight(img.getHeight());
//            movieImage.setAspectRatio(img.getAspectRatio());
//            movieImage.setVoteAverage(img.getVoteAverage());
//            movieImage.setIso6391(img.getIso6391());
//            movieImages.add(movieImage);
//        });
//
//        // Backdrops (상위 5개만)
//        images.getBackdrops().stream().limit(5).forEach(img -> {
//            MovieImage movieImage = new MovieImage();
//            movieImage.setMovie(movie);
//            movieImage.setFilePath(img.getFilePath());
//            movieImage.setType(ImageType.BACKDROP);
//            movieImage.setWidth(img.getWidth());
//            movieImage.setHeight(img.getHeight());
//            movieImage.setAspectRatio(img.getAspectRatio());
//            movieImage.setVoteAverage(img.getVoteAverage());
//            movieImage.setIso6391(img.getIso6391());
//            movieImages.add(movieImage);
//        });
//
//        imageRepository.saveAll(movieImages);
//    }
//
//    private void saveVideos(VideosResponse videos) {
//        Movie movie = movieRepository.findById(videos.getId()).orElse(null);
//        if (movie == null) return;
//
//        videoRepository.deleteByMovieId(videos.getId());
//
//        List<MovieVideo> movieVideos = videos.getResults().stream()
//                .filter(v -> "YouTube".equals(v.getSite()))
//                .limit(10) // 상위 10개만
//                .map(video -> {
//                    MovieVideo movieVideo = new MovieVideo();
//                    movieVideo.setId(video.getId());
//                    movieVideo.setMovie(movie);
//                    movieVideo.setName(video.getName());
//                    movieVideo.setKey(video.getKey());
//                    movieVideo.setSite(video.getSite());
//                    movieVideo.setSize(video.getSize());
//                    movieVideo.setType(video.getType());
//                    movieVideo.setOfficial(video.getOfficial());
//                    movieVideo.setPublishedAt(video.getPublishedAt());
//                    return movieVideo;
//                })
//                .collect(Collectors.toList());
//
//        videoRepository.saveAll(movieVideos);
//    }
//
//    private void saveWatchProviders(WatchProvidersResponse providers) {
//        Movie movie = movieRepository.findById(providers.getId()).orElse(null);
//        if (movie == null) return;
//
//        watchProviderRepository.deleteByMovieId(providers.getId());
//
//        List<WatchProvider> watchProviders = new ArrayList<>();
//
//        // 한국 제공자
//        if (providers.getResults() != null && providers.getResults().getKr() != null) {
//            WatchProviderCountry kr = providers.getResults().getKr();
//
//            // 스트리밍
//            if (kr.getFlatrate() != null) {
//                kr.getFlatrate().forEach(p -> {
//                    WatchProvider provider = createWatchProvider(movie, p, ProviderType.STREAM, "KR");
//                    watchProviders.add(provider);
//                });
//            }
//
//            // 대여
//            if (kr.getRent() != null) {
//                kr.getRent().forEach(p -> {
//                    WatchProvider provider = createWatchProvider(movie, p, ProviderType.RENT, "KR");
//                    watchProviders.add(provider);
//                });
//            }
//
//            // 구매
//            if (kr.getBuy() != null) {
//                kr.getBuy().forEach(p -> {
//                    WatchProvider provider = createWatchProvider(movie, p, ProviderType.BUY, "KR");
//                    watchProviders.add(provider);
//                });
//            }
//        }
//
//        watchProviderRepository.saveAll(watchProviders);
//    }
//
//    private WatchProvider createWatchProvider(Movie movie, WatchProviderInfo info, ProviderType type, String country) {
//        WatchProvider provider = new WatchProvider();
//        provider.setMovie(movie);
//        provider.setProviderId(info.getProviderId());
//        provider.setProviderName(info.getProviderName());
//        provider.setLogoPath(info.getLogoPath());
//        provider.setType(type);
//        provider.setDisplayPriority(info.getDisplayPriority());
//        provider.setCountry(country);
//        return provider;
//    }
//
//    private Movie convertToMovieEntity(ContentResponse dto) {
//        Movie movie = new Movie();
//        movie.setId(dto.getId());
//        movie.setTitle(dto.getTitle());
//        movie.setOriginalTitle(dto.getOriginalTitle());
//        movie.setOverview(dto.getOverview());
//        movie.setReleaseDate(dto.getReleaseDate());
//        movie.setPopularity(dto.getPopularity());
//        movie.setVoteAverage(dto.getVoteAverage());
//        movie.setVoteCount(dto.getVoteCount());
//        movie.setPosterPath(dto.getPosterPath());
//        movie.setBackdropPath(dto.getBackdropPath());
//        movie.setOriginalLanguage(dto.getOriginalLanguage());
//        movie.setAdult(dto.getAdult());
//
//        if (dto.getGenreIds() != null) {
//            movie.setGenreIds(new HashSet<>(dto.getGenreIds()));
//        }
//
//        return movie;
//    }
//
//    private Genre convertToGenreEntity(GenreResponse dto) {
//        Genre genre = new Genre();
//        genre.setId(dto.getId());
//        genre.setName(dto.getName());
//        return genre;
//    }
//}
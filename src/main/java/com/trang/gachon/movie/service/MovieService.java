package com.trang.gachon.movie.service;

import com.trang.gachon.movie.dto.MovieRequest;
import com.trang.gachon.movie.entity.*;
import com.trang.gachon.movie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final TypeRepository typeRepository;
    private final CinemaRoomRepository cinemaRoomRepository;

    //phim đang chiếu
    public List<Movie> getNowShowingMovies() {
        LocalDate today = LocalDate.now();
        return movieRepository.findByFromDateLessThanEqualAndToDateGreaterThanEqual(today, today);
    }

    //tìm kiếm theo tên
    public List<Movie> searchByName(String kw) {
        if (kw == null || kw.isBlank()) {
            return List.of();  // trả về list rỗng nếu kw null hoặc rỗng
        }
        return movieRepository.searchByName(kw);
    }

    //lấy all (admin)
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getFeaturedMovieForHome() {
        return movieRepository.findFirstByFeaturedOnHomeTrue()
                .orElseGet(() -> getNowShowingMovies().stream().findFirst().orElse(null));
    }

    public List<Movie> getHomeNowShowingMovies() {
        return getNowShowingMovies().stream().limit(4).collect(Collectors.toList());
    }

    public Movie getById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim!")); //giải thích: nếu không tìm thấy phim với id đã cho, sẽ ném ra IllegalArgumentException với thông báo "Không tìm thấy phim!"
    }


    //thêm phim (admin)
    @Transactional
    public Movie addMovie(MovieRequest req, MultipartFile largeImageFile, MultipartFile smallImageFile, MultipartFile bannerImageFile) throws IOException {
        if (req.getFromDate().isAfter(req.getToDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu chiếu không được sau ngày kết thúc");
        }

        Movie movie = buildMovieFromRequest(req, new Movie());
        if (largeImageFile != null && !largeImageFile.isEmpty())
            movie.setLargeImage(saveImage(largeImageFile));
        if (smallImageFile != null && !smallImageFile.isEmpty())
            movie.setSmallImage(saveImage(smallImageFile));
        if (bannerImageFile != null && !bannerImageFile.isEmpty())
            movie.setBannerImage(saveImage(bannerImageFile));

        Movie savedMovie = movieRepository.save(movie);
        syncFeaturedMovie(savedMovie);
        return savedMovie;
    }

     //sửa phim (admin)
    @Transactional
    public void updateMovie(Long id, MovieRequest req, MultipartFile largeImageFile, MultipartFile smallImageFile, MultipartFile bannerImageFile) throws IOException {
        if (req.getFromDate().isAfter(req.getToDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu chiếu không được sau ngày kết thúc");
        }

        Movie movie = getById(id); //lấy phim từ DB, nếu không tìm thấy sẽ ném ra IllegalArgumentException
        buildMovieFromRequest(req, movie); //cập nhật thông tin phim từ request

        if (largeImageFile != null && !largeImageFile.isEmpty())
            movie.setLargeImage(saveImage(largeImageFile)); //lưu ảnh mới nếu có
        if (smallImageFile != null && !smallImageFile.isEmpty())
            movie.setSmallImage(saveImage(smallImageFile)); //lưu ảnh mới nếu có
        if (bannerImageFile != null && !bannerImageFile.isEmpty())
            movie.setBannerImage(saveImage(bannerImageFile)); //banner riêng cho home

        movieRepository.save(movie); //lưu lại vào DB
        syncFeaturedMovie(movie);
    }

    //xóa phim (admin)
     @Transactional
     public void deleteMovie(Long id) {

         movieRepository.deleteById(id); //giải thích  : xóa phim khỏi cơ sở dữ liệu dựa trên id đã cho. Nếu không tìm thấy phim với id đó, sẽ ném ra EmptyResultDataAccessException.
    }

    //conver entity -> dto (dùng cho form edit)

    public MovieRequest toRequest(Movie movie) {
        return MovieRequest.builder()
                .movieNameEnglish(movie.getMovieNameEnglish())
                .movieNameVn(movie.getMovieNameVn())
                .actor(movie.getActor())
                .director(movie.getDirector())
                .movieProductionCompany(movie.getMovieProductionCompany())
                .duration(movie.getDuration())
                .version(movie.getVersion())
                .fromDate(movie.getFromDate())
                .toDate(movie.getToDate())
                .trailer(movie.getTrailer())
                .reviewLink(movie.getReviewLink())
                .content(movie.getContent())
                .bannerImageUrl(movie.getBannerImage())
                .featuredOnHome(movie.isFeaturedOnHome())
                .typeIds(movie.getTypes() != null ? movie.getTypes().stream().map(Type::getTypeId).collect(Collectors.toList()) : null) //nếu danh sách thể loại của phim không null, chuyển đổi từng thể loại thành id của nó và thu thập thành một list, ngược lại trả về null
                .build();
    }


    //helper : map request -> entity : dùng cho cả add và edit, nếu edit thì truyền vào movie đã có sẵn để cập nhật, nếu add thì truyền vào movie mới tạo.
    private Movie buildMovieFromRequest(MovieRequest req,Movie movie) {
        List<Type> types = typeRepository.findAllById(req.getTypeIds());

        movie.setMovieNameEnglish(req.getMovieNameEnglish());
        movie.setMovieNameVn(req.getMovieNameVn());
        movie.setActor(req.getActor());
        movie.setDirector(req.getDirector());
        movie.setMovieProductionCompany(req.getMovieProductionCompany());
        movie.setDuration(req.getDuration());
        movie.setVersion(req.getVersion());
        movie.setTrailer(req.getTrailer());
        movie.setReviewLink(req.getReviewLink());
        movie.setContent(req.getContent());
        movie.setTypes(types);
        movie.setFeaturedOnHome(req.isFeaturedOnHome());

        if (StringUtils.hasText(req.getBannerImageUrl())) {
            movie.setBannerImage(req.getBannerImageUrl().trim());
        }

        movie.setFromDate(req.getFromDate());
        movie.setToDate(req.getToDate());

        return movie;
        
    }

    // ============================================================
    // Helper: lưu ảnh vào uploads/
    // ============================================================
    private String saveImage(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadDir  = Paths.get("uploads");

        //nếu thư mục uploads chưa tồn tại thì tạo mới
        if (!Files.exists(uploadDir))
            Files.createDirectories(uploadDir);

        //lưu file vào thư mục uploads, nếu đã tồn tại file cùng tên thì ghi đè
        Files.copy(file.getInputStream(),
                uploadDir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    // Home chỉ nên có một phim nổi bật để hero luôn rõ ràng.
    private void syncFeaturedMovie(Movie movie) {
        if (!movie.isFeaturedOnHome()) {
            return;
        }

        List<Movie> featuredMovies = movieRepository.findAll().stream()
                .filter(Movie::isFeaturedOnHome)
                .filter(savedMovie -> !savedMovie.getMovieId().equals(movie.getMovieId()))
                .collect(Collectors.toList());

        for (Movie featuredMovie : featuredMovies) {
            featuredMovie.setFeaturedOnHome(false);
        }
        movieRepository.saveAll(featuredMovies);
    }
}

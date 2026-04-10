package ch.bfh.ddwm.dssbackend.tours;

import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import ch.bfh.ddwm.dssbackend.tours.dto.TourDTO;
import ch.bfh.ddwm.dssbackend.tours.dto.TourOverviewDTO;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // TODO move to central config
@RequestMapping("/api/tours")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    // TODO review/verify whole endpoint
    @GetMapping
    public PageResponse<TourOverviewDTO> getTours(@PageableDefault(size = 4) Pageable pageable) {
        return tourService.getTours(pageable);
    }

    @GetMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<String> getToursCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition
                        .attachment()
                        .filename("tours.csv")
                        .build()
                        .toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(tourService.getToursCsv());
    }

    // TODO review/verify whole endpoint
    @GetMapping("/{tourId}")
    public TourDTO getTourById(@PathVariable long tourId) {
        return tourService.getTourById(tourId);
    }
}

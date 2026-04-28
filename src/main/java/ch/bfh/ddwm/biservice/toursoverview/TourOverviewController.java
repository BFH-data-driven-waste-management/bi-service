package ch.bfh.ddwm.biservice.toursoverview;

import ch.bfh.ddwm.biservice.common.api.PageResponse;
import ch.bfh.ddwm.biservice.toursoverview.dto.TourOverviewResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/tours/touroverview")
public class TourOverviewController {

    private final TourOverviewService tourOverviewService;

    public TourOverviewController(TourOverviewService tourOverviewService) {
        this.tourOverviewService = tourOverviewService;
    }

    @GetMapping
    public PageResponse<TourOverviewResponse> getTours(@PageableDefault(size = 4) Pageable pageable) {
        return tourOverviewService.getTours(pageable);
    }

    @GetMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<byte[]> getToursCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition
                        .attachment()
                        .filename("tours.csv")
                        .build()
                        .toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(("\uFEFF" + tourOverviewService.getToursCsv()).getBytes(StandardCharsets.UTF_8));
    }
}

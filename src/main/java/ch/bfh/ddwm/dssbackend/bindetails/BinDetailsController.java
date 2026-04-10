package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.dto.BinDetailsResponse;
import ch.bfh.ddwm.dssbackend.bindetails.dto.BinVisitHistoryResponse;
import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // TODO move to central config
@RequestMapping("/api/bins/bindetails")
public class BinDetailsController {

    private final BinDetailsService service;

    public BinDetailsController(BinDetailsService service) {
        this.service = service;
    }

    @GetMapping("/{binId}")
    public BinDetailsResponse getBinDetails(@PathVariable long binId) {
        return service.getBinDetails(binId);
    }

    @GetMapping("/{binId}/visits")
    public PageResponse<BinVisitHistoryResponse> getBinVisits(
            @PathVariable long binId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return service.getBinVisits(binId, pageable);
    }

    @GetMapping(value = "/{binId}/visits/csv", produces = "text/csv")
    public ResponseEntity<String> getBinVisitsCsv(@PathVariable long binId) {
        String filename = "bin-" + binId + "-visits.csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition
                        .attachment()
                        .filename(filename)
                        .build()
                        .toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(service.getBinVisitsCsv(binId));
    }
}

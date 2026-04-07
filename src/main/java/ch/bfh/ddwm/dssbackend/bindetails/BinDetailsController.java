package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.dto.BinDetailsResponse;
import ch.bfh.ddwm.dssbackend.bindetails.dto.BinVisitHistoryResponse;
import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

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
}

package ch.bfh.ddwm.dssbackend.bins;

import ch.bfh.ddwm.dssbackend.bins.dto.BinResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bins")
public class BinsController {

    private final BinsService service;

    public BinsController(BinsService service) {
        this.service = service;
    }

    @GetMapping
    public List<BinResponse> getAllBins() {
        return service.getAllBins();
    }
}
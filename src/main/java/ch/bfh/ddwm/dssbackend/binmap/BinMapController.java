package ch.bfh.ddwm.dssbackend.binmap;

import ch.bfh.ddwm.dssbackend.binmap.dto.BinMapResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bins/binmap")
public class BinMapController {

    private final BinMapService service;

    public BinMapController(BinMapService service) {
        this.service = service;
    }

    @GetMapping
    public List<BinMapResponse> getBinMap() {
        return service.getBinMap();
    }
}

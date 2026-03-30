package ch.bfh.ddwm.dssbackend.bins;

import ch.bfh.ddwm.dssbackend.bins.dto.BinResponse;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/by-coordinates/2056")
    public BinResponse getBinByCoordinates2056(
            @RequestParam java.math.BigDecimal coordX,
            @RequestParam java.math.BigDecimal coordY
    ) {
        return service.getBinByCoordinates2056(coordX, coordY);
    }

    @GetMapping("/by-coordinates/4326")
    public BinResponse getBinByCoordinates4326(
            @RequestParam java.math.BigDecimal coordX,
            @RequestParam java.math.BigDecimal coordY
    ) {
        return service.getBinByCoordinates4326(coordX, coordY);
    }

    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    @ExceptionHandler(BinNotFoundException.class)
    public void handleBinNotFound() {
        // Exception translated to HTTP 404
    }
}
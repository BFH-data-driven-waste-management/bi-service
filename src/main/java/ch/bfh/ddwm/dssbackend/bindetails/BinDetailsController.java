package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.dto.BinDetailsResponse;
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
}

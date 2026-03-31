package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.dto.BinDetailsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bins/bindetails")
public class BinDetailsController {

    private final BinDetailsService service;

    public BinDetailsController(BinDetailsService service) {
        this.service = service;
    }

    @GetMapping("/{binKey}")
    public BinDetailsResponse getBinDetails(@PathVariable long binKey) {
        return service.getBinDetails(binKey);
    }
}

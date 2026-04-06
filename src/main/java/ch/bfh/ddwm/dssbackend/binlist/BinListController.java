package ch.bfh.ddwm.dssbackend.binlist;

import ch.bfh.ddwm.dssbackend.binlist.dto.BinListResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // TODO move to central config
@RequestMapping("/api/bins/binlist")
public class BinListController {

    private final BinListService service;

    public BinListController(BinListService service) {
        this.service = service;
    }

    @GetMapping
    public List<BinListResponse> getBinList() {
        return service.getBinList();
    }
}

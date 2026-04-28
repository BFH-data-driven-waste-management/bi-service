package ch.bfh.ddwm.biservice.binlist;

import ch.bfh.ddwm.biservice.binlist.dto.BinListResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
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

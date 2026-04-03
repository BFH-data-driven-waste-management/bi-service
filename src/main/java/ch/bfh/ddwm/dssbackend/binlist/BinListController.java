package ch.bfh.ddwm.dssbackend.binlist;

import ch.bfh.ddwm.dssbackend.binlist.dto.BinListResponse;
import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bins/binlist")
public class BinListController {

    private final BinListService service;

    public BinListController(BinListService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<BinListResponse> getBinList(@PageableDefault(size = 20) Pageable pageable) {
        return service.getBinList(pageable);
    }
}

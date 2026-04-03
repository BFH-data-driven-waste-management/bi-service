package ch.bfh.ddwm.dssbackend.binlist;

import ch.bfh.ddwm.dssbackend.binlist.dto.BinListResponse;
import ch.bfh.ddwm.dssbackend.binlist.model.BinListItem;
import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static ch.bfh.ddwm.dssbackend.common.DateKeyHelper.toDateKey;

@Service
public class BinListService {

    private final BinListRepository repository;

    public BinListService(BinListRepository repository) {
        this.repository = repository;
    }

    public PageResponse<BinListResponse> getBinList(Pageable pageable) {
        int normalizedPage = Math.max(pageable.getPageNumber(), 0);
        int normalizedSize = Math.max(pageable.getPageSize(), 1);
        int todayDateKey = toDateKey(java.time.LocalDate.now());

        PageResponse<BinListItem> page =
                repository.findBinListByDateKey(todayDateKey, normalizedPage, normalizedSize);

        return new PageResponse<>(
                page.content().stream()
                        .map(bin -> new BinListResponse(
                                bin.binId(),
                                bin.type(),
                                bin.isActive(),
                                bin.avgWeeklyVisits90d(),
                                bin.lowFillVisitRatio90d(),
                                bin.overfullVisitRatio90d(),
                                bin.coordX2056(),
                                bin.coordY2056()
                        ))
                        .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }
}

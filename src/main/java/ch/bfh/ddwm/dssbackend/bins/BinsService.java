package ch.bfh.ddwm.dssbackend.bins;

import ch.bfh.ddwm.dssbackend.bins.dto.BinResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BinsService {

    private final BinsRepository repository;

    public BinsService(BinsRepository repository) {
        this.repository = repository;
    }

    public List<BinResponse> getAllBins() {
        return repository.findAllBins();
    }
}
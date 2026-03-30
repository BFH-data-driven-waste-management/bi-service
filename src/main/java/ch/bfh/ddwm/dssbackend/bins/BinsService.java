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

    public BinResponse getBinByCoordinates2056(java.math.BigDecimal coordX2056, java.math.BigDecimal coordY2056) {
        BinResponse bin = repository.findByCoordinates2056(coordX2056, coordY2056);
        if (bin == null) {
            throw new BinNotFoundException("No bin found for 2056 coordinates");
        }
        return bin;
    }

    public BinResponse getBinByCoordinates4326(java.math.BigDecimal coordX4326, java.math.BigDecimal coordY4326) {
        BinResponse bin = repository.findByCoordinates4326(coordX4326, coordY4326);
        if (bin == null) {
            throw new BinNotFoundException("No bin found for 4326 coordinates");
        }
        return bin;
    }
}
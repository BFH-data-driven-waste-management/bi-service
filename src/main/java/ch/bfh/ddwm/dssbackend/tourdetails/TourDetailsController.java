package ch.bfh.ddwm.dssbackend.tourdetails;

import ch.bfh.ddwm.dssbackend.tourdetails.dto.TourResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tours/tourdetails")
public class TourDetailsController {

    private final TourDetailsService tourDetailsService;

    public TourDetailsController(TourDetailsService tourDetailsService) {
        this.tourDetailsService = tourDetailsService;
    }

    @GetMapping("/{tourId}")
    public TourResponse getTourById(@PathVariable long tourId) {
        return tourDetailsService.getTourById(tourId);
    }
}

package ch.bfh.ddwm.dssbackend.bindetails;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "ch.bfh.ddwm.dssbackend.bindetails")
public class BinExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(BinNotFoundException.class)
    public void handleBinNotFound() {
        // Exception translated to HTTP 404
    }
}

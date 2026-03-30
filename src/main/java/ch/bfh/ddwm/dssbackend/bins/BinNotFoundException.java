package ch.bfh.ddwm.dssbackend.bins;

public class BinNotFoundException extends RuntimeException {
    public BinNotFoundException(String message) {
        super(message);
    }
}
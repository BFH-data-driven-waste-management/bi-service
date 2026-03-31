package ch.bfh.ddwm.dssbackend.bindetails;

public class BinNotFoundException extends RuntimeException {
    public BinNotFoundException(String message) {
        super(message);
    }
}
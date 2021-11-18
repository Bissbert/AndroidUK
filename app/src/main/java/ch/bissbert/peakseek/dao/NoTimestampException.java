package ch.bissbert.peakseek.dao;

public class NoTimestampException extends Exception {
    public NoTimestampException(String table){
        super("There is no timestamp for "+table);
    }
}

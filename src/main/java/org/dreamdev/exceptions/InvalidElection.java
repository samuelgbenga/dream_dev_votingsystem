package org.dreamdev.exceptions;

public class InvalidElection extends RuntimeException {
    public InvalidElection(String message) {
        super(message);
    }
}
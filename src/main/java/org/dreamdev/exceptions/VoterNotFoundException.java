package org.dreamdev.exceptions;

public class VoterNotFoundException extends RuntimeException {
    public VoterNotFoundException(String message) {
        super(message);
    }
}
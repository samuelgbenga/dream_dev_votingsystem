package org.dreamdev.exceptions;

public class InvalidIdFormat extends RuntimeException {
    public InvalidIdFormat(String message) {
        super(message);
    }
}
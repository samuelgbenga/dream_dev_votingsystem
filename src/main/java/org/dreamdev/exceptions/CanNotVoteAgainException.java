package org.dreamdev.exceptions;

public class CanNotVoteAgainException extends RuntimeException {
    public CanNotVoteAgainException(String message) {
        super(message);
    }
}
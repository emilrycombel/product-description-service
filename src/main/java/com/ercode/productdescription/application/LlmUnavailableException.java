package com.ercode.productdescription.application;

/** Raised when the LLM gateway cannot be reached or returns an error. Mapped to HTTP 502. */
public class LlmUnavailableException extends RuntimeException {

    public LlmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

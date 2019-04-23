package com.redhat.demo.optaplanner.upstream;

/**
 * The purpose of this Exception class is to signalize when Infinispan server could not be reached.
 */
public class InfinispanException extends RuntimeException {

    public InfinispanException(String message) {
        super(message);
    }

    public InfinispanException(String message, Throwable cause) {
        super(message, cause);
    }
}

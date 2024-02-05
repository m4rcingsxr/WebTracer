package com.webtracer;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code ApiException} is a custom unchecked exception that serves as a general-purpose exception
 * for handling various errors in the application, particularly in the context of API-related operations.
 * This exception extends {@link RuntimeException}, allowing it to be used without requiring mandatory
 * exception handling (i.e., no need to declare it in a method's {@code throws} clause).
 */
@Getter
@Setter
public final class ApiException extends RuntimeException {

    public ApiException() {
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

}

/**
 *
 */
package org.jboss.pnc.cleaner.builds;

public class CleanerException extends Exception {

    public CleanerException() {
    }

    public CleanerException(String message) {
        super(message);
    }

    public CleanerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CleanerException(Throwable cause) {
        super(cause);
    }

    public CleanerException(String message, Throwable cause, Object... params) {
        this(String.format(message, params), cause);
    }

}

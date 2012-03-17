package com.robmayhew.qds;

/**
 * The QDSJSONException is thrown by the JSON.org classes when things are amiss.
 * @author JSON.org
 * @version 2010-12-24
 */
public class QDSJSONException extends Exception {
    private static final long serialVersionUID = 0;
    private Throwable cause;

    /**
     * Constructs a QDSJSONException with an explanatory message.
     * @param message Detail about the reason for the exception.
     */
    public QDSJSONException(String message) {
        super(message);
    }

    public QDSJSONException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }
}

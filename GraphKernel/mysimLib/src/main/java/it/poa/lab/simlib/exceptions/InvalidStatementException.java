package it.poa.lab.simlib.exceptions;

/**
 * Exception thrown by {@link it.poa.lab.simlib.input.StatementFileReader} if the input file contains
 * not valid statements
 *
 * @author Giorgio Basile
 * @since 1.0
 */
public class InvalidStatementException extends RuntimeException{
    public InvalidStatementException() { super(); }
    public InvalidStatementException(String message) { super(message); }
    public InvalidStatementException(String message, Throwable cause) { super(message, cause); }
    public InvalidStatementException(Throwable cause) { super(cause); }
}

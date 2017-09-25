package Exceptions;

/**
 * Created by scapista on 8/31/17.
 * +
 * + Priority:
 * + 1. build failing
 * + 2. function not working
 * + 3. nice to have but would optimize
 * + 4. nice to have no benefit
 * + 5. future state
 */

public class NoMoreApiCallsException extends Exception{
    public NoMoreApiCallsException() {}

    public NoMoreApiCallsException( String message) {
        super(message);
    }
}

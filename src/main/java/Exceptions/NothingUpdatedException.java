package Exceptions;

/**
 * Created by scapista on 9/3/17.
 * +
 * + Priority:
 * + 1. build failing
 * + 2. function not working
 * + 3. nice to have but would optimize
 * + 4. nice to have no benefit
 * + 5. future state
 */

public class NothingUpdatedException extends Exception {
    public NothingUpdatedException() {}

    public NothingUpdatedException( String message) {
        super(message);
    }
}

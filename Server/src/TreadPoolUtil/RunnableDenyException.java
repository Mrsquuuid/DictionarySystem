package TreadPoolUtil;

/**
 * @author Yuzhe You (No.1159774)
 * Deny exception.
 */

public class RunnableDenyException extends RuntimeException {
    public RunnableDenyException(String message){
        super(message);
    }
}
package net.devintia.commons.async;

/**
 * A simple method invocation. If you care about the result use a {@link Callback}.
 *
 * @author Digot
 * @version 1.0
 * @param <T> The type of the argument and the result of the Callback
 */
public interface Delegate<T> {

    /**
     * Invokes the delegate with the given argument
     * @param arg The argument to pass
     */
    void invoke( T arg );

}

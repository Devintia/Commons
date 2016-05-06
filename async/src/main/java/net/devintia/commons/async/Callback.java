package net.devintia.commons.async;

/**
 * A simple method invocation that returns a result. If you don't care about the result use a {@link Delegate}.
 *
 * @author Digot
 * @version 1.0
 * @param <T> The type of the argument and the result of the Callback
 */
public interface Callback<T> {

    /**
     * Invokes the callback with the given argument and return the result of it
     * @param arg The argument to pass
     * @return the result of the Callback
     */
    T invoke( T arg );

}

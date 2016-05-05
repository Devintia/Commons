package net.devintia.commons.async;

/**
 * A generic listener, that is called when a {@link Future} was either resolved or failed.
 *
 * @author Digot
 * @version 1.0
 */
public interface FutureListener<T> {

    void onResolved( T arg );

    void onFailed( Throwable cause );

}

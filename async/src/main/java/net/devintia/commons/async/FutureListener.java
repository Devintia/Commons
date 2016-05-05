package net.devintia.commons.async;

/**
 * A generic listener, that is called when a {@link Future} was either resolved or failed.
 *
 * @author Digot
 * @version 1.0
 * @param <T> The type of the result of the corresponding {@link Future}
 */
public interface FutureListener<T> {

    /**
     * Called when the task of the corresponding {@link Future } was resolved
     *
     * @param arg The result object
     */
    void onResolved( T arg );

    /**
     * Called when the execution of task of the corresponding {@link Future } failed
     *
     * @param cause The failure cause
     */
    void onFailed( Throwable cause );

}

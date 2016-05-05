package net.devintia.commons.async;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A basic future class that supports listeners.
 *
 * @author Digot
 * @version 1.0
 * @param <T> The type of the result
 */
public class Future<T> {

    private FutureState state;
    private T result;
    private Throwable failCause;
    private Set<FutureListener<T>> registeredListeners;

    /**
     * Creates a new Future instance
     */
    public Future() {
        this.state = FutureState.PENDING;
        this.registeredListeners = new HashSet<>();
    }

    /**
     * Creates a Future that immediately resolves with a null result
     * @param <T> The type of the result
     * @return the created Future
     */
    public static <T> Future<T> createNull() {
        //Create a future and resolve it immediately
        Future<T> future = new Future<>();
        future.resolve( null );
        return future;
    }



    /**
     * Adds a {@link FutureListener} that listens to the result of the Future. If the Future is already finished when the listener is added, it gets called instead
     *
     * @param listener The listener that should be added
     */
    public synchronized void addListener( FutureListener<T> listener ) {
        switch ( this.state ) {
            case PENDING: this.registeredListeners.add( listener );
                break;
            case RESOLVED: listener.onResolved( this.result );
                break;
            case FAILED: listener.onFailed( this.failCause );
                break;
            default: break;
        }
    }

    /**
     * Finishes the Future and signals, that the execution of the corresponding task has failed
     *
     * @param cause The reason why the Future failed
     */
    public synchronized void fail( Throwable cause ) {
        //Set the fail cause and switch state
        this.failCause = cause;
        this.state = FutureState.FAILED;

        //Notify threads and call the listeners
        this.finish();
    }

    /**
     * Finishes the Future and signals, that the execution of the corresponding task has succeeded
     *
     * @param result The result of the task
     */
    public synchronized void resolve( T result ) {
        //Set the result and switch state
        this.result = result;
        this.state = FutureState.RESOLVED;

        //Notify threads and call the listeners
        this.finish();
    }

    /**
     * Tries to retrieve the result of the Future without any time out. If the Future is pending, it waits until the Future is done.
     *
     * @return the result of the Future
     * @throws InterruptedException When the method call was interrupted while waiting for the result
     * @throws ExecutionException When the future failed to resolve
     * @throws TimeoutException Won't throw in this overload
     */
    public synchronized T get() throws InterruptedException, ExecutionException, TimeoutException {
        return this.get( 0L, TimeUnit.MILLISECONDS );
    }

    /**
     * Tries to retrieve the result of the Future with time out. If the Future is pending, it waits until the Future is done.
     *
     * @param timeout The amount of time in the given TimeUnit the thread should wait for the result
     * @param timeUnit The TimeUnit of the timeout value
     * @return the result of the Future
     * @throws InterruptedException When the method call was interrupted while waiting for the result
     * @throws ExecutionException When the future failed to resolve
     * @throws TimeoutException When the result takes too long to resolve
     */
    public synchronized T get( long timeout, TimeUnit timeUnit ) throws ExecutionException, InterruptedException, TimeoutException {
        switch ( this.state ) {
            case RESOLVED: return this.result;
            case FAILED:   throw new ExecutionException( "Future failed to resolve", this.failCause );
            case PENDING:
            default:
                if( timeout == 0 ) {
                    //No timeout, wait until we have the result and then return it
                    this.wait();
                    return this.get();
                }
                else {
                    //Wait for the result within the given timeout time and return it.
                    this.wait( timeUnit.toMillis( timeout ) );
                    if( !this.isDone() ) throw new TimeoutException( "Future took too long!" );
                    return this.get();
                }

        }
    }

    /**
     * @return Whether the Future is done or not
     */
    public synchronized boolean isDone() {
        return this.state != FutureState.PENDING;
    }

    /**
     * @return Whether the execution of the Future was successful or not
     */
    public synchronized boolean isSuccess() {
        return this.state == FutureState.RESOLVED;
    }

    /**
     * @return Whether the execution of the Future was a failure or not
     */
    public synchronized boolean isFailed() {
        return this.state == FutureState.FAILED;
    }

    private synchronized void finish() {
        //Make sure that the future is done
        if( !this.isDone() ) {
            throw new IllegalStateException( "Still pending" );
        }

        //Call all listeners
        for ( FutureListener<T> registeredCallback : this.registeredListeners ) {
            switch ( this.state ) {
                case RESOLVED: registeredCallback.onResolved( this.result );
                    break;
                case FAILED: registeredCallback.onFailed( this.failCause );
                    break;
                default: break;
            }
        }

        //Notify waiting threads
        this.notifyAll();
    }

    private enum FutureState {
        PENDING, RESOLVED, FAILED
    }
}

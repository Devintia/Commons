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


    public static <T> Future<T> createNull() {
        //Create a future and resolve it immediately
        Future<T> future = new Future<>();
        future.resolve( null );
        return future;
    }

    public Future() {
        this.state = FutureState.PENDING;
        this.registeredListeners = new HashSet<>();
    }

    public synchronized void addListener( FutureListener<T> listener ) {
        this.registeredListeners.add( listener );
    }

    public synchronized void fail( Throwable cause ) {
        //Set the fail cause and switch state
        this.failCause = cause;
        this.state = FutureState.FAILED;

        //Notify threads and call the listeners
        this.finish();
    }

    public synchronized void resolve( T result ) {
        //Set the result and switch state
        this.result = result;
        this.state = FutureState.RESOLVED;

        //Notify threads and call the listeners
        this.finish();
    }

    public synchronized void finish() {
        //Make sure that the future is done
        if( !this.isDone() ) {
            throw new IllegalStateException( "Still pending" );
        }

        //Call all listeners
        for ( FutureListener<T> registeredCallback : this.registeredListeners ) {
            System.out.println( this.state );
            switch ( this.state ) {
                case RESOLVED: registeredCallback.onResolved( this.result );
                    break;
                case FAILED: registeredCallback.onFailed( this.failCause );
                    break;
            }
        }

        //Notify waiting threads
        this.notifyAll();
    }


    public synchronized T get() throws InterruptedException, ExecutionException, TimeoutException {
        return this.get( 0, TimeUnit.MILLISECONDS );
    }

    public synchronized T get( int timeout, TimeUnit timeUnit ) throws ExecutionException, InterruptedException, TimeoutException {
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

    public synchronized boolean isDone() {
        return this.state != FutureState.PENDING;
    }

    public synchronized boolean isSuccess() {
        return this.state == FutureState.RESOLVED;
    }

    public synchronized boolean isFailed() {
        return this.state == FutureState.FAILED;
    }

    private enum FutureState {
        PENDING, RESOLVED, FAILED
    }
}

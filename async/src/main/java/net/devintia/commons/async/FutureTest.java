package net.devintia.commons.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Digot
 * @version 1.0
 */
public class FutureTest {

    public static void main( String[] args ) {
        Future<String> testFuture = FutureTest.call( new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep( 1000 );
                throw new NullPointerException( "null" );
                //return "hi";
            }
        } );

        testFuture.addListener( new FutureListener<String>() {
            @Override
            public void onResolved( String arg ) {
                System.out.println( "Resolved " + arg.getClass().getName() );
            }

            @Override
            public void onFailed( Throwable cause ) {
                System.out.println( "Failed: "+ cause.getClass().getName() );
            }
        } );

        try( Timer timer = new Timer( "Test1" )) {
            try {
                System.out.println( testFuture.get( 1, TimeUnit.SECONDS ) );
            } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
                e.printStackTrace();
            }

        }

        try( Timer timer = new Timer( "Test2" )) {
            try {
                System.out.println( testFuture.get() );
            } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
                e.printStackTrace();
            }
        }
    }

    public static <T> Future<T> call( Callable<T> runnable ) {
        Future<T> future = new Future<>();

        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    T result = runnable.call();
                    future.resolve( result );
                }
                catch ( Exception e ) {
                    future.fail( e );
                }
            }
        } ).start();

        return future;
    }

}

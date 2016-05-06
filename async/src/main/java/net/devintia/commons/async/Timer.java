package net.devintia.commons.async;

/**
 * Used to measure how long the execution of a code block takes
 *
 * @author Digot
 * @version 1.0
 */
public class Timer implements AutoCloseable {

    private final long startMs;
    private final String topic;

    /**
     * The default constructor for the timer
     *
     * @param topic The topic of the measure
     */
    public Timer ( String topic ) {
        this.topic = topic;
        this.startMs = System.currentTimeMillis();
    }

    @Override
    public void close ( ) {
        System.out.println( this.topic + " took " + ( System.currentTimeMillis() - this.startMs ) + "ms" );
    }
}
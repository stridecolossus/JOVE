package org.sarge.jove.task;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.util.ToString;

/**
 * Background thread pool.
 * @author Sarge
 */
public class BackgroundTaskQueue implements TaskQueue {
	private static final Logger LOG = Logger.getLogger( BackgroundTaskQueue.class.getName() );

	private static final UncaughtExceptionHandler HANDLER = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException( Thread t, Throwable e ) {
			LOG.log( Level.SEVERE, "Error in thread " + t.getName(), e );
		}
	};

	private final ThreadPoolExecutor executor;

	private int max;

	/**
	 * Constructor.
	 */
	public BackgroundTaskQueue() {
		// Create prioritised queue
		final BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();

		// Create factory for threads with exception capture
		final ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread( Runnable r ) {
				final Thread t = new Thread( r );
				t.setDaemon( true );
				t.setUncaughtExceptionHandler( HANDLER );
				return t;
			}
		};

		// Create executor
		// TODO - allow configuration
		executor = new ThreadPoolExecutor( 3, 6, 1L, TimeUnit.MINUTES, queue, factory );
	}

	/**
	 * @return Underlying executor service
	 */
	ThreadPoolExecutor getExecutor() {
		return executor;
	}

	@Override
	public int getSize() {
		return executor.getQueue().size();
	}

	@Override
	public int getMaximumSize() {
		return max;
	}

	/**
	 * Queues a background task for execution.
	 * @param task Background task
	 */
	@Override
	public void add( DefaultTask task ) {
		// Queue task
		executor.execute( task );

		// Update stats
		final int size = getSize();
		if( size > max ) max = size;
	}

	@Override
	public void cancel( DefaultTask task ) {
		switch( task.getState() ) {
		case QUEUED:
			executor.remove( task );
			// TODO - log warning if not removed?
			break;

		case RUNNING:
			// TODO - how to interrupt thread?
			break;
		}
	}

	/**
	 * Terminates this queue immediately.
	 */
	public void shutdown() {
		executor.shutdownNow();
	}

	/**
	 * Terminates this queue.
	 * @param timeout Graceful shutdown period (ms)
	 */
	public void shutdown( long timeout ) {
		executor.shutdown();
		try {
			executor.awaitTermination( timeout, TimeUnit.MILLISECONDS );
		}
		catch( InterruptedException e ) {
			LOG.log( Level.WARNING, "Interrupting awaiting graceful shutdown", e );
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}

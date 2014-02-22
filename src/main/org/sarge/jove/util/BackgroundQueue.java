package org.sarge.jove.util;

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
public class BackgroundQueue {
	private static final Logger LOG = Logger.getLogger( BackgroundQueue.class.getName() );

	/**
	 * Wrapper for prioritised tasks.
	 */
	private static class Entry implements Runnable, Comparable<Entry> {
		private Runnable task;
		private int priority;

		@Override
		public int compareTo( Entry other ) {
			return this.priority - other.priority;
		}

		@Override
		public void run() {
			task.run();
		}
	}

	private static final UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException( Thread t, Throwable e ) {
			LOG.log( Level.SEVERE, "Error in thread " + t.getName(), e );
		}
	};

	private final ThreadPoolExecutor executor;

	/**
	 * Constructor.
	 */
	public BackgroundQueue() {
		// Create prioritised queue
		final BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();

		// Create factory for threads with exception capture
		final ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread( Runnable r ) {
				final Thread t = new Thread( r ); // TODO - custom names?
				t.setDaemon( true );
				t.setUncaughtExceptionHandler( handler );
				return t;
			}
		};

		// Create executor
		executor = new ThreadPoolExecutor( 3, 6, 1L, TimeUnit.MINUTES, queue, factory );
	}

	/**
	 * @return Underlying executor service
	 */
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

	/**
	 * Queues a prioritised task.
	 * @param task			Task
	 * @param priority		Priority (lower is more important)
	 */
	public void add( Runnable task, int priority ) {
		// Wrap as prioritised task
		final Entry entry = new Entry();
		entry.task = task;
		entry.priority = priority;

		// Add to queue
		executor.execute( entry );
	}

	/**
	 * Queues an un-prioritised task.
	 * @param task Task
	 */
	public void add( Runnable task ) {
		executor.execute( task );
	}

	/**
	 * Terminates this queue.
	 * @param timeout Graceful shutdown period or <tt>null</tt> to force terminate
	 */
	public void shutdown( Long timeout ) {
		if( timeout == null ) {
			// Kill service
			executor.shutdownNow();
		}
		else {
			// Attempt graceful shutdown and wait for current tasks
			executor.shutdown();
			try {
				executor.awaitTermination( timeout, TimeUnit.MILLISECONDS );
			}
			catch( InterruptedException e ) {
				LOG.log( Level.WARNING, "Interrupting awaiting graceful shutdown", e );
			}
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}

package org.sarge.jove.task;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import org.sarge.jove.scene.RenderContext;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Prioritised queue of tasks to be executed on the rendering thread.
 * @author Sarge
 */
public class RenderThreadTaskQueue implements TaskQueue {
	/**
	 * Throttle limit on the number of tasks to execute per iteration.
	 */
	public static enum LimitPolicy {
		/**
		 * No limit (execute all pending task).
		 */
		NONE,

		/**
		 * Limited by number of tasks per iteration.
		 */
		NUMBER,

		/**
		 * Limited by cumulative execution time.
		 */
		DURATION
	}

	private final Queue<DefaultTask> queue = new PriorityBlockingQueue<>();

	// Config
	private LimitPolicy policy = LimitPolicy.NONE;
	private long limit = 50;

	// Stats
	private int max;

	// Limit stats
	private long start;
	private int count;

	/**
	 * Sets the policy for throttling the number of tasks to be executed per iteration.
	 * @param policy Limit policy (default is {@link NONE})
	 */
	public void setLimitPolicy( LimitPolicy policy ) {
		Check.notNull( policy );
		this.policy = policy;
	}

	/**
	 * Sets the limit to be applied to the throttling policy.
	 * @param limit Limit (depends on the current policy, default is <b>50</b>)
	 */
	public void setLimitParameter( long limit ) {
		Check.oneOrMore( limit );
		this.limit = limit;
	}

	@Override
	public int getSize() {
		return queue.size();
	}

	@Override
	public int getMaximumSize() {
		return max;
	}

	@Override
	public void add( DefaultTask task ) {
		// Queue task
		queue.add( task );

		// Update stats
		final int size = queue.size();
		if( size > max ) max = size;
	}

	@Override
	public void cancel( DefaultTask task ) {
		queue.remove( task );
	}

	/**
	 * Removes all tasks from the queue.
	 */
	public void clear() {
		queue.clear();
	}

	/**
	 * Executes pending tasks on the queue.
	 * @param ctx Rendering context
	 * TODO - do we need context here?
	 * @return Actual number of tasks executed
	 */
	public int execute( RenderContext ctx ) {
		// Init stats
		count = 0;
		start = System.currentTimeMillis();

		// Execute pending tasks until all done or throttle limit exceeded
		while( true ) {
			// Get next task in priority order
			final DefaultTask task = queue.poll();

			// Stop if no pending tasks remaining
			if( task == null ) break;

			// Perform task
			task.run();
			++count;

			// Check throttle limit
			if( isFinished() ) {
				break;
			}
		}

		return count;
	}

	/**
	 * Tests whether execution has finished according to the configured limiting policy.
	 */
	private boolean isFinished() {
		switch( policy ) {
		case NUMBER:
			return count >= limit;

		case DURATION:
			return ( System.currentTimeMillis() - start ) > limit;

		default:
			return false;
		}
	}

	@Override
	public String toString() {
		final ToString ts = new ToString( this );
		ts.append( "limit", limit );
		ts.append( "policy", policy );
		ts.append( "size", queue.size() );
		ts.append( "max", max );
		return ts.toString();
	}
}

package org.sarge.jove.app;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.sarge.jove.scene.RenderContext;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Prioritised queue of tasks to be executed on the rendering thread.
 * @see RenderQueueTask
 * @author Sarge
 */
public class RenderThreadQueue {
	/**
	 * Prioritised queue entry.
	 */
	private class Entry implements Comparable<Entry> {
		private RenderQueueTask task;
		private int priority;

		@Override
		public int compareTo( Entry other ) {
			return this.priority - other.priority;
		}
	}

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
		DURATION,
	}

	private final Queue<Entry> queue = new PriorityQueue<>();

	private LimitPolicy policy = LimitPolicy.NONE;
	private long limit = 50;

	/**
	 * @return Pending tasks
	 * TODO - just return the 'list'?
	 */
	public List<RenderQueueTask> getQueue() {
		final List<RenderQueueTask> list = new ArrayList<>();
		for( Entry entry : queue ) {
			list.add( entry.task );
		}
		return list;
	}

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

	/**
	 * Adds a prioritised task to the queue.
	 * @param task			Task
	 * @param priority		Priority (lower is more important)
	 */
	public synchronized void add( RenderQueueTask task, int priority ) {
		Check.notNull( task );
		final Entry entry = new Entry();
		entry.task = task;
		entry.priority = priority;
		queue.add( entry );
	}

	/**
	 * Adds a un-prioritised task to the queue (zero priority).
	 * @param task Task
	 */
	public void add( RenderQueueTask task ) {
		add( task, 0 );
	}

	/**
	 * Removes all tasks from the queue.
	 */
	public synchronized void clear() {
		queue.clear();
	}

	/**
	 * Executes pending tasks on the queue.
	 * @param ctx Rendering context
	 * @return Actual number of tasks executed
	 */
	public synchronized int execute( RenderContext ctx ) {
		// Execute pending tasks until all done or throttle limit exceeded
		final long start = ctx.getTime();
		int count = 0;
		while( true ) {
			// Get next task in priority order
			final Entry entry = queue.poll();

			// Stop if no pending tasks remaining
			if( entry == null ) break;

			// Perform task
			entry.task.execute( ctx );
			++count;

			// Check throttle limit
			// TODO - this 'stop' flag is a bit cumbersome?
			final boolean stop;
			switch( policy ) {
			case NUMBER:
				stop = count >= limit;
				break;

			case DURATION:
				stop = ( System.currentTimeMillis() - start ) > limit;
				break;

			default:
				stop = false;
				break;
			}
			if( stop ) break;
		}

		return count;
	}

	@Override
	public String toString() {
		final ToString ts = new ToString( this );
		ts.append( "limit", limit );
		ts.append( "policy", policy );
		ts.append( "size", queue.size() );
		return ts.toString();
	}
}

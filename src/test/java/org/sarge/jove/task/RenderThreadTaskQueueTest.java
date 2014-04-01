package org.sarge.jove.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.task.RenderThreadTaskQueue.LimitPolicy;
import org.sarge.jove.task.Task.State;
import org.sarge.lib.util.Util;

public class RenderThreadTaskQueueTest {
	private RenderThreadTaskQueue queue;
	private Task task;
	private Runnable runnable;

	@Before
	public void before() {
		queue = new RenderThreadTaskQueue();
		runnable = mock( Runnable.class );
		task = new Task( runnable, 1, queue );
	}

	@Test
	public void constructor() {
		assertEquals( 0, queue.getSize() );
		assertEquals( 0, queue.getMaximumSize() );
	}

	@Test
	public void add() {
		queue.add( task );
		assertEquals( 1, queue.getSize() );
		assertEquals( 1, queue.getMaximumSize() );
	}

	@Test
	public void clear() {
		queue.add( task );
		queue.clear();
		assertEquals( 0, queue.getSize() );
		assertEquals( 1, queue.getMaximumSize() );
	}

	@Test
	public void execute() {
		// Add to queue
		queue.add( task );
		task.setState( State.QUEUED );

		// Check task executed
		final int count = queue.execute( null );
		verify( runnable ).run();
		assertEquals( 0, queue.getSize() );
		assertEquals( 1, count );
		assertEquals( 1, queue.getMaximumSize() );
	}

	@Test
	public void executePrioritised() {
		// Add low priority task
		queue.add( task );
		task.setState( State.QUEUED );

		// Add high priority task
		final Runnable high = mock( Runnable.class );
		final Task highTask = new Task( high, 2, queue );
		queue.add( highTask );
		highTask.setState( State.QUEUED );

		// Limit to one task per iteration and check higher priority task added
		queue.setLimitParameter( 1 );
		queue.setLimitPolicy( LimitPolicy.NUMBER );
		queue.execute( null );
		verify( high ).run();
		verifyZeroInteractions( runnable );
		assertEquals( 1, queue.getSize() );
		assertEquals( 2, queue.getMaximumSize() );
	}

	@Test
	public void executeNumberLimit() {
		// Configure queue to execute one task per iteration
		queue.setLimitPolicy( LimitPolicy.NUMBER );
		queue.setLimitParameter( 1 );

		// Add two tasks
		queue.add( task );
		task.setState( State.QUEUED );
		queue.add( new Task( mock( Runnable.class ), 0, queue ) );

		// Execute and check only one executed
		final int count = queue.execute( null );
		assertEquals( 1, count );
		assertEquals( 1, queue.getSize() );
		assertEquals( 2, queue.getMaximumSize() );
		verify( runnable ).run();
	}

	@Test
	public void executeTimeLimit() {
		// Throttle queue by time
		queue.setLimitPolicy( LimitPolicy.DURATION );
		queue.setLimitParameter( 1 );

		// Create an expensive task
		final Runnable delay = new Runnable() {
			@Override
			public void run() {
				Util.kip( 50 );
			}
		};
		final Task delayedTask = new Task( delay, 999, queue );
		delayedTask.setState( State.QUEUED );

		// Add another task that should miss the cut
		queue.add( delayedTask );
		queue.add( task );
		task.setState( State.QUEUED );

		// Execute and check only first task executed
		final int count = queue.execute( null );
		assertEquals( 1, count );
		assertEquals( 1, queue.getSize() );
		assertEquals( 2, queue.getMaximumSize() );
		assertEquals( State.FINISHED, delayedTask.getState() );
		assertEquals( State.QUEUED, task.getState() );
	}
}

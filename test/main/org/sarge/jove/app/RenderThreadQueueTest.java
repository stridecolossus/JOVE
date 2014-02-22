package org.sarge.jove.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderThreadQueue.LimitPolicy;
import org.sarge.jove.scene.RenderContext;
import org.sarge.lib.util.Util;

public class RenderThreadQueueTest {
	private RenderThreadQueue queue;
	private RenderQueueTask task;
	private RenderContext ctx;

	@Before
	public void before() {
		queue = new RenderThreadQueue();
		task = mock( RenderQueueTask.class );
		ctx = mock( RenderContext.class );
	}

	@Test
	public void constructor() {
		assertNotNull( queue.getQueue() );
		assertEquals( true, queue.getQueue().isEmpty() );
	}

	@Test
	public void add() {
		queue.add( task );
		assertEquals( 1, queue.getQueue().size() );
		assertEquals( task, queue.getQueue().iterator().next() );
	}

	@Test
	public void addPrioritised() {
		// Add a task with a low priority
		final RenderQueueTask first = mock( RenderQueueTask.class );
		queue.add( first, 2 );

		// Add another with higher priority
		final RenderQueueTask second = mock( RenderQueueTask.class );
		queue.add( second, 1 );

		// Check tasks ordered in queue
		assertEquals( 2, queue.getQueue().size() );
		assertEquals( second, queue.getQueue().get( 0 ) );
		assertEquals( first, queue.getQueue().get( 1 ) );
	}

	@Test
	public void clear() {
		queue.add( task );
		queue.clear();
		assertEquals( true, queue.getQueue().isEmpty() );
	}

	@Test
	public void execute() {
		queue.add( task );
		final int count = queue.execute( ctx );
		verify( task ).execute( ctx );
		assertEquals( true, queue.getQueue().isEmpty() );
		assertEquals( 1, count );
	}

	@Test
	public void executeNumberLimit() {
		// Configure queue to execute one task per iteration
		queue.setLimitPolicy( LimitPolicy.NUMBER );
		queue.setLimitParameter( 1 );

		// Add two tasks
		queue.add( task );
		queue.add( task );

		// Execute and check only one executed
		final int count = queue.execute( ctx );
		assertEquals( 1, count );
		assertEquals( 1, queue.getQueue().size() );
		verify( task, times( 1 ) ).execute( ctx );
	}

	@Test
	public void executeTimeLimit() {
		// Throttle queue by time
		queue.setLimitPolicy( LimitPolicy.DURATION );
		queue.setLimitParameter( 1 );

		// Create an expensive task
		final RenderQueueTask delay = new RenderQueueTask() {
			@Override
			public void execute( RenderContext unused )  {
				Util.kip( 50 );
			}
		};

		// Add another task that should miss the cut
		queue.add( delay );
		queue.add( task );

		// Execute and check only first task executed
		final int count = queue.execute( ctx );
		assertEquals( 1, count );
		assertEquals( 1, queue.getQueue().size() );
		verifyZeroInteractions( task );
	}
}

package org.sarge.jove.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.task.Task.State;
import org.sarge.lib.util.Util;

public class BackgroundTaskQueueTest {
	private BackgroundTaskQueue queue;
	private DefaultTask task;

	@Before
	public void before() {
		queue = new BackgroundTaskQueue();
		task = new DefaultTask( mock( RunnableTask.class ), 0 );
	}

	@After
	public void after() {
		queue.shutdown();
	}

	@Test
	public void constructor() {
		assertEquals( 0, queue.getSize() );
		assertEquals( 0, queue.getMaximumSize() );
	}

	@Test(timeout=1000)
	public void add() {
		// Queue a task
		task.setState( State.QUEUED );
		queue.add( task );

		// Wait until task executed
		while( task.getState() != State.FINISHED ) {
			Util.kip( 50 );
		}
	}
}

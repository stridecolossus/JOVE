package org.sarge.jove.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.task.Task.Listener;
import org.sarge.jove.task.Task.State;

public class DefaultTaskTest {
	private DefaultTask task;
	private RunnableTask runnable;
	private TaskQueue queue;

	@Before
	public void before() {
		runnable = mock( RunnableTask.class );
		queue = mock( TaskQueue.class );
		when( runnable.getQueue() ).thenReturn( queue );
		task = new DefaultTask( runnable, 1 );
	}

	@Test
	public void constructor() {
		assertEquals( runnable, task.getTask() );
		assertEquals( 1, task.getPriority() );
		assertEquals( State.PENDING, task.getState() );
	}

	@Test
	public void compareTo() {
		assertEquals( 0, task.compareTo( task ) );
		assertEquals( -1, task.compareTo( new DefaultTask( runnable, 0 ) ) );
		assertEquals( 1, task.compareTo( new DefaultTask( runnable, 2 ) ) );
	}

	@Test
	public void start() {
		task.start();
		assertEquals( State.QUEUED, task.getState() );
		verify( queue ).add( task );
	}

	@Test
	public void run() {
		// Attach a listener
		final Listener listener = mock( Listener.class );
		task.add( listener );

		// Execute task
		task.start();
		task.run();

		// Check underlying task executed
		assertEquals( State.FINISHED, task.getState() );
		verify( runnable ).run();

		// Check lifecycle events
		verify( listener ).notify( task, State.RUNNING );
		verify( listener ).notify( task, State.FINISHED );
	}

	@Test
	public void runFailedTask() {
		doThrow( new RuntimeException() ).when( runnable ).run();
		task.start();
		task.run();
		assertEquals( State.FAILED, task.getState() );
	}

	@Test
	public void cancel() {
		task.start();
		task.cancel();
		assertEquals( State.CANCELLED, task.getState() );
	}
}

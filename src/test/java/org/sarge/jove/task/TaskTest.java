package org.sarge.jove.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.task.Task.Listener;
import org.sarge.jove.task.Task.State;

public class TaskTest {
	private Task task;
	private Runnable runnable;
	private TaskQueue queue;

	@Before
	public void before() {
		runnable = mock( Runnable.class );
		queue = mock( TaskQueue.class );
		task = new Task( runnable, 1, queue );
	}

	private static void check( State state, State... valid ) {
		// Check valid state changes
		for( State next : valid ) {
			assertEquals( true, state.isValid( next ) );
		}

		// Check invalid state changes
		final Set<State> invalid = new HashSet<>( Arrays.asList( State.values() ) );
		invalid.removeAll( Arrays.asList( valid ) );
		for( State next : invalid ) {
			assertEquals( false, state.isValid( next ) );
		}
	}

	@Test
	public void isValid() {
		check( State.PENDING, State.QUEUED, State.CANCELLED );
		check( State.QUEUED, State.RUNNING, State.CANCELLED );
		check( State.RUNNING, State.FINISHED, State.FAILED, State.CANCELLED );
		check( State.FINISHED );
		check( State.FAILED );
		check( State.CANCELLED );
	}

	@Test
	public void constructor() {
		assertEquals( runnable, task.getRunnable() );
		assertEquals( 1, task.getPriority() );
		assertEquals( queue, task.getQueue() );
		assertEquals( State.PENDING, task.getState() );
	}

	@Test
	public void setState() {
		task.setState( State.QUEUED );
		assertEquals( State.QUEUED, task.getState() );
	}

	@Test
	public void addListener() {
		final Listener listener = mock( Listener.class );
		task.add( listener );
		task.setState( State.QUEUED );
		verify( listener ).notify( task, State.QUEUED );
	}

	@Test
	public void start() {
		task.start();
		assertEquals( State.QUEUED, task.getState() );
		verify( queue ).add( task.getRunnable() );
		verifyZeroInteractions( runnable );
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
	public void cancelPending() {
		task.cancel();
		assertEquals( State.CANCELLED, task.getState() );
	}

	@Test
	public void cancelQueued() {
		task.start();
		task.cancel();
		assertEquals( State.CANCELLED, task.getState() );
	}

	@Test
	public void cancelRunning() {
		task.start();
		task.setState( State.RUNNING );
		task.cancel();
		assertEquals( State.CANCELLED, task.getState() );
	}
}

package org.sarge.jove.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.task.Task.Listener;
import org.sarge.jove.task.Task.State;

public class TaskGroupTest {
	private TaskGroup group;
	private DefaultTask one, two, three;
	private Listener listener;

	@Before
	public void before() {
		one = mock( DefaultTask.class );
		two = mock( DefaultTask.class );
		three = mock( DefaultTask.class );
		group = new TaskGroup( Arrays.asList( new DefaultTask[]{ one, two }, new DefaultTask[]{ three } ) );
		listener = mock( Listener.class );
		group.add( listener );
	}

	@Test
	public void constructor() {
		assertEquals( State.PENDING, group.getState() );
	}

	@Test
	public void start() {
		// Start group and check first two parallel sub-tasks are started
		group.start();
		verify( one ).start();
		verify( two ).start();
		verifyZeroInteractions( three );

		// Check group state
		assertEquals( State.RUNNING, group.getState() );
		verify( listener ).notify( group, State.QUEUED );
		verify( listener ).notify( group, State.RUNNING );
		verifyNoMoreInteractions( listener );
	}

	@Test
	public void lifecycle() {
		// Complete one sub-task and check nothing else changes
		group.start();
		group.listener.notify( one, State.FINISHED );
		assertEquals( State.RUNNING, group.getState() );
		verifyZeroInteractions( three );

		// Complete other task and check next stage started
		group.listener.notify( two, State.FINISHED );
		assertEquals( State.RUNNING, group.getState() );
		verify( three ).start();

		// Complete last task and check group finished
		group.listener.notify( three, State.FINISHED );
		assertEquals( State.FINISHED, group.getState() );
	}

	@Test
	public void cancel() {
		// Start group and complete one sub-task
		group.start();
		group.listener.notify( one, State.FINISHED );

		// Cancel group
		group.cancel();
		assertEquals( State.CANCELLED, group.getState() );

		// Check completed task is not affected
		verify( one ).start();
		verifyNoMoreInteractions( one );

		// Check active task is cancelled
		verify( two ).start();
		verify( two ).cancel();
		verify( two ).setState( State.CANCELLED );
		verifyNoMoreInteractions( two );

		// Check pending tasks are cancelled
		verify( three ).setState( State.CANCELLED );
		verifyNoMoreInteractions( three );
	}

	@Test
	public void failedTask() {
		// Start group and complete one sub-task
		group.start();
		group.listener.notify( one, State.FINISHED );

		// Fail a task
		group.listener.notify( two, State.FAILED );
		assertEquals( State.FAILED, group.getState() );

		// Check completed task is unaffected
		verify( one ).start();
		verifyNoMoreInteractions( one );

		// Check active task is cancelled
		verify( two ).start();
		verify( two ).cancel();
		verify( two ).setState( State.CANCELLED );
		verifyNoMoreInteractions( two );

		// Check pending task is cancelled
		verify( three ).setState( State.CANCELLED );
		verifyNoMoreInteractions( three );
	}
}

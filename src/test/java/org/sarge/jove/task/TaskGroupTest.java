package org.sarge.jove.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.task.Task.Listener;
import org.sarge.jove.task.Task.State;

public class TaskGroupTest {
	/**
	 * Produces a property.
	 * @author chris
	 */
	@SuppressWarnings("unused")
	public static class Producer implements Runnable {
		private String property = "hello";
		private String another;
		private boolean fail;

		@Override
		public void run() {
			if( fail ) throw new RuntimeException();
		}
	}

	/**
	 * Consumes multiple properties.
	 * @author chris
	 */
	@SuppressWarnings("unused")
	public class Consumer implements Runnable {
		private String property;
		private String same;

		@Override
		public void run() {
		}
	}

	private TaskGroup parent;
	private Task child;
	private TaskQueue queue;
	private Listener listener;

	@Before
	public void before() {
		queue = mock( TaskQueue.class );
		child = new Task( new Producer(), 0, queue );
		parent = new TaskGroup( new Consumer(), 0, queue );
		listener = mock( Listener.class );
		child.add( listener );
	}

	@Test
	public void add() {
		parent.add( child );
	}

	@Test
	public void addSpecifiedProperty() {
		parent.add( child, "property" );
	}

	@Test(expected=IllegalArgumentException.class)
	public void addSelf() {
		parent.add( parent );
	}

	@Test(expected=IllegalArgumentException.class)
	public void addDuplicate() {
		parent.add( child );
		parent.add( child );
	}

	@Test(expected=IllegalArgumentException.class)
	public void addNotFound() {
		parent.add( new Task( mock( Runnable.class ), 0, queue ) );
	}

	@Test(expected=IllegalArgumentException.class)
	public void addAmbiguous() {
		@SuppressWarnings("unused")
		final Runnable ambiguous = new Runnable() {
			private String property;
			private String same;

			@Override
			public void run() {
			}
		};
		parent.add( new Task( ambiguous, 0, queue ) );
	}

	@Test(expected=IllegalArgumentException.class)
	public void addDuplicateTask() {
		parent.add( child );
		parent.add( child );
	}

	@Test(expected=IllegalArgumentException.class)
	public void addDuplicateTargetField() {
		final Runnable dup = new Runnable() {
			@SuppressWarnings("unused")
			private String property;

			@Override
			public void run() {
			}
		};
		parent.add( new Task( dup, 0, queue ) );
		parent.add( child );
	}

	@Test
	public void start() {
		// Start group and check dependency is queued
		parent.add( child );
		parent.start();
		assertEquals( State.QUEUED, child.getState() );
		verify( queue ).add( child.getRunnable() );

		// Check parent task is unaffected
		assertEquals( State.PENDING, parent.getState() );
		verify( listener ).notify( child, State.QUEUED );

		// Complete dependent task
		child.run();
		assertEquals( State.FINISHED, child.getState() );
		verify( listener ).notify( child, State.RUNNING );
		verify( listener ).notify( child, State.FINISHED );

		// Check dependency is copied to parent
		final Consumer consumer = (Consumer) parent.getRunnable();
		assertEquals( "hello", consumer.property );

		// Check parent is started
		verify( queue ).add( parent.getRunnable() );
		assertEquals( State.QUEUED, parent.getState() );

		// Complete parent
		parent.run();
		assertEquals( State.FINISHED, parent.getState() );
		verifyNoMoreInteractions( listener );
	}

	@Test(expected=IllegalArgumentException.class)
	public void startNoDependencies() {
		parent.start();
	}

	@Test
	public void startFailedChildTask() {
		// Dependent task will fail
		final Producer producer = (Producer) child.getRunnable();
		producer.fail = true;

		// Run group
		parent.add( child );
		parent.start();
		child.run();

		// Check dependent task failed and parent was cancelled
		assertEquals( State.FAILED, child.getState() );
		assertEquals( State.CANCELLED, parent.getState() );
	}

	@Test
	public void cancelNotStarted() {
		parent.add( child );
		parent.cancel();
		assertEquals( State.PENDING, child.getState() );
		assertEquals( State.CANCELLED, parent.getState() );
	}

	@Test
	public void cancelStarted() {
		parent.add( child );
		parent.start();
		parent.cancel();
		assertEquals( State.CANCELLED, child.getState() );
		assertEquals( State.CANCELLED, parent.getState() );
		verify( queue ).cancel( child.getRunnable() );
	}

	@Test
	public void cancelRunning() {
		parent.add( child );
		parent.start();
		child.run();
		parent.cancel();
		assertEquals( State.FINISHED, child.getState() );
		assertEquals( State.CANCELLED, parent.getState() );
		verify( queue ).cancel( parent.getRunnable() );
	}

	@Test(expected=RuntimeException.class)
	public void cancelAlreadyCancelled() {
		parent.add( child );
		parent.cancel();
		parent.cancel();
	}
}


package org.sarge.jove.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.task.Task.Listener;
import org.sarge.jove.task.Task.State;

public class AbstractTaskTest {
	private class MockAbstractTask extends AbstractTask {
		@Override
		public void start() {
			// Empty
		}

		@Override
		public void cancel() {
			// Empty
		}
	}

	private AbstractTask task;
	private Listener listener;

	@Before
	public void before() {
		task = new MockAbstractTask();
		listener = mock( Listener.class );
	}

	@Test
	public void setState() {
		task.setState( State.QUEUED );
		assertEquals( State.QUEUED, task.getState() );
	}

	@Test
	public void addListener() {
		task.add( listener );
		task.setState( State.QUEUED );
		verify( listener ).notify( task, State.QUEUED );
	}
}

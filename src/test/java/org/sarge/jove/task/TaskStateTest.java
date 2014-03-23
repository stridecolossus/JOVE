package org.sarge.jove.task;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.sarge.jove.task.Task.State;

public class TaskStateTest {
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
}

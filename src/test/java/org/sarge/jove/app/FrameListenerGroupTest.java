package org.sarge.jove.app;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sarge.jove.app.FrameListener;
import org.sarge.jove.app.FrameListenerGroup;
import org.sarge.jove.util.MockitoTestCase;

public class FrameListenerGroupTest extends MockitoTestCase {
	private FrameListenerGroup group;
	private @Mock FrameListener listener;

	@Before
	public void before() {
		group = new FrameListenerGroup();
	}

	@Test
	public void add() {
		group.add( listener );
		group.update( 1, 2 );
		verify( listener ).update( 1, 2 );
	}

	@Test
	public void remove() {
		group.add( listener );
		group.remove( listener );
		group.update( 1, 2 );
		verifyZeroInteractions( listener );
	}

	@Test
	public void clear() {
		group.add( listener );
		group.clear();
		group.update( 1, 2 );
		verifyZeroInteractions( listener );
	}
}

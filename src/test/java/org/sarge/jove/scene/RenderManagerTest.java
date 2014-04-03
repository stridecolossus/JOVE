package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.scene.RenderQueue.SortOrder;

public class RenderManagerTest {
	private RenderManager manager;
	private RenderQueue queue;
	private Node node;

	@Before
	public void before() {
		node = mock( Node.class );
		queue = mock( RenderQueue.class );
		manager = new RenderManager( Collections.singletonList( queue ) );
	}

	@Test
	public void getQueue() {
		assertNotNull( manager.getQueue( queue ) );
		assertEquals( true, manager.getQueue( queue ).isEmpty() );
	}

	private void add() {
		when( node.getRenderQueue() ).thenReturn( queue );
		manager.visit( node );
	}

	@Test
	public void visit() {
		add();
		assertEquals( 1, manager.getQueue( queue ).size() );
		assertEquals( node, manager.getQueue( queue ).get( 0 ) );
	}

	@Test(expected=IllegalArgumentException.class)
	public void visitUnknownQueue() {
		final RenderQueue other = mock( RenderQueue.class );
		when( node.getRenderQueue() ).thenReturn( other );
		manager.visit( node );
	}

	@Test
	public void sort() {
		final Comparator<Node> comparator = mock( Comparator.class );
		add();
		add();
		when( queue.getSortOrder() ).thenReturn( SortOrder.FRONT_TO_BACK );
		manager.sort( comparator );
		verify( comparator ).compare( node, node );
	}

	@Test
	public void render() {
		// TODO
	}

	@Test
	public void clear() {
		add();
		manager.clear();
		assertEquals( true, manager.getQueue( queue ).isEmpty() );
	}
}

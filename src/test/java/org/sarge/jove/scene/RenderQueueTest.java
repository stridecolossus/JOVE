package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.scene.RenderQueue.SortOrder;

public class RenderQueueTest {
	private Node one, two;

	@Before
	public void before() {
		one = mock( Node.class );
		two = mock( Node.class );
	}

	@Test
	public void getSortOrder() {
		assertEquals( SortOrder.FRONT_TO_BACK, RenderQueue.Default.OPAQUE.getSortOrder() );
		assertEquals( SortOrder.NONE, RenderQueue.Default.SKY.getSortOrder() );
		assertEquals( SortOrder.BACK_TO_FRONT, RenderQueue.Default.TRANSLUCENT.getSortOrder() );
		assertEquals( SortOrder.BACK_TO_FRONT, RenderQueue.Default.POST.getSortOrder() );
		assertEquals( null, RenderQueue.Default.NONE.getSortOrder() );
	}

	public void sort( SortOrder order, Node... expected ) {
		// Create a queue
		final List<Node> nodes = Arrays.asList( two, one );

		// Mock a distance comparator that always returns ONE first
		final Comparator<Node> comparator = new Comparator<Node>() {
			@Override
			public int compare( Node a, Node b ) {
				if( a == one ) {
					return -1;
				}
				else
				if( b == one ) {
					return +1;
				}
				else {
					return 0;
				}
			}
		};

		// Apply default sort and check expected order
		order.sort( nodes, comparator );
		assertEquals( expected[ 0 ], nodes.get( 0 ) );
		assertEquals( expected[ 1 ], nodes.get( 1 ) );
	}

	@Test
	public void sort() {
		sort( SortOrder.FRONT_TO_BACK, one, two );
		sort( SortOrder.BACK_TO_FRONT, two, one );
		sort( SortOrder.NONE, two, one );
	}
}

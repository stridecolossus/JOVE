package org.sarge.jove.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sarge.jove.scene.Node.Visitor;
import org.sarge.jove.scene.RenderQueue.SortOrder;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Manager for a set of {@link RenderQueue}s.
 * @author Sarge
 */
public class RenderManager implements Visitor {
	private final Map<RenderQueue, List<Node>> queues = new LinkedHashMap<>();
	private final List<Node> stack = new ArrayList<>();

	/**
	 * Constructor.
	 * @param queues Managed queues (in execution order)
	 */
	public RenderManager( List<RenderQueue> queues ) {
		Check.notEmpty( queues );
		for( RenderQueue q : queues ) {
			this.queues.put( q, new ArrayList<Node>() );
		}
	}

	/**
	 * @param q Render queue
	 * @return List of nodes in the given queue
	 */
	public List<Node> getQueue( RenderQueue q ) {
		return Collections.unmodifiableList( queues.get( q ) );
	}

	@Override
	public boolean visit( Node node ) {
		// TODO
		// - frustum culling here
		// - skip if culled

		// Lookup render queue, skip if not rendered
		final RenderQueue q = node.getRenderQueue();
		assert q != null;
		if( q == RenderQueue.Default.NONE ) return false;

		// Add to queue
		final List<Node> nodes = queues.get( node.getRenderQueue() );
		if( nodes == null ) throw new IllegalArgumentException( "Queue not registered: " + node.getRenderQueue() );
		nodes.add( node );

		// Recurse
		return true;
	}

	/**
	 * Sorts all queues.
	 * @param comparator Distance comparator for this scene
	 */
	public void sort( Comparator<Node> comparator ) {
		for( Entry<RenderQueue, List<Node>> entry : queues.entrySet() ) {
			final RenderQueue q = entry.getKey();
			final List<Node> nodes = entry.getValue();
			final SortOrder order = q.getSortOrder();
			order.sort( nodes, comparator );
		}
	}

	/**
	 * Renders all queues.
	 * @param ctx Rendering context
	 */
	public void render( RenderContext ctx ) {
		// TODO
		// - get depth-range from render-queue?
		// - or have init/reset methods to do whatever each queue requires?
		for( List<Node> list : queues.values() ) {
			for( Node node : list ) {
				render( node, ctx );
			}
		}
	}

	/**
	 * Renders the given node.
	 * @param node		Node to render
	 * @param ctx		Rendering context
	 */
	private void render( Node node, RenderContext ctx ) {
		// Build stack of nodes from root
		stack.clear();
		do {
			stack.add( node );
			node = node.getParent();
		}
		while( node != null );
		Collections.reverse( stack );

		// Apply material and render nodes
		// TODO - remember stack and only apply/remove differences to reduce state changes, would work nicely with OPAQUE_UNSORTED
		for( Node n : stack ) {
			n.apply( ctx );
		}

		// Reset materials
		Collections.reverse( stack );
		for( Node n : stack ) {
			n.reset( ctx );
		}
	}

	/**
	 * Clears all render queues.
	 */
	public void clear() {
		for( List<Node> q : queues.values() ) {
			q.clear();
		}
	}

	@Override
	public String toString() {
		final ToString str = new ToString( this );
		for( Entry<RenderQueue, List<Node>> entry : queues.entrySet() ) {
			str.append( entry.getKey() + " " + entry.getValue().size() );
		}
		return str.toString();
	}
}

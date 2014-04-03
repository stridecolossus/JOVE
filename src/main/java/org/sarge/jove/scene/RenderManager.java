package org.sarge.jove.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
	private final LinkedList<Node> path = new LinkedList<>();
	private final List<Node> prev = new ArrayList<>();

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
		final List<Node> nodes = queues.get( q );
		if( nodes == null ) throw new IllegalArgumentException( "Queue not registered: " + q );
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
		path.clear();
		Node n = node;
		while( true ) {
			// Lookup this node in the previous render
			final int idx = prev.indexOf( n );

			if( idx >= 0 ) {
				// Splice previous path and stop
				//path.addAll( prev.subList( 0, idx ) );
				// Restore at idx then render path
				break;
			}
			else {
				// Otherwise walk up to parent
				path.push( n );
				n = n.getParent();

				// Stop at root
				if( n == null ) {
					break;
				}
			}
		}

		// Store path for next render
		prev.clear();
		prev.addAll( path );

		// Render nodes
		for( Node r : path ) {
			r.apply( ctx );
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

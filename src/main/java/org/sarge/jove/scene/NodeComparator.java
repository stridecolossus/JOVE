package org.sarge.jove.scene;

import java.util.Comparator;

import org.sarge.jove.geometry.Point;
import org.sarge.lib.util.Check;

/**
 * Comparator for sorting nodes in a render stage.
 */
public class NodeComparator implements Comparator<Node> {
	private final boolean reverse;
	private final Point pos = new Point();

	private Point eye;

	/**
	 * Constructor.
	 * @param reverse		Whether sorted by furthest or nearest distances
	 * @param eye			Eye position
	 */
	public NodeComparator( boolean reverse ) {
		this.reverse = reverse;
	}

	/**
	 * Sets the eye position.
	 * @param eye Eye position
	 */
	public void setEyePosition( Point eye ) {
		Check.notNull( eye );
		this.eye = eye;
	}

	@Override
	public int compare( Node lhs, Node rhs ) {
		final float d1 = distanceTo( lhs );
		final float d2 = distanceTo( rhs );
		final float diff = d1 - d2;
		if( reverse ) {
			return (int) -diff;
		}
		else {
			return (int) diff;
		}
	}

	/**
	 * Calculates distance from eye to the given node.
	 */
	private float distanceTo( Node node ) {
		node.getWorldMatrix().getColumn( 3, pos );
		return eye.distanceSquared( pos );
	}
}

package org.sarge.jove.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Location;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.MutablePoint;
import org.sarge.jove.geometry.MutableVector;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;
import org.sarge.jove.scene.Node.Visitor;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Scene/viewport.
 * <p>
 * The scene:
 * <ul>
 * <li>encapsulates the projection and camera view matrix</li>
 * <li>defines the rendering viewport dimensions</li>
 * <li>manages the various rendering buffers</li>
 * <li>contains the <i>root</i> of the scene-graph to be rendered</li>
 * </ul>
 * @see Viewport
 * @see Camera
 * @see Projection
 * @author Sarge
 */
public class Scene {
	/**
	 * Distance comparator for picking and distance ordering.
	 */
	private final Comparator<Node> comparator = new Comparator<Node>() {
		private final MutablePoint pos = new MutablePoint();

		@Override
		public int compare( Node a, Node b ) {
			return (int) ( distance( a ) - distance( b ) );
		}

		private float distance( Node node ) {
			// TODO - should this be from bounding volume?
			node.getWorldMatrix().getColumn( 3, pos );
			return cam.getPosition().distanceSquared( pos );
		}
	};

	// Scene
	private final Camera cam = new Camera();
	private Projection projection;
	private Matrix matrix;
	private boolean dirty;
	private Node root;

	// Viewport
	private final Viewport viewport;
	private Rectangle rect;
	private Colour col = new Colour( 0.6f, 0.6f, 0.6f, 1 );
	private float near = 0.1f;
	private float far = 1000f;

	// Rendering
	private final RenderManager mgr;

	// Working
	private final MutableVector vec = new MutableVector();

	/**
	 * Constructor.
	 * @param viewport		Viewport
	 * @param rect			Viewport rectangle
	 * @param projection	Scene projection
	 * @param mgr			Render manager
	 */
	public Scene( Viewport viewport, Rectangle rect, Projection projection, RenderManager mgr ) {
		Check.notNull( viewport );
		Check.notNull( mgr );

		this.viewport = viewport;
		this.mgr = mgr;

		setRectangle( rect );
		setProjection( projection );
	}

	/**
	 * @return Camera
	 */
	public Camera getCamera() {
		return cam;
	}

	/**
	 * @return Scene viewport rectangle
	 */
	public Rectangle getRectangle() {
		return rect;
	}

	/**
	 * Resets the viewport rectangle.
	 * @param rect
	 */
	public void setRectangle( Rectangle rect ) {
		Check.notNull( rect );
		this.rect = rect;
		dirty = true;
	}

	/**
	 * Sets the root node of this scene.
	 * @param root Root node
	 */
	public void setRoot( Node root ) {
		this.root = root;
	}

	/**
	 * Sets the clear colour.
	 * @param col Clear colour or <tt>null</tt> for no frame buffer clearing
	 */
	public void setClearColour( Colour col ) {
		this.col = col;
	}

	/**
	 * Sets the near clipping plane.
	 * @param near Near clipping distance
	 */
	public void setNearPlane( float near ) {
		Check.zeroOrMore( near );
		this.near = near;
		dirty = true;
	}

	/**
	 * Sets the far clipping plane.
	 * @param far Far clipping distance
	 */
	public void setFarPlane( float far ) {
		Check.zeroOrMore( far );
		this.far = far;
		dirty = true;
	}

	/**
	 * @return Scene projection
	 */
	public Projection getProjection() {
		return projection;
	}

	/**
	 * Sets the projection for this scene.
	 * @param projection New projection
	 */
	public void setProjection( Projection projection ) {
		Check.notNull( projection );
		this.projection = projection;
		dirty = true;
	}

	/**
	 * @return Projection matrix
	 * @see #setProjection(Projection)
	 */
	public Matrix getProjectionMatrix() {
		if( dirty ) {
			matrix = projection.getMatrix( near, far, rect.getDimensions() );
			dirty = false;
		}

		return matrix;
	}

	/**
	 * @return Distance comparator for nodes in this scene
	 */
	public Comparator<Node> getDistanceComparator() {
		return comparator;
	}

	/**
	 * Renders this scene.
	 * @param ctx Render context
	 */
	public void render( RenderContext ctx ) {
		// Init viewport
		viewport.init( rect );

		// Clear buffers
		viewport.clear( col );

		// Stop if nothing to render
		if( root == null ) return;

		// Render scene
		ctx.setScene( this );
		mgr.visit( root );
		mgr.sort( comparator );
		mgr.render( ctx );
		ctx.setScene( null );
	}

	 /*
	 * TODO - add radius?
	 *
		// compute width and height of the near and far plane sections
		tang = tan(angle);
		sphereFactorY = 1.0/cos(angle);

		// compute half of the the horizontal field of view and sphereFactorX
		float anglex = atan(tang*ratio);
		sphereFactorX = 1.0/cos(anglex);
	 */

	/**
	 * Tests whether the given point is within the view frustum.
	 * @param pt Point being tested
	 * @return Whether the point is contained by the frustum
	 */
	public boolean contains( Point pt ) {
		// Calc vector from point to eye position
		vec.subtract( pt, cam.getPosition() );

		// Test distance by projecting onto vector
		final float z = vec.dot( cam.getDirection() );
		if( ( z < near ) || ( z > far ) ) return false;

		// Test against frustum half-height at this distance
		final Dimensions dim = rect.getDimensions();
		final float y = vec.dot( cam.getUpDirection() );
		final float h = z * projection.getHeight( dim );
		if( ( y < -h ) || ( y > h ) ) return false;

		// Test against frustum width
		final float x = vec.dot( cam.getRightAxis() );
		final float w = h * ( dim.getWidth() / (float) dim.getHeight() );
		if( ( x < -w ) || ( x > w ) ) return false;

		// Point is within frustum
		return true;
	}

	/**
	 * Un-projects the given <b>window</b> location to its point in this viewport.
	 * @param loc		Screen coordinates
	 * @param pt		Resultant point in the scene
	 * TODO - test
	 */
	public void unproject( Location loc, MutablePoint pt ) {
		// Convert to viewport coordinates (inverting Y coordinate)
		final int vx = loc.getX() - rect.getX();
		final int vy = rect.getY() - loc.getY();

		// Convert to NDC (also inverts Y)
		final float nx = ( 2 * vx ) / (float) rect.getWidth() - 1;
		final float ny = ( 2 * vy ) / (float) rect.getHeight() - 1;

		// Un-project into scene
		pt.set( nx, ny, -1 );
		matrix.multiply( cam.getViewMatrix() ).invert().multiply( pt );
	}

	/**
	 * Picks from this scene.
	 * @param ray Picking ray
	 * @return Intersected objects ordered by distance from camera (nearest first)
	 */
	public List<NodeGroup> pick( Ray ray ) {
		/*
		 * TODO - this is a right load of shite
		 *
		// Calc pick coords
		final float x = loc.getX() / rect.getWidth();
		final float y = loc.getY() / rect.getHeight();

		// Project into the scene
		final Vector v = matrix.multiply( Vector.Z_AXIS.invert() ); // TODO - calc once, or make Z_AXIS mutable-vector?  dangerous?
		final Ray ray = new Ray( new Point( x, y, 0 ), v );
		*/

		// Walk scene-graph and find intersected objects
		final List<NodeGroup> nodes = new ArrayList<>();
		final Visitor visitor = new Visitor() {
			@Override
			public boolean visit( Node node ) {
				/*
				if( e instanceof NodeGroup ) {
					// Check bounding volume intersection
					final NodeGroup node = (NodeGroup) e;
					final BoundingVolume vol = node.getBoundingVolume();
					if( vol.intersects( ray ) ) {
						// Intersects, recurse to children
						nodes.add( node );
						return true;
					}
					else {
						// Does not intersect, stop recursion
						return false;
					}
				}
				else {
					// Not pickable
					return false;
				}
				*/

				// TODO - add BV to node interface, def is NULL or delegates to parent?

				return false;
			}
		};
		root.accept( visitor );

		// Order by distance
		Collections.sort( nodes, comparator );

		return nodes;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}

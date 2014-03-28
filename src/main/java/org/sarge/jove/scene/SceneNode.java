package org.sarge.jove.scene;

import org.sarge.jove.geometry.BoundingVolume;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Transform;
import org.sarge.jove.material.Material;
import org.sarge.lib.util.Check;

/**
 * Node specifying scene transformation and materials.
 * <p>
 * Comprises:
 * <ul>
 * <li>transformations</li>
 * <li>bounding volume</li>
 * <li>material state</li>
 * <li>state flags</li>
 * </ul>
 * @author Sarge
 */
public class SceneNode extends NodeGroup {
	private Transform local = Matrix.IDENTITY;
	private BoundingVolume vol = BoundingVolume.NULL;
	private Material mat;

	private transient Matrix world;

	/**
	 * Constructor with a specific render queue.
	 * @param name Node name
	 */
	public SceneNode( String name, RenderQueue queue ) {
		super( name, queue );
	}

	/**
	 * Convenience constructor for an opaque node.
	 * @param name Node name
	 */
	public SceneNode( String name ) {
		this( name, RenderQueue.Default.OPAQUE );
	}

	/**
	 * @return Bounding volume of this node or <tt>null</tt> if none
	 */
	public BoundingVolume getBoundingVolume() {
		return vol;
	}

	/**
	 * Sets the bounding volume of this node.
	 * @param vol Bounding volume or <tt>null</tt> if none
	 */
	public void setBoundingVolume( BoundingVolume vol ) {
		Check.notNull( vol );
		this.vol = vol;
		propagate( Flag.BOUNDING_VOLUME );
	}

	/**
	 * @return Local transform
	 */
	public Transform getTransform() {
		return local;
	}

	/**
	 * Sets the local transform.
	 * @param local Local transform
	 */
	public void setTransform( Transform local ) {
		Check.notNull( local );
		this.local = local;
		set( Flag.TRANSFORM );
	}

	/**
	 * @return World transform matrix
	 */
	@Override
	public Matrix getWorldMatrix() {
		final NodeGroup parent = getParent();
		if( parent == null ) {
			// Local is equivalent to world transform if at root of the scene-graph
			clear( Flag.TRANSFORM );
			return local.toMatrix();
		}
		else {
			// Update world transform as required
			if( this.isDirtyTransform() || parent.isDirtyTransform() ) {
				world = parent.getWorldMatrix().multiply( local.toMatrix() );
				clear( Flag.TRANSFORM );
			}
			return world;
		}
	}

	@Override
	protected boolean isDirtyTransform() {
		if( isFlagged( Flag.TRANSFORM ) ) return true;
		if( local.isDirty() ) return true;
		return false;
	}

	/**
	 * @return Material for this node
	 */
	public Material getMaterial() {
		return mat;
	}

	/**
	 * Sets the material for this node.
	 * @param mat Node material or <tt>null</tt> if none
	 */
	public void setMaterial( Material mat ) {
		this.mat = mat;
	}

	@Override
	public void apply( RenderContext ctx ) {
		// Record matrix for this node
		ctx.setModelMatrix( local.toMatrix() );

		// Apply material and add entry to stack
		if( mat != null ) {
			mat.apply( ctx );
			ctx.push( mat );
		}
	}

	@Override
	public void reset( RenderContext ctx ) {
		if( mat != null ) {
			// 1. need to maintain stack of the current 'merged' material so we can restore previous states w/o having to walk up the stack
			// 1a. actually do we need this stack? we are now just walking the chain of nodes from root
			// 2. is there some cunning means of grouping into states: texture-unit, material vars (e.g. time), render properties (e.g. blend), shader params???

			// final Material prev = ctx.pop();
			//mat.reset( ctx, prev );
		}
	}
}

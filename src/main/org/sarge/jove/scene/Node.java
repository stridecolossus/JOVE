package org.sarge.jove.scene;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.geometry.BoundingVolume;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Transform;
import org.sarge.jove.light.Light;
import org.sarge.jove.material.Material;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictList;

/**
 * Scene-graph node.
 * @author Sarge
 */
public class Node {
	/**
	 * Node visitor.
	 * @see #accept(Visitor)
	 */
	public static interface Visitor {
		/**
		 * Visits the given node.
		 * @param node Node
		 * @return Whether to recurse to the children of the given node
		 * TODO - do we need this return?
		 */
		boolean visit( Node node );
	}

	// Scene
	private final List<Light> lights = new StrictList<>();
	private final List<Node> children = new StrictList<>();
	private boolean opaque = true;
	private Material mat;
	private Renderable renderable;

	// Node details
	private String name;
	private Node parent;

	// Geometry
	private boolean dirty = true;
	private Transform local = Matrix.IDENTITY;
	private Transform world;
	private BoundingVolume vol;

	/**
	 * Constructor.
	 * @param name Node name
	 */
	public Node( String name ) {
		Check.notEmpty( name );
		this.name = name;
	}

	/**
	 * @return Node name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Parent of this node or <tt>null</tt> if root
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * @return Scene-element
	 */
	public Renderable getRenderable() {
		return renderable;
	}

	/**
	 * @param renderable Scene-element
	 */
	public void setRenderable( Renderable renderable ) {
		this.renderable = renderable;
	}

	/**
	 * @return Whether this node is opaque or translucent
	 */
	public boolean isOpaque() {
		return opaque;
	}

	/**
	 * Sets whether this node is opaque or translucent.
	 * @param opaque Opaque or translucent flag
	 */
	public void setOpaque( boolean opaque ) {
		this.opaque = opaque;
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
		this.vol = vol;
	}

	/**
	 * @return Local transform
	 */
	public Transform getLocalTransform() {
		return local;
	}

	/**
	 * Sets the local transform.
	 * @param local Local transform
	 */
	public void setLocalTransform( Transform local ) {
		Check.notNull( local );
		this.local = local;
		dirty = true;
	}

	/**
	 * @return World transform
	 */
	public Transform getWorldTransform() {
		if( parent == null ) {
			dirty = false;
			return local;
		}
		else {
			if( dirty || parent.dirty || parent.local.isDirty() || local.isDirty() ) {
				// TODO - return local if parent == IDENTITY?
				world = parent.getWorldTransform().toMatrix().multiply( local.toMatrix() );
				dirty = false;
			}
			return world;
		}
	}

	/**
	 * @return Node material
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

	/**
	 * @return Children of this node
	 */
	public List<Node> getChildren() {
		return new ArrayList<>( children );
	}

	/**
	 * Adds a child node to this scene-graph.
	 * @param node Node to add
	 */
	public void add( Node node ) {
		// Check not already in a scene-graph
		if( node.parent != null ) throw new IllegalArgumentException( "Node already has a parent: " + node.parent );
		if( node == this ) throw new IllegalArgumentException( "Cannot add to self" );

		// Check for cyclic dependency
		Node n = this.parent;
		while( n != null ) {
			if( n == node ) throw new IllegalArgumentException( "Cyclic dependency" );
			n = n.parent;
		}

		// Add to scene-graph
		children.add( node );
		node.parent = this;
		node.dirty = true;
	}

	/**
	 * Removes a child from this scene-graph node.
	 * @param r Child to remove
	 */
	public void remove( Node node ) {
		assert node.parent != null;
		node.parent = null;
		children.remove( node );
	}

	/**
	 * @return Lights on this node
	 */
	public List<Light> getLights() {
		return new ArrayList<>( lights );
	}

	/**
	 * Adds a light to this node.
	 * @param light Light to add
	 */
	public void add( Light light ) {
		lights.add( light );
	}

	/**
	 * Removes a light from this node.
	 * @param light Light to remove
	 */
	public void remove( Light light ) {
		lights.remove( light );
	}

	/**
	 * Accepts the given visitor and optionally recurses to child nodes.
	 * @param visitor Node visitor
	 */
	public void accept( Visitor visitor ) {
		// Visit this node
		final boolean recurse = visitor.visit( this );

		// Recurse
		if( recurse ) {
			for( Node node : children ) {
				node.accept( visitor );
			}
		}
	}

	@Override
	public String toString() {
		return name;
	}
}

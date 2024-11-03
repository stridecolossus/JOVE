package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.sarge.jove.geometry.*;

/**
 * A <i>mutable normal vertex</i> is used to compute vertex normals for a mesh.
 * <p>
 * Note that the vertex normal is undefined until {@link #add(Vector)} is invoked.
 * <p>
 * @author Sarge
 */
public class MutableNormalVertex extends Vertex {
	private int x, y, z;

	/**
	 * Constructor.
	 * @param pos Vertex position
	 */
	public MutableNormalVertex(Point pos) {
		super(pos);
	}

	/**
	 * @return Vertex normal
	 * @see #add(Vector)
	 */
	public Normal normal() {
		return new Normal(new Vector(x, y, z));
	}

    @Override
    void add(Vector normal) {
    	x += normal.x;
    	y += normal.y;
    	z += normal.z;
    }

    @Override
    public void buffer(ByteBuffer bb) {
    	super.buffer(bb);
    	this.normal().buffer(bb);
    }

    @Override
   	public int hashCode() {
   		return Objects.hash(position(), x, y, z);
   	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof MutableNormalVertex that) &&
				this.position().equals(that.position()) &&
				this.normal().equals(that.normal());
	}
}

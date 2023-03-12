package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.*;

/**
 * A <i>vertex</i> is an element of a {@link Mesh}.
 * TODO
 * @author Sarge
 */
public class Vertex implements Bufferable {
	private final Point pos;

	/**
	 * Constructor.
	 * @param pos Vertex position
	 */
	public Vertex(Point pos) {
		this.pos = notNull(pos);
	}

	/**
	 * @return Vertex position
	 */
	public final Point position() {
		return pos;
	}

	/**
	 * Adds the given vector to the normal of this vertex.
	 * @param normal Vertex normal
	 * @throws UnsupportedOperationException if this vertex does not contain a normal
	 */
	void add(Vector normal) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void buffer(ByteBuffer bb) {
		pos.buffer(bb);
	}

	@Override
	public int hashCode() {
		return pos.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vertex that) &&
				this.pos.equals(that.position());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("pos", pos).build();
	}
}

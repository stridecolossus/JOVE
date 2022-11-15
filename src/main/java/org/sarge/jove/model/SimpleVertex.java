package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;

/**
 * A <i>simple vertex</i> comprises the vertex position only.
 * @author Sarge
 */
public class SimpleVertex implements Vertex {
	private static final Layout LAYOUT = new Layout(Point.LAYOUT);

	private final Point pos;

	/**
	 * Constructor.
	 * @param pos Vertex position
	 */
	public SimpleVertex(Point pos) {
		this.pos = notNull(pos);
	}

	@Override
	public Layout layout() {
		return LAYOUT;
	}

	@Override
	public Point position() {
		return pos;
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
				(obj instanceof SimpleVertex that) &&
				this.pos.equals(that.pos);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(pos).build();
	}
}

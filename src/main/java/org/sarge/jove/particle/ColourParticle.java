package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.*;

/**
 * A <i>particle</i> is a model for an element of a particle system.
 * @author Sarge
 */
public class ColourParticle extends Particle {
	private Colour col;

	/**
	 * Constructor.
	 * @param pos Starting position
	 * @param vec Initial vector
	 * @param col Colour
	 */
	public ColourParticle(Point pos, Vector vec, Colour col) {
		super(pos, vec);
		this.col = notNull(col);
	}

	// TODO - fade(factor) -> base-class
	// TODO - alpha? darken?

	@Override
	public int length() {
		return super.length() + Colour.LAYOUT.length();
	}

	@Override
	public void buffer(ByteBuffer bb) {
//		pos.buffer(bb);
		col.buffer(bb);
	}

//	@Override
//	public int hashCode() {
//		return Objects.hash(pos, vec, col);
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		return
//				(obj == this) ||
//				(obj instanceof ColourParticle that) &&
//				this.pos.equals(that.pos) &&
//				Objects.equals(this.vec, that.vec) &&
//				this.col.equals(that.col);
//	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("col", col)
				.build();
	}
}

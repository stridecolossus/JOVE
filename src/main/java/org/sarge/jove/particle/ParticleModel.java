package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;

/**
 * A <i>particle model</i> is a dynamic, renderable model for a particle system.
 * @author Sarge
 */
public class ParticleModel implements Model {
	private static final CompoundLayout LAYOUT = CompoundLayout.of(Point.LAYOUT);

	private final ParticleSystem sys;

	private final Bufferable vertices = new Bufferable() {
		@Override
		public int length() {
			return sys.size() * LAYOUT.stride();
		}

		@Override
		public void buffer(ByteBuffer bb) {
			for(Particle p : sys.particles()) {
				p.buffer(bb);
			}
		}
	};

	/**
	 * Constructor.
	 * @param sys Particle system
	 */
	public ParticleModel(ParticleSystem sys) {
		this.sys = notNull(sys);
	}

	@Override
	public Header header() {
		return new Header(Primitive.POINTS, sys.size(), LAYOUT);
	}

	@Override
	public Bufferable vertices() {
		return vertices;
	}

	@Override
	public Optional<Bufferable> index() {
		return Optional.empty();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(header()).append(sys).build();
	}
}

package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;

/**
 * A <i>particle model</i> is a dynamic, renderable model for a particle system.
 * @author Sarge
 */
public class ParticleModel extends AbstractModel {
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
		super(Primitive.POINTS, LAYOUT);
		this.sys = notNull(sys);
	}

	@Override
	public int count() {
		return sys.size();
	}

	@Override
	public Bufferable vertices() {
		return vertices;
	}
}

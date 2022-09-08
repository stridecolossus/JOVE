package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;
import org.sarge.jove.particle.ParticleSystem.Characteristic;

/**
 * A <i>particle model</i> is a dynamic, renderable model for a particle system.
 * @author Sarge
 */
public class ParticleModel extends AbstractModel {
	private final ParticleSystem sys;

	private final Bufferable vertices = new Bufferable() {
		@Override
		public int length() {
			return sys.size() * layout().stride();
		}

		@Override
		public void buffer(ByteBuffer bb) {
			// Write particles
			final long now = Instant.now().toEpochMilli(); // TODO
			for(Particle p : sys.particles()) {
				p.origin().buffer(bb);
				bb.putFloat((now - p.time()) / 5000f); // TODO - do this in shader, just pass timestamp => time push constant
			}

			// Write creation timestamps
			if(times(sys)) {
//				for(Particle p : sys.particles()) {
//					bb.putLong(p.time() - now);
//				}
			}
//			// TODO - not interleaved!
		}
	};

	/**
	 * Constructor.
	 * @param sys Particle system
	 */
	public ParticleModel(ParticleSystem sys) {
		super(Primitive.POINTS, layout(sys));
		this.sys = notNull(sys);
	}

	// TODO - this sucks => doesn't pass layout to ctor?
	private static boolean times(ParticleSystem sys) {
		return sys.characteristics().contains(Characteristic.TIMESTAMPS);
	}

	// TODO - non-interleaved
	private static CompoundLayout layout(ParticleSystem sys) {
		final List<Layout> layout = new ArrayList<>();
		layout.add(Point.LAYOUT);
		if(times(sys)) {
			layout.add(new Layout(1, Layout.Type.FLOAT, true, Float.BYTES)); // TODO - age -> integer (long)
		}
		return new CompoundLayout(layout);
	}

	@Override
	public synchronized int count() {
		return sys.size();
	}

	@Override
	public synchronized Bufferable vertices() {
		return vertices;
	}
}

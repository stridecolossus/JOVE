package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;

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
		super(Primitive.POINTS, build());
		this.sys = notNull(sys);
	}

//	// TODO - this sucks => doesn't pass layout to ctor?
//	private static boolean times(ParticleSystem sys) {
//		return sys.characteristics().contains(Characteristic.TIMESTAMPS);
//	}

	// TODO - non-interleaved
	private static CompoundLayout build() {
		final List<Layout> layout = new ArrayList<>();
		// TODO - configured
		layout.add(Point.LAYOUT);
		layout.add(Colour.LAYOUT);
//		if(times(sys)) {
//			layout.add(new Layout(1, Layout.Type.FLOAT, true, Float.BYTES)); // TODO - age -> integer (long)
//		}
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

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
	private final boolean times;

	private final Bufferable vertices = new Bufferable() {
		@Override
		public int length() {
			//return sys.size() * layout().stride();
			throw new RuntimeException();
		}

		@Override
		public void buffer(ByteBuffer bb) {
			// Write particles
			for(Particle p : sys.particles()) {
				p.origin().buffer(bb);
			}

//			// Write creation timestamps
//			if(times) {
//				for(Particle p : sys.particles()) {
//					bb.putLong(p.time());
//				}
//			}
//			// TODO - not interleaved!
//			// TODO - requires current time -> fragment shader (uniform?)
		}
	};

	/**
	 * Constructor.
	 * @param sys Particle system
	 */
	public ParticleModel(ParticleSystem sys) {
		this(sys, false);
	}

	/**
	 * Constructor.
	 * @param sys		Particle system
	 * @param times		Whether particles include creation timestamps
	 */
	public ParticleModel(ParticleSystem sys, boolean times) {
		super(Primitive.POINTS, layout(times));
		this.sys = notNull(sys);
		this.times = times;
	}

	private static CompoundLayout layout(boolean times) {
		final List<Layout> layout = new ArrayList<>();
		layout.add(Point.LAYOUT);
		if(times) {
			layout.add(new Layout(1, Layout.Type.INTEGER, true, 4));
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

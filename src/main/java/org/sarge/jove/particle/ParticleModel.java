package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;

import org.sarge.jove.common.*;
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
		super(Primitive.POINTS, new Layout(Point.LAYOUT, Colour.LAYOUT));
		this.sys = notNull(sys);
	}

	// TODO - layout should be dynamic? e.g. optional component: position, direction, colour, timestamp, etc => particle ~ vertex

	@Override
	public synchronized int count() {
		return sys.size();
	}

	@Override
	public synchronized Bufferable vertices() {
		return vertices;
	}
}

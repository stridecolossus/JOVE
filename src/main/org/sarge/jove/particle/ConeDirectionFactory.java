package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.RandomUtil;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Direction factory defined by a cone.
 * <p>
 * The <i>half spread</i> is essentially the randomised range of the X-Z coordinates around the axis.
 * e.g. if the spread is <tt>0.1</tt> and the X-coordinate of the axis is <tt>0.5</tt> the resultant range is <tt>0.4</tt> to <tt>0.6</tt>.
 * <p>
 * Note that the Y coordinate is fixed, i.e. This factory assumes the axis is roughly orientated <i>up</i>.
 *
 * TODO - change so this can specify cone in any 2 (or 3?) dimensions, rather than assuming X-Z, or leave as-is and use rotation?
 *
 * @author Sarge
 */
public class ConeDirectionFactory implements DirectionFactory {
	private final Vector axis;
	private final float spread;

	/**
	 * Constructor.
	 * @param axis		Cone axis
	 * @param spread	Half-spread range
	 */
	public ConeDirectionFactory( Vector axis, float spread ) {
		Check.notNull( axis );
		Check.zeroOrMore( spread );

		this.axis = axis;
		this.spread = spread;
	}

	@Override
	public Vector getDirection() {
		return new Vector(
			axis.getX() + RandomUtil.nextFloat( -spread, spread ),
			axis.getY(),
			axis.getZ() + RandomUtil.nextFloat( -spread, spread )
		);
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}

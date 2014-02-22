package org.sarge.jove.model;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.UnitCircle.CirclePoint;
import org.sarge.lib.util.Check;

/**
 * Mesh builder for spheres and similar shapes.
 * TODO - merge with conics builder?
 * @author Sarge
 */
public class SphereBuilder {
	private final UnitCircle circle;
	private final MeshBuilder builder;

	public SphereBuilder( UnitCircle circle, String layout ) {
		Check.notNull( circle );
		this.circle = circle;
		this.builder = new MeshBuilder( MeshLayout.create( Primitive.TRIANGLE_FAN, layout, false ) );
	}

	public void add( float y, float radius ) {
		for( CirclePoint pt : circle.getPoints() ) {
			final float x = pt.getX() * radius;
			final float z = pt.getY() * radius;
			final Point pos = new Point( x, y, z );
			final Vertex v = new Vertex( pos );
			// TODO - texture coords
			builder.add( v );
		}
	}
}

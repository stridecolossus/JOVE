package org.sarge.jove.model;

import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.UnitCircle.CirclePoint;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Mesh builder for spheres and similar shapes.
 * @author Sarge
 */
public class ConicBuilder {
	private final MeshLayout layout;
	private final CirclePoint[] circle;
	private final float step;

	/**
	 * Constructor.
	 * @param layout	Mesh layout
	 * @param circle	Unit-circle for sphere rings
	 */
	public ConicBuilder( MeshLayout layout, UnitCircle circle ) {
		Check.notNull( layout );
		this.layout = layout;
		this.circle = circle.getPoints();
		this.step = 1f / this.circle.length;
	}

	/**
	 * Adds a ring of vertices.
	 * @param y			Ring displacement <b>and</b> vertical texture coordinate (0..1)
	 * @param radius	Radius
	 * @param builder	Output mesh
	 */
	public void addRing( float y, float radius, MeshBuilder builder ) {
		Check.range( y, 0, 1 );
		Check.zeroOrMore( radius );

		// Note current start index
		final int start = builder.getVertices().size();

		// Add ring vertices
		float x = 0;
		for( CirclePoint pt : circle ) {
			// Create vertex
			final Point pos = new Point( pt.getX(), y, pt.getY() );
			final Vector normal = new Vector( pt.getX(), y, pt.getY() );
			final TextureCoord coords = new TextureCoord( x, y );

			// Add to sphere
			final Vertex v = new Vertex( pos.multiply( radius ) );
			v.setNormal( normal.normalize() );
			v.setTextureCoords( coords );
			builder.add( v );

			// Move to next texture coord
			x += step;
		}

		// Join strip to previous ring
		if( start > 0 ) {
			builder.addStrip( start, circle.length );
		}
	}

	/**
	 * Creates a sphere.
	 * @param radius	Radius
	 * @param num		Number of points per ring (2 or more)
	 * @param rings		Number of rings
	 * @return Mesh builder
	 */
	public MeshBuilder createSphere( float radius, int rings ) {
		Check.oneOrMore( rings );

		// Create builder
		final MeshBuilder builder = new MeshBuilder( layout );

		// Determine ring step-size
		final float ystep = ( 2 * radius ) / ( rings + 1 );

		// Build rings
		float y = radius;
		for( int n = 0; n < rings; ++n ) {
			addRing( y, radius, builder );
			y -= ystep;
		}

		return builder;
	}

	/**
	 * Creates a cone.
	 * @param radius	Radius of base
	 * @param height	Cone height
	 * @return Mesh builder
	 */
	public MeshBuilder createCone( float radius, float height ) {
		final MeshBuilder builder = new MeshBuilder( layout );
		final float half = height / 2f;
		addRing( half, 0, builder );
		addRing( -half, radius, builder );
		return builder;
	}

	/**
	 * Creates a cylinder.
	 * @param radius	Radius
	 * @param height	Height
	 * @return Mesh builder
	 */
	public MeshBuilder createCylinder( float radius, float height ) {
		final MeshBuilder builder = new MeshBuilder( layout );
		final float half = height / 2f;
		addRing( half, radius, builder );
		addRing( -half, radius, builder );
		return builder;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}

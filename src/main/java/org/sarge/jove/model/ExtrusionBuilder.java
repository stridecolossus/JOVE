package org.sarge.jove.model;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.geometry.MutablePoint;
import org.sarge.jove.geometry.MutableVector;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;

/**
 * Builder for extruded shapes using a template polygon.
 * @author Sarge
 */
public class ExtrusionBuilder {
	private final IndexedMeshBuilder builder;
	private final List<Point> polygon;
	private final Vector normal;

	private final MutableVector axis = new MutableVector();

	/**
	 * Constructor.
	 * @param layout		Mesh layout
	 * @param polygon		Template polygon
	 * @param normal		Polygon normal
	 */
	public ExtrusionBuilder( MeshLayout layout, List<Point> polygon, Vector normal ) {
		Check.notNull( normal );
		this.polygon = new ArrayList<>( polygon );
		this.normal = normal;
		this.builder = new IndexedMeshBuilder( layout );
	}

	/**
	 * Adds a new segment with the given position and orientation.
	 * @param pos		Segment position
	 * @param normal	Segment normal (assumes normalized)
	 */
	public void add( Point pos, Vector n ) {
		// Get start index of previous segment
		final int start = builder.getVertexCount();

		// Calc rotation from template to this normal
		axis.set( normal ).cross( n ).normalize();
		final float angle = normal.dot( n );

		// Generate rotated template
		final Quaternion rot = new Quaternion( axis, angle );
		final List<Point> segment = new ArrayList<>( polygon.size() );
		for( Point p : polygon ) {
			segment.add( rot.rotate( p ) );
		}

		// Add vertices
		for( Point p : segment ) {
			final Vertex v = new Vertex( p );
			builder.add( v );
			// TODO - colour, coords, normals even?
		}

		// Build triangle-strip
		if( start > 0 ) {
			builder.addStrip( start, polygon.size() );
		}

		// Update mesh
//		builder.update( start,  );
		// TODO
	}

	/**
	 * TODO - make static?
	 * Extrusion by the given amount.
	 * @param h Extrusion height
	 */
	public void extrude( float h ) {
		// Add bottom polygon
		for( Point p : polygon ) {
			final Vertex v = new Vertex( p );
			builder.add( v );
		}

		// Add top polygon
//		final Vector vec = normal.multiply( h );
		final MutableVector vec = new MutableVector( normal ).multiply( h );
		for( Point p : polygon ) {
//			final MutablePoint vp = p.add( vec );
			final MutablePoint vp = new MutablePoint( p ).add( vec );
			final Vertex v = new Vertex( vp );
			builder.add( v );
		}

		// Stripify
		builder.addStrip( 0, polygon.size() );

		// Create mesh
		builder.build();
		// TODO
	}
}

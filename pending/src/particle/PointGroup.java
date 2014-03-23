package org.sarge.jove.particle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.AbstractMesh;
import org.sarge.jove.model.AccessMode;
import org.sarge.jove.model.BufferDataType;
import org.sarge.jove.model.BufferLayout;
import org.sarge.jove.model.DefaultBufferDataType;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.model.MeshLayout;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.scene.Renderable;
import org.sarge.lib.util.ToString;

/**
 * Group of points.
 * @author Sarge
 */
public class PointGroup<T extends Vertex> implements Renderable {
	private final MeshBuilder builder;
	private final boolean dynamic;

	private AbstractMesh mesh;
	private Point pos;

	private final Comparator<Vertex> order = new Comparator<Vertex>() {
		@Override
		public int compare( Vertex a, Vertex b ) {
			final float da = distance( a );
			final float db = distance( b );
			return Float.compare( db, da );
		}

		private float distance( Vertex v ) {
			return v.getPosition().distanceSquared( pos );
		}
	};

	/**
	 * Constructor.
	 * @param colours		Whether the points are coloured
	 * @param dynamic		Whether the group is dynamically updated
	 */
	public PointGroup( boolean colours, AccessMode mode ) {
		// Build VBO layout
		final List<BufferDataType> types = new ArrayList<>();
		types.add( DefaultBufferDataType.VERTICES );
		if( colours ) types.add( DefaultBufferDataType.COLOURS );

		// Create mesh layout
		final BufferLayout buffer = new BufferLayout( types, mode );
		final MeshLayout layout = new MeshLayout( Primitive.POINTS, Collections.singletonList( buffer ), false );

		// Create builder
		this.builder = new MeshBuilder( layout );
		this.dynamic = mode != AccessMode.STATIC;
	}

	/**
	 * @return Underlying mesh builder
	 */
	public MeshBuilder getBuilder() {
		return builder;
	}

	@Override
	public void render( RenderContext ctx ) {
		// Init mesh
		if( mesh == null ) {
			mesh = ctx.getRenderingSystem().createMesh( builder );
		}

		// Build dynamic mesh
		if( dynamic ) {
			// Order by depth relative to camera
			final Camera cam = ctx.getScene().getCamera();
			final Matrix model = ctx.getModelMatrix();
			this.pos = model.transpose().multiply( cam.getPosition() );
			Collections.sort( builder.getVertices(), order );
//
//			// Update mesh
//			builder.build();
		}

		// Render points
		mesh.render( ctx );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}

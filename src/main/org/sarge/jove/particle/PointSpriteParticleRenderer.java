package org.sarge.jove.particle;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.material.PointSpriteProperty;
import org.sarge.jove.model.AbstractMesh;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.model.MeshLayout;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.scene.RenderContext;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Point-sprite renderer.
 * @author Sarge
 */
public class PointSpriteParticleRenderer implements ParticleRenderer {
	private final PointSpriteProperty props;
	private final MeshBuilder builder;

	private AbstractMesh mesh;
	private Point pos;

	private final Comparator<Particle> comparator = new Comparator<Particle>() {
		@Override
		public int compare( Particle a, Particle b ) {
			final float da = distance( a );
			final float db = distance( a );
			return Float.compare( db, da );
		}

		private float distance( Particle p ) {
			return p.getPosition().distanceSquared( pos );
		}
	};

	/**
	 * Constructor.
	 * @param layout	Mesh layout
	 * @param props		Properties
	 * @throws IllegalArgumentException if the layout is not valid for point-sprites
	 */
	public PointSpriteParticleRenderer( String layout, PointSpriteProperty props ) {
		Check.notNull( props );
		this.props = props;

		// Build VBO layout
		final MeshLayout m = MeshLayout.create( Primitive.POINTS, layout, false );
		if( m.getBufferLayout().size() != 1 ) throw new IllegalArgumentException( "Layout must have exactly one VBO" );
		if( !m.getBufferLayout().iterator().next().isDynamic() ) throw new IllegalArgumentException( "VBO layout must be dynamic" );

		// Create builder and mesh
		this.builder = new MeshBuilder( m );
	}

	/**
	 * Constructor using default VBO layout.
	 * @param props Properties
	 */
	public PointSpriteParticleRenderer( PointSpriteProperty props ) {
		this( "~VC", props );
	}

	/**
	 * Constructor using default VBO layout and point-sprite properties.
	 */
	public PointSpriteParticleRenderer() {
		this( new PointSpriteProperty() );
	}

	@Override
	public void init( RenderingSystem sys ) {
		builder.build();
		mesh = sys.createMesh( builder );
	}

	@Override
	public void render( List<Particle> particles, final RenderContext ctx ) {
		// Order sprites by distance
		pos = ctx.getScene().getCamera().getPosition();
		Collections.sort( particles, comparator );

		// Enable point-sprites
		// TODO - effect? add to material?
		final RenderingSystem sys = ctx.getRenderingSystem();
		sys.setPointSprites( props );

		// Build vertex data
		builder.reset();
		builder.clearVBOs();
		// TODO - by-pass builder/vertex stuff and use VBO directly?
		for( Particle p : particles ) {
			final Vertex v = new Vertex( p.getPosition() );
			v.setColour( p.getColour() );
			builder.add( v );
		}
		builder.build();

		// Render sprites
		mesh.render( ctx );

		// Disable point-sprites
		sys.setPointSprites( null );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}

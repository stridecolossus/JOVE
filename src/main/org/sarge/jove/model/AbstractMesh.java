package org.sarge.jove.model;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.AbstractGraphicResource;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.scene.Renderable;
import org.sarge.lib.util.ToString;

/**
 * VBO based template implementation.
 * @author Sarge
 */
public abstract class AbstractMesh extends AbstractGraphicResource implements Renderable {
	private final List<VertexBufferObject> vertexBuffers = new ArrayList<>();
	private final IndexBufferObject indexBuffer;
	private final int attributeCount;
	private /* final */ int drawSize;
	private final MeshBuilder builder;

	/**
	 * Constructor.
	 * @param builder Mesh builder
	 * @throws IllegalArgumentException if the mesh is not ready or VBOs are not supported
	 */
	protected AbstractMesh( MeshBuilder builder ) {
		if( !isSupported() ) throw new UnsupportedOperationException( "VBOs not supported" );
		if( !builder.isReady() ) throw new IllegalArgumentException( "Mesh buffers have not been built" );

		// Allocate VAO
		final int id = allocateVAO();
		setResourceID( id );
		bind( id );
		verify();

		// Create VBOs
		final MeshLayout layout = builder.getLayout();
		for( int n = 0; n < layout.getBufferLayout().size(); ++n ) {
			// Lookup buffer and associated layout
			final BufferLayout b = layout.getBufferLayout().get( n );
			final FloatBuffer fb = builder.getVertexBuffers().get( n );

			// Create VBO
			final VertexBufferObject vbo = createVertexBuffer();
			verify();
			vertexBuffers.add( vbo );

			// Init VBO
			vbo.activate();
			vbo.buffer( fb, b.getAccessMode() );
			verify();

			// Init vertex attributes
			int offset = 0;
			final int stride = b.getSize() * BufferDataType.FLOAT_SIZE;
			for( int c = 0; c < b.getBufferDataTypes().size(); ++c ) {
				final int size = b.getBufferDataTypes().get( c ).getSize();
				setVertexAttribute( c, size, stride, offset );
				verify();
				offset += size * 4; // TODO - assumes float
			}
		}

		// TODO
		attributeCount = layout.getBufferLayout().get( 0 ).getBufferDataTypes().size();

		// Create index buffer as required
		final IntBuffer indices = builder.getIndexBuffer();
		if( indices == null ) {
			indexBuffer = null;
			drawSize = builder.getVertices().size();
		}
		else {
			indexBuffer = createIndexBuffer();
			indexBuffer.activate();
			indexBuffer.buffer( indices, AccessMode.STATIC ); // TODO - index buffer mode
			verify();
			drawSize = indices.limit();
		}

		// Deactivate VAO and VBOs
		for( VertexBufferObject vbo : vertexBuffers ) {
			vbo.deactivate();
		}
		bind( 0 );
		verify();

		// Link to builder if dynamic
		this.builder = layout.isDynamic() ? builder : null;
	}

	/**
	 * @return VBOs
	 */
	public List<VertexBufferObject> getVertexBuffers() {
		return vertexBuffers;
	}

	/**
	 * @return Index VBO
	 */
	public IndexBufferObject getIndexBuffer() {
		return indexBuffer;
	}

	/**
	 * @return Whether the platform supports this mesh
	 */
	protected abstract boolean isSupported();

	/**
	 * Allocates a new VAO.
	 * @return VAO id
	 */
	protected abstract int allocateVAO();

	/**
	 * Verifies the most recent operation.
	 */
	protected abstract void verify();

	/**
	 * Binds the specified VAO.
	 * @param id VAO id
	 */
	protected abstract void bind( int id );

	/**
	 * Initialises a vertex attribute.
	 * @param idx		Vertex attribute ID
	 * @param size		Data size (number of components)
	 * @param stride	Vertex stride (bytes)
	 * @param offset	Offset in buffer (bytes)
	 * TODO - data type, class from Number
	 */
	protected abstract void setVertexAttribute( int idx, int size, int stride, int offset );

	/**
	 * Creates a new vertex buffer object.
	 * @return VBO
	 */
	protected abstract VertexBufferObject createVertexBuffer();

	/**
	 * Creates an index buffer.
	 * @return Index buffer
	 */
	protected abstract IndexBufferObject createIndexBuffer();

	@Override
	public void render( RenderContext ctx ) {
// TODO
//		// Check shader
//		if( ctx.getActiveStackEntry().getShader() == null ) throw new IllegalArgumentException( "No active material/shader for mesh rendering" );

		// Enable VAO
		bind( super.getResourceID() );

		// Activate vertex attributes
		enableVertexAttributes( true );

		// Update VBO if dynamic
		if( builder != null ) {
			// TODO - check builder for updates
			update();
		}

		// Draw mesh
		draw( drawSize, indexBuffer );

		// Deactivate vertex attributes and VAO
		enableVertexAttributes( false );
		bind( 0 );
	}

	/**
	 * Updates this mesh.
	 */
	private void update() {
		// Update VBOs
		final List<BufferLayout> layout = builder.getLayout().getBufferLayout();
		for( int n = 0; n < layout.size(); ++n ) {
			// Skip static VBOs
			final BufferLayout b = layout.get( n );
			if( b.getAccessMode() == AccessMode.STATIC ) continue;

			// Upload vertex data
			final FloatBuffer fb = builder.getVertexBuffers().get( n );
			final VertexBufferObject vbo = vertexBuffers.get( 0 );
			vbo.activate();
			vbo.buffer( fb, 0 );
		}

		// Update index buffer
		if( builder.getLayout().isIndexBufferDynamic() ) {
			indexBuffer.buffer( builder.getIndexBuffer(), 0 );
			drawSize = builder.getIndices().size();
		}
		else {
			drawSize = builder.getVertices().size();
		}
	}

	/**
	 * Activates or deactivates vertex attributes.
	 * @param enable Flag
	 */
	private void enableVertexAttributes( boolean enable ) {
		for( int n = 0; n < attributeCount; ++n ) {
			enableVertexAttribute( n, enable );
		}
	}

	/**
	 * Enables or disables a vertex attribute.
	 * @param idx		Vertex attribute index
	 * @param enable	Flag
	 */
	protected abstract void enableVertexAttribute( int idx, boolean enable );

	/**
	 * Renders this mesh.
	 * @param size		Number of elements to draw
	 * @param indices	Index buffer or <tt>null</tt> if none
	 */
	protected abstract void draw( int size, IndexBufferObject indices );

	@Override
	protected void delete( int id ) {
		// Release VBOs
		for( VertexBufferObject vbo : vertexBuffers ) {
			vbo.release();
		}

		// Release index buffer
		if( indexBuffer != null ) {
			indexBuffer.release();
		}

		// Release VAO
		deleteVAO( id );
	}

	/**
	 * Deletes the given VAO.
	 * @param id VAO id
	 */
	protected abstract void deleteVAO( int id );

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}

package org.sarge.jove.model.md5;

import java.io.File;
import java.io.IOException;

import org.sarge.jove.model.md5.MD5Model.Joint;
import org.sarge.jove.model.md5.MD5Model.MeshData;
import org.sarge.jove.model.md5.MD5Model.Triangle;
import org.sarge.jove.model.md5.MD5Model.VertexData;
import org.sarge.jove.model.md5.MD5Model.Weight;
import org.sarge.lib.util.Check;

/**
 * MD5 mesh loader.
 * @author Sarge
 */
public class MD5MeshLoader {
	private final MD5Parser parser;

	/**
	 * Constructor.
	 * @param parser MD5 file parser
	 */
	public MD5MeshLoader( MD5Parser parser ) {
		Check.notNull( parser );
		this.parser = parser;
	}

	/**
	 * Loads the static model.
	 * @param path MD5 mesh file-path
	 * @throws IOException if the mesh file cannot be parsed
	 */
	public MD5Model loadModel( String path ) throws IOException {
		// Start parser
		parser.open( path );
		parser.readHeader();

		// Load counts
		final int numJoints = parser.readInteger( "numJoints" );
		final int numMeshes = parser.readInteger( "numMeshes" );

		// Create data model
		final MD5Model model = new MD5Model( numJoints, numMeshes );

		// Load joints
		parser.startSection( "joints" );
		for( int n = 0; n < numJoints; ++n ) {
			// Load joint name
			final Joint joint = new Joint();
			joint.name = parser.readString();

			// Link to parent
			joint.parent = parser.readInteger();

			// Load geometry
			joint.pos = parser.readPoint();
			joint.rot = parser.readOrientation();

			// Skip rest of line
			parser.nextLine();

			// Add joint
			model.joints[ n ] = joint;
		}
		parser.endSection();

		// Load meshes
		for( int n = 0; n < numMeshes; ++n ) {
			parser.startSection( "mesh" );
			model.meshes[ n ] = loadMesh();
			parser.endSection();
		}

		// Cleanup
		parser.close();

		return model;
	}

	/**
	 * Load a mesh.
	 */
	private MeshData loadMesh() throws IOException {
		// Create new mesh
		final MeshData mesh = new MeshData();

		// Load texture file-path
		parser.skipToken( "shader" );
		final File file = new File( parser.readString() );
		mesh.texture = file.getName();

		// Load vertices
		final int numVerts = parser.readInteger( "numverts" );
		mesh.vertices = new VertexData[ numVerts ];
		for( int n = 0; n < numVerts; ++n ) {
			parser.skipToken( "vert" );
			final VertexData vertex = new VertexData();
			parser.skipToken( String.valueOf( n ) );
			vertex.coords = parser.readTextureCoords();
			vertex.start = parser.readInteger();
			vertex.count = parser.readInteger();
			mesh.vertices[ n ] = vertex;
		}

		// Load triangles
		final int numTriangles = parser.readInteger( "numtris" );
		mesh.triangles = new Triangle[ numTriangles ];
		for( int n = 0; n < numTriangles; ++n ) {
			parser.skipToken( "tri" );
			parser.skipToken( String.valueOf( n ) );
			final Triangle tri = new Triangle();
			for( int c = 0; c < 3; ++c ) {
				tri.index[ c ] = parser.readInteger();
			}
			mesh.triangles[ n ] = tri;
		}

		// Load weights
		final int numWeights = parser.readInteger( "numweights" );
		mesh.weights = new Weight[ numWeights ];
		for( int n = 0; n < numWeights; ++n ) {
			parser.skipToken( "weight" );
			final Weight weight = new Weight();
			parser.skipToken( String.valueOf( n ) );
			weight.jointIndex = parser.readInteger();
			weight.bias = parser.readFloat();
			weight.pos = parser.readPoint();
			mesh.weights[ n ] = weight;
		}

		return mesh;
	}
}

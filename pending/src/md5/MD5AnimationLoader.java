package org.sarge.jove.model.md5;

import java.io.IOException;

import org.sarge.jove.model.md5.MD5Model.Joint;
import org.sarge.lib.util.Check;

/**
 * MD5 animation file loader.
 * @author Sarge
 */
public class MD5AnimationLoader {
	private final MD5Parser parser;

	/**
	 * Constructor.
	 * @param parser MD5 file parser
	 */
	public MD5AnimationLoader( MD5Parser parser ) {
		Check.notNull( parser );
		this.parser = parser;
	}

	/**
	 * Loads the additional animation data.
	 * @param path		Animation file-path
	 * @param model		Mesh model
	 * @throws IOException if the file cannot be parsed
	 */
	public void load( String path, MD5Model model ) throws IOException {
		// Start parser
		parser.open( path );
		parser.readHeader();

		// Read header
		final int numFrames = parser.readInteger( "numFrames" );
		final int numJoints = parser.readInteger( "numJoints" );
		parser.readInteger( "frameRate" );
		final int numAnimatedComponents = parser.readInteger( "numAnimatedComponents" );

		// Verify header
		if( numJoints != model.joints.length ) throw new IOException( "Incorrect number of joints" );

		// Parse file
		loadHierarchy( model.joints );
		loadBounds( numFrames );
		loadBaseFrame( model.joints );
		loadFrames( numFrames, numAnimatedComponents );

		// Cleanup
		parser.close();
	}

	/**
	 * Loads additional joints data.
	 */
	private void loadHierarchy( Joint[] joints ) throws IOException {
		parser.startSection( "hierarchy" );
		for( int n = 0; n < joints.length; ++n ) {
			// Verify joint name
			final String name = parser.readString();
			if( !name.equals( joints[ n ].name ) ) throw new IOException( "Mismatched joint name: " + name );

			// Verify joint parent
			final int parent = parser.readInteger();
			if( parent != joints[ n ].parent ) throw new IOException( "Mismatched joint parent index: " + name );

			// Load additional joint info
			joints[ n ].flags = parser.readInteger();
			joints[ n ].start = parser.readInteger();

			// Skip rest of line
			parser.nextLine();
		}
		parser.endSection();
	}

	/**
	 * Loads model bounding volumes.
	 * TODO - what is this for?
	 */
	private void loadBounds( int num ) throws IOException {
		parser.startSection( "bounds" );
		for( int n = 0; n < num; ++n ) {
			parser.readPoint();
			parser.readPoint();
		}
		parser.endSection();
	}

	/**
	 * Loads skeleton base-frame.
	 */
	private void loadBaseFrame( Joint[] joints ) throws IOException {
		parser.startSection( "baseframe" );
		for( int n = 0; n < joints.length; ++n ) {
			joints[ n ].basePosition = parser.readPoint();
			joints[ n ].baseRot = parser.readOrientation();
		}
		parser.endSection();
	}

	/**
	 * Loads animation frames.
	 */
	private void loadFrames( int numFrames, int numComponents ) throws IOException {
		for( int n = 0; n < numFrames; ++n ) {
			// Start frame section
			parser.skipToken( "frame" );
			parser.skipToken( String.valueOf( n ) );
			parser.skipToken( MD5Parser.OPEN_BRACE );

			// Load components
			for( int c = 0; c < numComponents; ++c ) {
				// TODO - 6 x numJoints, why?
				parser.readFloat();
			}

			// End frame section
			parser.skipToken( MD5Parser.CLOSE_BRACE );
		}
	}
}

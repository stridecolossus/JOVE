package org.sarge.jove.model.md5;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.util.Check;

/**
 * MD5 file parser.
 * @author Sarge
 */
public class MD5Parser {
	/**
	 * Section start delimiter.
	 */
	public static final String OPEN_BRACE = "{";

	/**
	 * Section end delimiter.
	 */
	public static final String CLOSE_BRACE = "}";

	/**
	 * Array start delimiter.
	 */
	public static final String OPEN_BRACKET = "(";

	/**
	 * Array end delimiter.
	 */
	public static final String CLOSE_BRACKET = ")";

	private static final String QUOTE = "\"";

	/**
	 * Decorates exceptions with offending line number.
	 */
	private class ParseException extends IOException {
		public ParseException( String msg ) {
			super( msg + " at line " + reader.getLineNumber() );
		}
	}

	private final float[] tupleArray = new float[ 3 ];
	private final float[] coordsArray = new float[ 2 ];

	private final DataSource src;

	private LineNumberReader reader;
	private String[] tokens;
	private int idx;
	private boolean parsingSection;

	/**
	 * Constructor.
	 * @param src Data-source
	 */
	public MD5Parser( DataSource src ) {
		Check.notNull( src );
		this.src = src;
	}

	/**
	 * @return underlying data-source
	 */
	public DataSource getDataSource() {
		return src;
	}

	/**
	 * Opens an MD5 file for parsing.
	 * @param path File-path
	 * @throws IOException if the file cannot be opened
	 */
	public void open( String path ) throws IOException {
		// Open file
		if( reader != null ) throw new IOException( "Previous file was not closed" );
		reader = new LineNumberReader( new InputStreamReader( src.open( path ) ) );

		// Start parsing
		nextLine();
	}

	/**
	 * Loads and tokenizes the next line.
	 */
	public void nextLine() throws IOException {
		// TODO - no way to integrate this with TextLoader?
		while( true ) {
			// Read next line
			String line = reader.readLine();

			// Check for EOF
			if( line == null ) throw new IOException( "Unexpected EOF" );

			// Skip empty lines
			line = line.trim();
			if( line.length() == 0 ) continue;

			// Tokenize line
			tokens = line.split( "\\s" );
			break;
		}

		// Get first token
		idx = 0;
	}

	/**
	 * Parses the MD5 header.
	 * @return File version number
	 * @throws IOException if the header cannot be parsed or the file version is not supported
	 */
	public int readHeader() throws IOException {
		// Verify file version
		final int ver = readInteger( "MD5Version" );
		if( ver > 10 ) throw new IOException( "Unsupported version: " + ver );

		// Skip command line
		skipToken( "commandline" );
		readString();

		return ver;
	}

	/**
	 * Reads the next token.
	 */
	public String readToken() throws IOException {
		// Read next line as required
		if( idx >= tokens.length ) {
			nextLine();
		}

		// Get next token from this line
		return tokens[ idx++ ].trim();
	}

	/**
	 * Skips the specified token.
	 * @param token Token to skip
	 * @throws IOException if the token was not skipped
	 */
	public void skipToken( String token ) throws IOException {
		final String str = readToken();
		if( !str.equals( token ) ) throw new ParseException( "Unexpected token: expected=" + token + " actual=" + str );
	}

	/**
	 * Reads an integer value.
	 * @return Integer
	 * @throws IOException if the next token is not an integer
	 */
	public int readInteger() throws IOException {
		final String value = readToken();
		try {
			return Integer.parseInt( value );
		}
		catch( NumberFormatException e ) {
			throw new ParseException( "Invalid integer: " + value );
		}
	}

	/**
	 * Convenience method to read an integer after skipping its associated label.
	 * @param token Label to skip
	 * @return Integer
	 * @throws IOException if the label cannot be skipped or the next token is not an integer
	 */
	public int readInteger( String token ) throws IOException {
		skipToken( token );
		return readInteger();
	}

	/**
	 * Reads a floating-point value.
	 * @return Floating-point value
	 * @throws IOException if the next token is not floating-point
	 */
	public float readFloat() throws IOException {
		final String value = readToken();
		try {
			return Float.parseFloat( value );
		}
		catch( NumberFormatException e ) {
			throw new ParseException( "Invalid float: " + value );
		}
	}

	/**
	 * Reads an array of floating-point values delimited by brackets.
	 * @param array Floating-point array
	 * @throws IOException if the array cannot be read
	 */
	public void readFloatArray( float[] array ) throws IOException {
		skipToken( OPEN_BRACKET );
		for( int n = 0; n < array.length; ++n ) {
			array[ n ] = readFloat();
		}
		skipToken( CLOSE_BRACKET );
	}

	/**
	 * Reads a quote-delimited string.
	 * @return String
	 * @throws ParseException if the string cannot be parsed
	 */
	public String readString() throws IOException {
		// Read start of string
		String token = readToken();
		if( !token.startsWith( QUOTE ) ) throw new ParseException( "Starting quote not found" );

		// Read until reach end of string
		final StringBuilder sb = new StringBuilder();
		while( true ) {
			// Add next part of string
			if( sb.length() > 0 ) sb.append( ' ' );
			sb.append( token );

			// Stop at end-of-string
			if( token.endsWith( QUOTE ) ) break;

			// Move to next part
			if( idx >= tokens.length ) throw new ParseException( "Ending quote not found" );
			token = tokens[ idx ];
			++idx;
		}

		// Strip quotes
		final String str = sb.toString();
		return str.substring( 1, str.length() - 1 );
	}

	/**
	 * Starts reading a new section.
	 * @param section Section identifier
	 * @throws IOException if the section cannot be parsed
	 */
	public void startSection( String section ) throws IOException {
		if( parsingSection ) throw new ParseException( "Already parsing a section" );
		skipToken( section );
		skipToken( OPEN_BRACE );
		parsingSection = true;
	}

	/**
	 * Finishes parsing a section.
	 * @throws IOException if not parsing a section
	 */
	public void endSection() throws IOException {
		if( !parsingSection ) throw new ParseException( "Not parsing a section" );
		skipToken( CLOSE_BRACE );
		parsingSection = false;
	}

	/**
	 * Reads a point.
	 * @return Point
	 * @throws IOException if the point cannot be parsed
	 */
	public Point readPoint() throws IOException {
		readFloatArray( tupleArray );
		return new Point( tupleArray );
	}

	/**
	 * Reads an orientation as a quaternion.
	 * @return Orientation
	 * @throws IOException if the orientation cannot be parsed
	 */
	public Quaternion readOrientation() throws IOException {
		readFloatArray( tupleArray );
		final float w = calcQuaternion( tupleArray[0], tupleArray[1], tupleArray[2] );
		return new Quaternion( w, tupleArray[0], tupleArray[1], tupleArray[2] );
	}

	// TODO - should this be built-in/on class?
	private static float calcQuaternion( float x, float y, float z ) {
	    final float t = 1f - ( x * x ) - ( y * y ) - ( z * z );
	    if( t < 0 ) {
	    	return 0;
	    }
	    else {
	    	return (float) -Math.sqrt( t );
	    }
	}

	/**
	 * Reads texture coordinates.
	 * @return Texture coordinates
	 * @throws IOException if the coordinates cannot be parsed
	 */
	public TextureCoord readTextureCoords() throws IOException {
		readFloatArray( coordsArray );
		return new TextureCoord( coordsArray );
	}

	/**
	 * Closes this parser.
	 * @throws IOException if the parser cannot be closed.
	 */
	public void close() throws IOException {
		if( reader != null ) {
			reader.close();
			reader = null;
		}
	}
}

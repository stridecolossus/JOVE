package org.sarge.jove.model.obj;

/**
 * OBJ model loading utilities.
 * @author Sarge
 */
public class ObjectModelHelper {
	/**
	 * Converts arguments to a floating-point array
	 * @param args		Arguments
	 * @param array		Floating-point array
	 */
	public static void toArray( String[] args, float[] array ) {
		// Verify number of elements
		if( args.length < array.length ) throw new IllegalArgumentException( "Expected array of length " + array.length );

		// Convert
		for( int n = 0; n < array.length; ++n ) {
			array[ n ] = Float.parseFloat( args[ n ] );
		}
	}

	/**
	 * Extracts a single string argument.
	 * @param args		Arguments
	 * @param err		Error message
	 * @return String
	 * @throws IllegalArgumentException if the arguments is not a single string
	 */
	public static String toString( String[] args, String err ) {
		if( ( args == null ) || ( args.length != 1 ) ) throw new IllegalArgumentException( err );
		return args[ 0 ];
	}

	private ObjectModelHelper() {
		// Utility class
	}
}

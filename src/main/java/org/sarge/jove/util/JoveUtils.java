package org.sarge.jove.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * General JOVE utilities and helpers.
 * @author Sarge
 */
public class JoveUtils {
	/**
	 * Maps an OpenGL constant to its name.
	 * @param clazz		OpenGL class
	 * @param value		Constant value
	 * @return Constant name or <tt>null</tt> if not found
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static String mapIntegerConstant( Class<?> clazz, int value ) throws IllegalArgumentException, IllegalAccessException {
		for( Field f : clazz.getFields() ) {
			// Ensure field can be queried
			if( !f.isAccessible() ) {
				f.setAccessible( true );
			}

			// Skip if not a constant
			final int mods = f.getModifiers();
			if( !Modifier.isStatic( mods ) ) continue;
			if( !Modifier.isPublic( mods ) ) continue;
			if( !Modifier.isFinal( mods ) ) continue;

			// Skip if not an integer
			if( f.getType() != int.class ) continue;

			// Stop if found matching constant
			if( f.getInt( null ) == value ) return f.getName();
		}

		// No matching constant
		return null;
	}
}

package org.sarge.jove.shader;

import org.sarge.lib.util.Check;

/**
 * Shader parameter descriptor.
 * @author Sarge
 */
public class ShaderParameter<T> {
	private final String name;
	private final int loc;
	private final int size;
	private final int len;
	private final ShaderParameterValue<T> value;

	private boolean dirty = true;

	/**
	 * Constructor.
	 * @param name		Parameter name
	 * @param type		Parameter type
	 * @param loc		Location
	 * @param size		Number of components per element
	 * @param len		Number of elements (one-or-more)
	 */
	protected ShaderParameter( String name, int loc, int size, int len, ShaderParameterValue<T> value ) {
		Check.notEmpty( name );
		Check.zeroOrMore( loc );
		Check.oneOrMore( size );
		Check.oneOrMore( len );
		Check.notNull( value );

		this.name = name;
		this.loc = loc;
		this.size = size;
		this.len = len;
		this.value = value;
	}

	/**
	 * @return Parameter name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Parameter location
	 */
	public int getLocation() {
		return loc;
	}

	/**
	 * @return Number of components per element, e.g. 3 for a <tt>vec3</tt>
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return Array length or <tt>one</tt> if not an array
	 */
	public int getLength() {
		return len;
	}

	/**
	 * @return Whether the parameter value has changed since the last read via {@link #getValue()}
	 * @see #setValue(Object)
	 */
	public boolean isDirty() {
		return dirty;
	}



	@Override
	public String toString() {
		return name;
	}
}

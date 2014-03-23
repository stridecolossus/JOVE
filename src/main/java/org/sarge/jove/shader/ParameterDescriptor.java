package org.sarge.jove.shader;

import org.sarge.lib.util.Check;

/**
 * Descriptor for a {@link ShaderParameter}.
 * @author Sarge
 */
class ParameterDescriptor {
	private final String name;
	private final int len;
	private final String type;
	private final int loc;

	/**
	 * Constructor.
	 * @param name		Parameter name
	 * @param len		Array length 1..n
	 * @param type		Type name
	 * @param loc		Location
	 */
	ParameterDescriptor( String name, int len, String type, int loc ) {
		Check.notEmpty( name );
		Check.oneOrMore( len );
		Check.notEmpty( type );
		Check.zeroOrMore( loc );

		this.name = name;
		this.len = len;
		this.type = type;
		this.loc = loc;
	}

	/**
	 * @return Parameter name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Array length 1..n
	 */
	public int getLength() {
		return len;
	}

	/**
	 * @return Type name
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return Parameter location
	 */
	public int getLocation() {
		return loc;
	}

	@Override
	public String toString() {
		return name;
	}
}

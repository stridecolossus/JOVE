package org.sarge.jove.material;

import org.sarge.jove.app.RenderingSystem;

/**
 * Rendering property.
 * @author Sarge
 */
public interface RenderProperty {
	/**
	 * @return Property identifier
	 */
	String getType();

	/**
	 * Applies this property.
	 * @param sys Rendering system
	 */
	void apply( RenderingSystem sys );

	/**
	 * Restores the default property.
	 * @param sys Rendering system
	 */
	void reset( RenderingSystem sys );
}

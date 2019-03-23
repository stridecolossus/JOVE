package org.sarge.jove.material;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.material.Material.Property;
import org.sarge.jove.material.Shader.Parameter;
import org.sarge.jove.util.FloatSupplier;

/**
 * Binder for a floating-point material property.
 * @author Sarge
 * @see Parameter#set(float)
 */
public class FloatPropertyBinder implements Property.Binder {
	private final FloatSupplier supplier;

	/**
	 * Constructor.
	 * @param supplier Value supplier
	 */
	public FloatPropertyBinder(FloatSupplier supplier) {
		this.supplier = notNull(supplier);
	}

	@Override
	public void apply(Parameter param) {
		param.set(supplier.getAsFloat());
	}

	// TODO - handle arrays? or is that just a bufferable?
}

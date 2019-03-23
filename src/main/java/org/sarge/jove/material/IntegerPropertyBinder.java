package org.sarge.jove.material;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import org.sarge.jove.material.Material.Property;
import org.sarge.jove.material.Shader.Parameter;

/**
 * Integer property binder.
 * @author Sarge
 * @see Parameter#set(int)
 * TODO - how to handle int arrays? ditto bool arrays?
 */
public class IntegerPropertyBinder implements Property.Binder {
	/**
	 * Creates a binder for a boolean property binder.
	 * @param supplier Boolean supplier
	 */
	public static Property.Binder of(BooleanSupplier supplier) {
		final IntSupplier adapter = () -> supplier.getAsBoolean() ? 1 : 0;
		return new IntegerPropertyBinder(adapter);
	}

	private final IntSupplier supplier;

	/**
	 * Constructor.
	 * @param supplier Integer supplier
	 */
	public IntegerPropertyBinder(IntSupplier supplier) {
		this.supplier = notNull(supplier);
	}

	@Override
	public void apply(Parameter param) {
		param.set(supplier.getAsInt());
	}
}

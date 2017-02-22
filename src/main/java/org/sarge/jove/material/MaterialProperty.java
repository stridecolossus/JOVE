package org.sarge.jove.material;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.shader.ShaderParameter;
import org.sarge.jove.shader.ShaderProgram;

/**
 * Material property used to populate a {@link ShaderParameter}.
 * @author Sarge
 */
@FunctionalInterface
public interface MaterialProperty {
	/**
	 * Applies this property to the given shader.
	 * @param param		Shader parameter
	 * @param shader	Shader
	 */
	void apply(ShaderParameter param, ShaderProgram shader);

	/**
	 * @return Whether this property has changed (default is <tt>false</tt>)
	 */
	default boolean isDirty() {
		return false;
	}

	/**
	 * @param value Literal floating-point property
	 * @return Floating-point property
	 */
	static MaterialProperty value(float value) {
		return (param, shader) -> param.set(value, shader);
	}

	/**
	 * @param value Literal integer property
	 * @return Integer property
	 */
	static MaterialProperty value(int value) {
		return (param, shader) -> param.set(value, shader);
	}

	/**
	 * @param value Literal boolean property
	 * @return Boolean property
	 */
	static MaterialProperty value(boolean value) {
		return (param, shader) -> param.set(value ? 1 : 0, shader);
	}

	/**
	 * @param obj Literal bufferable property
	 * @return Bufferable property
	 */
	static MaterialProperty property(Bufferable obj) {
		return (param, shader) -> param.set(obj, shader);
	}
}

package org.sarge.jove.material;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Shader program.
 * @author Sarge
 */
public final class Shader {
	/**
	 * Shader parameter.
	 */
	public interface Parameter {
		/**
		 * Sets an integer shader parameter.
		 * @param num Integer
		 */
		void set(int num);

		/**
		 * Sets a floating-point shader parameter.
		 * @param f Floating-point value
		 */
		void set(float f);

		/**
		 * Sets a buffer shader parameter.
		 * @param buffer Buffer
		 */
		void set(FloatBuffer buffer);
	}

	private final Map<String, Parameter> params;

	/**
	 * Constructor.
	 * @param params Shader parameters ordered by name
	 */
	public Shader(Map<String, Parameter> params) {
		this.params = Map.copyOf(params);
	}

	/**
	 * Looks up a shader parameter by name.
	 * @param name Parameter name
	 * @return Shader parameter
	 */
	public Parameter parameter(String name) {
		return params.get(name);
	}

	/**
	 * @return Shader parameters ordered by name
	 */
	public Map<String, Parameter> parameters() {
		return new HashMap<>(params);
	}

	// TODO
	// - apply
	// - apply default

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}

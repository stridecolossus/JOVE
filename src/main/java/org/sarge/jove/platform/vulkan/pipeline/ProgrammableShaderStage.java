package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>programmable shader stage</i> defines a pipeline stage implemented by a {@link Shader} module.
 * @author Sarge
 */
public record ProgrammableShaderStage(VkShaderStageFlags stage, Shader shader, String name, SpecialisationConstants constants) {
	/**
	 * Default entry-point name for a shader.
	 */
	public static final String MAIN = "main";

	/**
	 * Constructor.
	 * @param stage			Shader stage
	 * @param shader		Shader module
	 * @param name			Method name
	 * @param constants		Optional specialisation constants
	 */
	public ProgrammableShaderStage {
		requireNonNull(stage);
		requireNonNull(shader);
		requireNotEmpty(name);
	}

	/**
	 * Convenience constructor.
	 * @param stage			Shader stage
	 * @param shader		Shader module
	 * @see #MAIN
	 */
	public ProgrammableShaderStage(VkShaderStageFlags stage, Shader shader) {
		this(stage, shader, MAIN, null);
	}

	/**
	 * Helper.
	 * Builds a programmable shader stage with default properties.
	 * @param stage			Shader stage
	 * @param shader		Shader
	 * @return Programmable shader stage
	 */
	public static ProgrammableShaderStage of(VkShaderStageFlags stage, Shader shader) {
		return new Builder()
				.stage(stage)
				.shader(shader)
				.build();
	}

	/**
	 * @return Programmable shader stage descriptor
	 */
	VkPipelineShaderStageCreateInfo descriptor() {
		final var info = new VkPipelineShaderStageCreateInfo();
		info.stage = new EnumMask<>(stage);
		info.module = shader.handle();
		info.pName = name;
		if(constants != null) {
			info.pSpecializationInfo = constants.descriptor();
		}
		return info;
	}

	/**
	 * Builder for a shader stage.
	 */
	public static class Builder {
		private VkShaderStageFlags stage;
		private Shader shader;
		private String name = MAIN;
		private SpecialisationConstants constants;

		/**
		 * Sets the shader stage.
		 * @param stage Shader stage
		 */
		public Builder stage(VkShaderStageFlags stage) {
			this.stage = stage;
			return this;
		}

		/**
    	 * Sets the shader module.
    	 * @param shader Shader module
    	 */
    	public Builder shader(Shader shader) {
    		this.shader = shader;
    		return this;
    	}

    	/**
    	 * Sets the method name of this shader stage (default is {@code main}).
    	 * @param name Shader method name
    	 */
    	public Builder name(String name) {
    		this.name = name;
    		return this;
    	}

    	/**
    	 * Sets the specialisation constants to parameterise this shader.
    	 * @param constants Specialisation constants
    	 */
    	public Builder constants(SpecialisationConstants constants) {
    		this.constants = constants;
    		return this;
    	}

    	/**
    	 * Constructs this shader stage.
    	 * @return New shader stage
    	 */
    	public ProgrammableShaderStage build() {
    		return new ProgrammableShaderStage(stage, shader, name, constants);
    	}
    }
}

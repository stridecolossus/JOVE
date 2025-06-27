package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireNotEmpty;

import org.sarge.jove.platform.vulkan.*;

/**
 * A <i>programmable shader stage</i> defines a pipeline stage implemented by a {@link Shader} module.
 * @author Sarge
 */
public record ProgrammableShaderStage(VkShaderStage stage, Shader shader, String name, SpecialisationConstants constants) {
	/**
	 * Constructor.
	 * @param stage			Shader stage
	 * @param shader		Shader module
	 * @param name			Method name
	 * @param constants		Specialisation constants
	 */
	public ProgrammableShaderStage {
		requireNonNull(stage);
		requireNonNull(shader);
		requireNotEmpty(name);
		requireNonNull(constants);
	}

	/**
	 * @return Programmable shader stage descriptor
	 */
	VkPipelineShaderStageCreateInfo descriptor() {
		final var info = new VkPipelineShaderStageCreateInfo();
		info.stage = stage;
		info.module = shader.handle();
		info.pName = name;
		info.pSpecializationInfo = constants.descriptor();
		return info;
	}

	/**
	 * Builder for a shader stage.
	 */
	public static class Builder {
		private VkShaderStage stage;
		private Shader shader;
		private String name = "main";
		private SpecialisationConstants constants;

		/**
		 * Constructor.
		 * @param stage Shader stage
		 */
		public Builder stage(VkShaderStage stage) {
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

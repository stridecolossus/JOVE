package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireNotEmpty;

import org.sarge.jove.platform.vulkan.*;

/**
 * A <i>programmable shader stage</i> defines a pipeline stage implemented by a {@link Shader} module.
 * @author Sarge
 */
public record ProgrammableShaderStage(VkShaderStage stage, Shader shader, String name, SpecialisationConstants constants) {
	private static final String MAIN = "main";

	/**
	 * Constructor.
	 * @param stage		Shader stage
	 * @param shader	Shader module
	 */
	public ProgrammableShaderStage(VkShaderStage stage, Shader shader) {
		this(stage, shader, MAIN, null);
	}

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
	 * Populates the shader stage descriptor.
	 */
	void populate(VkPipelineShaderStageCreateInfo info) {
		info.sType = VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO;
		info.stage = stage;
		info.module = shader.handle();
		info.pName = name;
		if(constants != null) {
			info.pSpecializationInfo = constants.build();
		}
	}

	/**
	 * Builder for a shader stage.
	 */
	public static class Builder {
		private final VkShaderStage stage;
		private Shader shader;
		private String name = MAIN;
		private SpecialisationConstants constants;

		/**
		 * Constructor.
		 * @param stage Shader stage
		 */
		public Builder(VkShaderStage stage) {
			this.stage = requireNonNull(stage);
		}

		/**
    	 * Sets the shader module.
    	 * @param shader Shader module
    	 */
    	public Builder shader(Shader shader) {
    		this.shader = requireNonNull(shader);
    		return this;
    	}

    	/**
    	 * Sets the method name of this shader stage (default is {@code main}).
    	 * @param name Shader method name
    	 */
    	public Builder name(String name) {
    		this.name = requireNotEmpty(name);
    		return this;
    	}

    	/**
    	 * Sets the specialisation constants to parameterise this shader.
    	 * @param constants Specialisation constants
    	 */
    	public Builder constants(SpecialisationConstants constants) {
    		this.constants = requireNonNull(constants);
    		return this;
    	}

    	/**
    	 * Constructs this shader stage.
    	 * @return New shader stage
    	 * @throws IllegalArgumentException if the shader module has not been configured
    	 */
    	public ProgrammableShaderStage build() {
    		if(shader == null) throw new IllegalArgumentException("Shader module not populated");
    		return new ProgrammableShaderStage(stage, shader, name, constants);
    	}
    }
}

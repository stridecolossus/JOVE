package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.core.Shader;
import org.sarge.jove.util.Check;

/**
 * Builder for a shader stage.
 */
public class ShaderStageBuilder extends AbstractPipelineBuilder<VkPipelineShaderStageCreateInfo> {
	private VkShaderStageFlag stage;
	private Shader shader;
	private String name = "main";

	/**
	 * Sets the shader stage.
	 * @param stage Shader stage
	 */
	public ShaderStageBuilder stage(VkShaderStageFlag stage) {
		this.stage = notNull(stage);
		return this;
	}

	/**
	 * Sets the shader module.
	 * @param shader Shader module
	 */
	public ShaderStageBuilder shader(Shader shader) {
		this.shader = notNull(shader);
		return this;
	}

	/**
	 * Sets the shader entry-point.
	 * @param name Entry-point name (default is <code>main</code>)
	 */
	public ShaderStageBuilder name(String name) {
		this.name = notEmpty(name);
		return this;
	}

	/**
	 * Constructs the descriptor for this shader stage.
	 * @return New shader stage descriptor
	 */
	@Override
	protected VkPipelineShaderStageCreateInfo result() {
		Check.notNull(stage);
		Check.notNull(shader);

		final var info = new VkPipelineShaderStageCreateInfo();
		info.stage = stage;
		info.module = shader.handle();
		info.pName = name;

		return info;
	}
}

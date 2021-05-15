package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;

import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.core.Shader;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;
import org.sarge.jove.platform.vulkan.util.StructureCollector;

/**
 * Builder for a shader stage.
 * @see VkPipelineShaderStageCreateInfo
 * @see Shader
 * @author Sarge
 */
public class ShaderStageBuilder extends AbstractPipelineBuilder<VkPipelineShaderStageCreateInfo> {
	/**
	 * Entry for a shader stage.
	 */
	private static class Entry {
		private VkShaderStageFlag stage;
		private Shader shader;
		private String name = "main";

		/**
		 * Validates this shader stage.
		 */
		private void validate() {
			if(stage == null) throw new IllegalArgumentException("Shader stage not specified");
			if(shader == null) throw new IllegalArgumentException("No shader specified: " + stage);
		}

		/**
		 * Populates the shader stage descriptor.
		 * @param info Shader stage descriptor
		 */
		private void populate(VkPipelineShaderStageCreateInfo info) {
			info.stage = stage;
			info.module = shader.handle();
			info.pName = name;
		}
	}

	private final Map<VkShaderStageFlag, Entry> shaders = new HashMap<>();

	private Entry entry;

	/**
	 * Starts a new shader stage.
	 */
	void init() {
		if(entry != null) throw new IllegalStateException("Previous shader stage has not been completed");
		entry = new Entry();
	}

	/**
	 * Sets the shader stage.
	 * @param stage Shader stage
	 */
	public ShaderStageBuilder stage(VkShaderStageFlag stage) {
		entry.stage = notNull(stage);
		return this;
	}

	/**
	 * Sets the shader module.
	 * @param shader Shader module
	 */
	public ShaderStageBuilder shader(Shader shader) {
		entry.shader = notNull(shader);
		return this;
	}

	/**
	 * Sets the shader entry-point.
	 * @param name Entry-point name (default is <code>main</code>)
	 */
	public ShaderStageBuilder name(String name) {
		entry.name = notEmpty(name);
		return this;
	}

	/**
	 * @return Number of shader stages
	 */
	int size() {
		return shaders.size();
	}

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException for a duplicate shader stage
	 */
	@Override
	public Builder build() {
		entry.validate();
		if(shaders.containsKey(entry.stage)) throw new IllegalArgumentException("Duplicate shader stage: " + entry.stage);
		shaders.put(entry.stage, entry);
		entry = null;
		return super.build();
	}

	/**
	 * {@inheritDoc}
	 * @throws IllegalStateException if no vertex shader has been configured
	 */
	@Override
	protected VkPipelineShaderStageCreateInfo result() {
		assert entry == null;
		if(!shaders.containsKey(VkShaderStageFlag.VERTEX)) throw new IllegalStateException("No vertex shader specified");
		return StructureCollector.toPointer(shaders.values(), VkPipelineShaderStageCreateInfo::new, Entry::populate);
	}
}

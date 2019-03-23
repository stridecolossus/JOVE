package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"stage",
	"module",
	"pName",
	"pSpecializationInfo"
})
public class VkPipelineShaderStageCreateInfo extends Structure {
	public static class ByValue extends VkPipelineShaderStageCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineShaderStageCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int stage;
	public Pointer module;
	public String pName;
	public VkSpecializationInfo.ByReference pSpecializationInfo;

	/**
	 * Builder for a pipeline shader stage descriptor.
	 */
	public static class Builder {
		public int stage;
		public Pointer shader;
		public String name = "main";
		public VkSpecializationInfo spec;

		/**
		 * Sets the pipeline stage.
		 * @param stage Pipeline stage
		 * @throws IllegalArgumentException if the given stage is not valid
		 */
		public Builder stage(VkShaderStageFlag stage) {
			if(!isValid(stage)) throw new IllegalArgumentException("Invalid shader pipeline stage: " + stage);
			this.stage = stage.value();
			return this;
		}

		/**
		 * @param stage Shader stage
		 * @return Whether the given shader stage is valid
		 * TODO - move to enum?
		 */
		private static boolean isValid(VkShaderStageFlag stage) {
			switch(stage) {
			case VK_SHADER_STAGE_VERTEX_BIT:
			case VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT:
			case VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT:
			case VK_SHADER_STAGE_GEOMETRY_BIT:
			case VK_SHADER_STAGE_FRAGMENT_BIT:
			case VK_SHADER_STAGE_COMPUTE_BIT:
				return true;

			default:
				return false;
			}
		}

		/**
		 * Sets the shader for this stage.
		 * @param shader Shader
		 */
		public Builder shader(VulkanShader shader) {
			this.shader = shader.handle();
			return this;
		}

		/**
		 * Sets the shader entry-point name.
		 * @param name Entry-point name
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Constructs this shader stage descriptor.
		 * @return New shader stage descriptor
		 * @throws IllegalArgumentException if the descriptor is not complete
		 */
		public VkPipelineShaderStageCreateInfo build() {
			final VkPipelineShaderStageCreateInfo info = new VkPipelineShaderStageCreateInfo();
			info.stage = oneOrMore(stage);
			info.module = notNull(shader);
			info.pName = notEmpty(name);
			info.pSpecializationInfo = null; // TODO
			return info;
		}
	}
}

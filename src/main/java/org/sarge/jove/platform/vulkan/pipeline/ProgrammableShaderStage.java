package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.sarge.jove.io.BufferHelper;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

/**
 * A <i>programmable shader stage</i> defines a pipeline stage implemented by a {@link Shader} module.
 * @author Sarge
 */
public record ProgrammableShaderStage(VkShaderStage stage, Shader shader, String name, VkSpecializationInfo constants) {
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
		Check.notNull(stage);
		Check.notNull(shader);
		Check.notEmpty(name);
	}

	/**
	 * Populates the shader stage descriptor.
	 */
	void populate(VkPipelineShaderStageCreateInfo info) {
		info.sType = VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO;
		info.stage = stage;
		info.module = shader.handle();
		info.pName = name;
		info.pSpecializationInfo = constants;
	}

	/**
	 * Builder for a shader stage.
	 */
	public static class Builder {
		private final VkShaderStage stage;
		private Shader shader;
		private String name = MAIN;
		private VkSpecializationInfo constants;

		/**
		 * Constructor.
		 * @param stage Shader stage
		 */
		public Builder(VkShaderStage stage) {
			this.stage = notNull(stage);
		}

		/**
    	 * Sets the shader module.
    	 * @param shader Shader module
    	 */
    	public Builder shader(Shader shader) {
    		this.shader = notNull(shader);
    		return this;
    	}

    	/**
    	 * Sets the method name of this shader stage (default is {@code main}).
    	 * @param name Shader method name
    	 */
    	public Builder name(String name) {
    		this.name = notEmpty(name);
    		return this;
    	}

    	/**
    	 * Sets the specialisation constants to parameterise this shader.
    	 * <p>
    	 * The following wrapper types are supported:
    	 * <ul>
    	 * <li>Integer</li>
    	 * <li>Float</li>
    	 * <li>Boolean</li>
    	 * </ul>
    	 * <p>
    	 * @param constants Specialisation constants indexed by identifier
    	 * @throws IllegalArgumentException for an unsupported constant type
    	 */
    	public Builder constants(Map<Integer, Object> constants) {
    		this.constants = build(constants);
    		return this;
    	}

    	/**
    	 * Builds the descriptor for the shader constants.
    	 */
    	private static VkSpecializationInfo build(Map<Integer, Object> constants) {
    		// Skip if empty
    		if(constants.isEmpty()) {
    			return null;
    		}

    		// Populate map entries
    		final var entries = constants.entrySet();
    		final Populate populate = new Populate();
    		final var info = new VkSpecializationInfo();
    		info.mapEntryCount = constants.size();
    		info.pMapEntries = StructureCollector.pointer(entries, new VkSpecializationMapEntry(), populate);

    		// Build constants data buffer
    		final var converter = new NativeBooleanConverter();
    		final ByteBuffer buffer = BufferHelper.allocate(populate.len);
    		for(var entry : entries) {
    			switch(entry.getValue()) {
    				case Float f -> buffer.putFloat(f);
    				case Integer n -> buffer.putInt(n);
    				case Boolean b -> buffer.putInt(converter.toNative(b, null));
    				default -> throw new RuntimeException();
    			}
    		}
    		assert !buffer.hasRemaining();

    		// Populate data buffer
    		info.dataSize = populate.len;
    		info.pData = buffer;

    		return info;
    	}

    	/**
    	 * Populates the descriptor for each constant and calculates the offsets and total length as side-effects.
    	 */
    	private static class Populate implements BiConsumer<Entry<Integer, Object>, VkSpecializationMapEntry> {
    		private int len = 0;

    		@Override
    		public void accept(Entry<Integer, Object> entry, VkSpecializationMapEntry struct) {
    			// Determine size of this constant
    			final Object value = entry.getValue();
    			final int size = switch(value) {
    				case Float f -> Float.BYTES;
    				case Integer n -> Integer.BYTES;
    				case Boolean b -> Integer.BYTES;
    				default -> throw new IllegalArgumentException("Unsupported constant type: " + value.getClass());
    			};

    			// Init constant
    			struct.constantID = entry.getKey();
    			struct.size = size;
    			struct.offset = len;

    			// Update buffer offset
    			len += size;
    		}
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

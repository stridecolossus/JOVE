package org.sarge.jove.platform.vulkan.pipeline;

import java.nio.*;
import java.util.*;

import org.sarge.jove.platform.vulkan.*;

/**
 * A set of <i>specialisation constants</i> are used to parameterise a shader module.
 * @see ProgrammableShaderStage
 * @author Sarge
 */
public class SpecialisationConstants {
	/**
	 * All constants are assumed to be 4 bytes.
	 */
	private static final int SIZE = 4;

	private final VkSpecializationInfo info;

	/**
	 * Constructor.
	 * @param constants Constants indexed by identifier
	 * @throws IllegalArgumentException for an unsupported constant type
	 */
	public SpecialisationConstants(Map<Integer, Object> constants) {
		this.info = build(constants);
	}

	/**
	 * @return Descriptor for the given constants
	 */
	private static VkSpecializationInfo build(Map<Integer, Object> constants) {
		// Init descriptor
		final var info = new VkSpecializationInfo();

    	// Create transient ordered map
    	final var map = new LinkedHashMap<>(constants);

    	// Populate data buffer
		info.pData = buffer(map);
    	info.dataSize = info.pData.length;

    	// Build map entries
    	final var builder = new EntryBuilder();
    	info.mapEntryCount = constants.size();
    	info.pMapEntries = map
    			.entrySet()
    			.stream()
    			.map(builder::build)
    			.toArray(VkSpecializationMapEntry[]::new);

    	assert info.pMapEntries.length == info.mapEntryCount;
    	assert builder.count == constants.size();

    	return info;
	}

	/**
	 * Builds the data buffer.
	 */
	private static byte[] buffer(Map<Integer, Object> constants) {
		// Allocate buffer
		final int length = constants.size() * SIZE;
		final byte[] array = new byte[length];
    	final var buffer = ByteBuffer.wrap(array).order(ByteOrder.nativeOrder());

    	// Populate buffer
    	for(var entry : constants.entrySet()) {
    		switch(entry.getValue()) {
        		case Float f	-> buffer.putFloat(f);
        		case Integer n	-> buffer.putInt(n);
        		case Boolean b	-> buffer.putInt(b ? 1 : 0);
        		default -> throw new IllegalArgumentException("Unsupported constant: " + entry);
    		}
    	}
    	assert !buffer.hasRemaining();

    	return array;
	}

	/**
	 * Builds map entries and records the number of entries as a side-effect.
	 */
	private static class EntryBuilder {
		private int count;

		public VkSpecializationMapEntry build(Map.Entry<Integer, Object> constant) {
			final var entry = new VkSpecializationMapEntry();
			entry.constantID = constant.getKey();
			entry.offset = count * SIZE;
			entry.size = SIZE;
			++count;
			return entry;
		}
	}

	/**
	 * @return Descriptor for this set of specialisation constants
	 */
	VkSpecializationInfo descriptor() {
		return info;
	}
}

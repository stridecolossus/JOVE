package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.nio.*;
import java.util.*;
import java.util.Map.Entry;

import org.sarge.jove.platform.vulkan.*;

/**
 * A set of <i>specialisation constants</i> are used to parameterise a shader program.
 * @see ProgrammableShaderStage
 * @author Sarge
 */
public class SpecialisationConstants {
	/**
	 * All constants are assumed to be 4 bytes.
	 */
	private static final int SIZE = 4;

	/**
	 * Constant wrapper.
	 */
	private record Constant(int id, Number value) {
		/**
		 * @return Map entry for this constant
		 */
		private VkSpecializationMapEntry descriptor() {
			final var descriptor = new VkSpecializationMapEntry();
			descriptor.constantID = id;
			descriptor.size = SIZE;
			return descriptor;
		}

		/**
		 * Appends this constant to the given buffer.
		 */
		private void append(ByteBuffer buffer) {
			if(value instanceof Float f) {
				buffer.putFloat(f);
			}
			else {
				buffer.putInt(value.intValue());
			}
		}
	}

	private final List<Constant> constants;

	/**
	 * Constructor.
	 * @param constants Shader constants indexed by identifier
	 * @throws IllegalArgumentException for an unsupported constant type
	 */
	public SpecialisationConstants(Map<Integer, Object> constants) {
		this.constants = constants
				.entrySet()
				.stream()
				.map(SpecialisationConstants::constant)
				.toList();
	}

	/**
	 * Converts and validates a specialisation constant.
	 */
	private static Constant constant(Entry<Integer, Object> entry) {
		final Number value = switch(entry.getValue()) {
			case Integer n 	-> n;
			case Float f	-> f;
			case Boolean b	-> b ? 1 : 0;
			default -> throw new IllegalArgumentException("Unsupported constant: " + entry);
		};
		final Integer key = requireNonNull(entry.getKey());
		return new Constant(key, value);
	}

	/**
	 * @return Descriptor for this set of specialisation constants
	 */
	VkSpecializationInfo descriptor() {
		// Ignore if empty
		// TODO - not here?
		if(constants.isEmpty()) {
			return null;
		}

		// Init descriptor
		final var info = new VkSpecializationInfo();
		info.mapEntryCount = constants.size();

		// Init data buffer
		final int length = constants.size() * SIZE;
		info.dataSize = length;
		info.pData = new byte[length];

		// Populate data buffer
		final var buffer = ByteBuffer.wrap(info.pData).order(ByteOrder.nativeOrder());
		for(Constant c : constants) {
			c.append(buffer);
		}
		assert !buffer.hasRemaining();

		// Build map entries
		info.pMapEntries = constants
				.stream()
				.map(Constant::descriptor)
				.toArray(VkSpecializationMapEntry[]::new);

		// Patch offsets
		for(int n = 0; n < info.pMapEntries.length; ++n) {
			info.pMapEntries[n].offset = n * SIZE;
		}
		assert info.pMapEntries.length == info.mapEntryCount;

		return info;
	}
}

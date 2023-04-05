package org.sarge.jove.platform.vulkan.pipeline;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;

import org.sarge.jove.io.BufferHelper;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

/**
 * A set of <i>specialisation constants</i> are used to parameterise shader programs.
 * @see ProgrammableShaderStage
 * @author Sarge
 */
public final class SpecialisationConstants {
	private final Map<Integer, Constant> constants;

	/**
	 * Constructor.
	 * @param constants Shader constants indexed by identifier
	 */
	public SpecialisationConstants(Map<Integer, Constant> constants) {
		this.constants = Map.copyOf(constants);
	}

	/**
	 * @return Vulkan descriptor for this set of constants
	 */
	VkSpecializationInfo build() {
		// Populate the list of entries
		final Populate populate = new Populate();
		final VkSpecializationMapEntry entries = StructureCollector.pointer(constants.entrySet(), new VkSpecializationMapEntry(), populate::populate);

		// Build the data buffer
		final var bb = BufferHelper.allocate(populate.len);
		for(Constant c : constants.values()) {
			c.buffer(bb);
		}

		// Build the specialisation constants descriptor
		final var info = new VkSpecializationInfo();
		info.mapEntryCount = constants.size();
		info.pMapEntries = entries;
		info.dataSize = populate.len;
		info.pData = bb;

		return info;
	}

	/**
	 * Helper to populate a constant entry that calculates the overall buffer length as a side-effect.
	 */
	private static class Populate {
		private int len;

		void populate(Entry<Integer, Constant> entry, VkSpecializationMapEntry out) {
			// Determine the size of this constant
			final Constant constant = entry.getValue();
			final int size = constant.size();

			// Populate the entry for this constant
			out.constantID = entry.getKey();
			out.offset = len;
			out.size = size;

			// Calculate the overall buffer length
			len += size;
		}
	}

	@Override
	public int hashCode() {
		return constants.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof SpecialisationConstants that) &&
				this.constants.equals(that.constants);
	}

	@Override
	public String toString() {
		return constants.toString();
	}

	/**
	 * A <i>specialisation constant</i> is a parameter used in a shader.
	 */
	public sealed interface Constant {
		/**
		 * @return Size of this constant (bytes)
		 */
		int size();

		/**
		 * Writes this constant to the given buffer.
		 * @param bb Buffer
		 */
		void buffer(ByteBuffer bb);

		/**
		 * Integer specialisation constant.
		 */
		record IntegerConstant(int value) implements Constant {
			@Override
			public int size() {
				return Integer.BYTES;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				bb.putInt(value);
			}
		}

		/**
		 * Floating-point specialisation constant.
		 */
		record FloatConstant(float value) implements Constant {
			@Override
			public int size() {
				return Float.BYTES;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				bb.putFloat(value);
			}

			@Override
			public boolean equals(Object obj) {
				return
						(obj == this) ||
						(obj instanceof FloatConstant that) &&
						MathsUtil.isEqual(this.value, that.value);
			}
		}

		/**
		 * Boolean specialisation constant.
		 * @see NativeBooleanConverter
		 */
		record BooleanConstant(boolean value) implements Constant {
			@Override
			public int size() {
				return Integer.BYTES;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				final int n = NativeBooleanConverter.toInteger(value);
				bb.putInt(n);
			}
		}
	}

	/**
	 * Convenience builder for a set of specialisation constants.
	 * Note that duplicate entries are over-ridden by default.
	 */
	public static class Builder {
		private final Map<Integer, Constant> constants = new HashMap<>();

		/**
		 * Adds a constant.
		 * @param id			Identifier
		 * @param constant		Constant
		 */
		public SpecialisationConstants.Builder add(int id, Constant constant) {
			Check.zeroOrMore(id);
			Check.notNull(constant);
			constants.put(id, constant);
			return this;
		}

		/**
		 * Adds all the given constants.
		 * @param constants Constants to add
		 */
		public SpecialisationConstants.Builder add(SpecialisationConstants constants) {
			this.constants.putAll(constants.constants);
			return this;
		}

		/**
		 * Constructs this set of constants.
		 * @return Specialisation constants
		 */
		public SpecialisationConstants build() {
			return new SpecialisationConstants(constants);
		}
	}
}

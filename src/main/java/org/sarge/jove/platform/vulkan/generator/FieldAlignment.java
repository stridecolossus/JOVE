package org.sarge.jove.platform.vulkan.generator;

import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.lang.foreign.MemoryLayout;

import org.sarge.jove.util.MathsUtility;

/**
 * The <i>field alignment</i> is used to determine padding required to align structure fields.
 * <p>
 * This is a mutable tracker that accumulates the byte alignment of each structure field via {@link #align(MemoryLayout)}.
 * The {@link #padding()} method determines padding required to be append to a structure based on the maximum alignment of its fields.
 * <p>
 * @author Sarge
 */
class FieldAlignment {
	private final long word;

	private long total;			// TODO - more logical/efficient to maintain current alignment?
	private long max;

	/**
	 * Constructor for the default word size (8 bytes).
	 */
	public FieldAlignment() {
		this(8);
	}

	/**
	 * Constructor.
	 * @param word Word size (bytes)
	 * @throws IllegalArgumentException if {@link #word} is zero or not a power-of-two
	 */
	public FieldAlignment(int word) {
		if(!MathsUtility.isPowerOfTwo(word)) {
			throw new IllegalArgumentException("Word size must be a power-of-two");
		}
		this.word = requireOneOrMore(word);
	}

	/**
	 * @return Accumulated byte alignment
	 */
	public long alignment() {
		return total % word;
	}

	/**
	 * Calculates the alignment padding required for the given layout and accumulates the current byte alignment as a side-effect.
	 * @param layout Memory layout
	 * @return Padding required (bytes)
	 * @see MemoryLayout#byteAlignment()
	 */
	public long align(MemoryLayout layout) {
		// Determine required padding for the next layout
		final long alignment = layout.byteAlignment();
		final long padding = (alignment - (total % word)) % alignment;

		// Track alignment
		total += padding + layout.byteSize();
		max = Math.max(max, alignment);

		// Invariants
		assert padding >= 0;
		assert padding < word;
		assert max > 0;

		return padding;
	}

	/**
	 * Determines the required padding to be appended to align the overall structure layout.
	 * @return Overall padding (bytes)
	 */
	public long padding() {
		if(total == 0) {
			return 0;
		}
		return total % max;
	}

	@Override
	public String toString() {
		return String.format("FieldAlignment[alignment=%d total=%d max=%d]", word, total, max);
	}
}

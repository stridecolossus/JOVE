package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

/**
 * A <i>render sequence</i> records Vulkan commands to a render task.
 * @author Sarge
 */
public interface RenderSequence {
	/**
	 * Records this render sequence to the given command buffer.
	 * @param buffer Command buffer to record
	 * @see Buffer#add(Command)
	 */
	void record(Buffer buffer);

	/**
	 * Wraps this render sequence with the given commands.
	 * @param before		Command before this sequence
	 * @param after			Command after this sequence
	 * @return Wrapped render sequence
	 */
	default RenderSequence wrap(Command before, Command after) {
		requireNonNull(before);
		requireNonNull(after);
		return buffer -> {
			buffer.add(before);
			RenderSequence.this.record(buffer);
			buffer.add(after);
		};
	}

	/**
	 * Creates a render sequence that records the given commands.
	 * @param commands Command sequence
	 * @return New render sequence
	 */
	static RenderSequence of(List<Command> commands) {
		final var copy = List.copyOf(commands);
		return buffer -> copy.forEach(buffer::add);
	}

	/**
	 * Creates a compound render sequence.
	 * @param sequences Render sequences
	 * @return New compound render sequence
	 */
	static RenderSequence compound(List<RenderSequence> sequences) {
		final var copy = List.copyOf(sequences);
		return buffer -> copy.forEach(e -> e.record(buffer));
	}
}

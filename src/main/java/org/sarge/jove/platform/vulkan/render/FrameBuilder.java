package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.*;

import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

/**
 * The <i>frame builder</i> is responsible for configuring the render task for the next frame.
 * TODO
 * @see FrameSet#buffer(int)
 * @see FrameBuffer#begin()
 * @see Buffer#add(org.sarge.jove.platform.vulkan.core.Command)
 * @author Sarge
 */
public class FrameBuilder {
	private final IntFunction<FrameBuffer> frames;
	private final Supplier<Buffer> factory;
	private final VkCommandBufferUsage[] flags;

	/**
	 * Constructor.
	 * @param frames		Frame buffer lookup function
	 * @param factory		Render task factory
	 * @param flags			Render task flags
	 */
	public FrameBuilder(IntFunction<FrameBuffer> frames, Supplier<Buffer> factory, VkCommandBufferUsage... flags) {
		this.frames = notNull(frames);
		this.factory = notNull(factory);
		this.flags = flags.clone();
	}

	/**
	 * Builds a render task for the next frame.
	 * @param index		Swapchain index
	 * @param seq		Render sequence
	 * @return Render task
	 */
	public Buffer build(int index, RenderSequence seq) {
		final FrameBuffer fb = frames.apply(index);
		final Buffer buffer = factory.get();
		buffer.begin(flags);
		buffer.add(fb.begin());
		seq.record(buffer);
		buffer.add(FrameBuffer.END);
		buffer.end();
		return buffer;
	}
}

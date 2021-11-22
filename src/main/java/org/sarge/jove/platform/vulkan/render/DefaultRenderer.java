package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Work;
import org.sarge.jove.platform.vulkan.core.Work.Batch;

/**
 * The <i>default renderer</i> performs a work submission to render a frame.
 * @author Sarge
 */
public class DefaultRenderer implements VulkanFrame.Renderer {
	private final IntFunction<Stream<Buffer>> factory;
	private final VkPipelineStage stage;

	/**
	 * Constructor.
	 * @param factory 		Provider for the rendering sequence(s) of a frame
	 * @param stage			Pipeline stage to render
	 */
	public DefaultRenderer(IntFunction<Stream<Buffer>> factory, VkPipelineStage stage) {
		this.factory = notNull(factory);
		this.stage = notNull(stage);
	}

	/**
	 * Constructor that renders a colour attachment.
	 * @param factory Provider for the rendering sequence(s) of a frame
	 */
	public DefaultRenderer(IntFunction<Stream<Buffer>> factory) {
		this(factory, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT);
	}

	@Override
	public void render(int index, VulkanFrame frame) {
		factory
				.apply(index)
				.map(buffer -> build(buffer, frame))
				.collect(collectingAndThen(toList(), Batch::of))
				.submit(frame.fence());
	}

	/**
	 * Builds a work submission for the given frame.
	 */
	private Work build(Buffer buffer, VulkanFrame frame) {
		return new Work.Builder(buffer.pool())
				.add(buffer)
				.wait(frame.available(), stage)
				.signal(frame.ready())
				.build();
	}
}

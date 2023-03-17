package org.sarge.jove.platform.vulkan.render;

import java.util.function.Supplier;

import org.sarge.jove.common.TransientObject;

/**
 * The <i>frame selector</i> allocates in-flight frames during rendering.
 * @author Sarge
 */
public interface FrameSelector extends TransientObject {
	/**
	 * @return Next frame to be rendered
	 */
	VulkanFrame frame();

    /**
     * Creates a selector for a number of <i>in-flight</i> frames.
     * @param count			Number of in-flight frames
     * @param factory		Frame factory
     * @return In-flight frame selector
     */
	static FrameSelector flight(int count, Supplier<VulkanFrame> factory) {
        // Instantiate in-flight frames
        final VulkanFrame[] frames = new VulkanFrame[count];
        for(int n = 0; n < count; ++n) {
        	frames[n] = factory.get();
        }

        // Cycle through in-flight frames
        return new FrameSelector() {
            private int index;

            @Override
            public VulkanFrame frame() {
            	final VulkanFrame next = frames[index];
            	if(++index >= frames.length) {
            		index = 0;
            	}
            	return next;
            }

            @Override
            public void destroy() {
            	for(var frame : frames) {
            		frame.destroy();
            	}
            }
        };
	}
}

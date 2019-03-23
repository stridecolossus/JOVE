package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.isPercentile;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.common.ScreenCoordinate;
import org.sarge.jove.util.StructureHelper;

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
	"viewportCount",
	"pViewports",
	"scissorCount",
	"pScissors"
})
public class VkPipelineViewportStateCreateInfo extends Structure {
	public static class ByValue extends VkPipelineViewportStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportStateCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int viewportCount;
	public Pointer pViewports;
	public int scissorCount;
	public Pointer pScissors;

	/**
	 * Builder for the viewport stage.
	 */
	public static class Builder {
		private final List<VkViewport> viewports = new ArrayList<>();
		private final List<VkRect2D> scissors = new ArrayList<>();

		/**
		 * Adds a viewport.
		 * @param rect		Viewport rectangle
		 * @param min		Minimum depth 0..1
		 * @param max		Maximum depth 0..1
		 * @throws IllegalArgumentException if the viewport is not valid
		 */
		public Builder viewport(Rectangle rect, float min, float max) {
			final ScreenCoordinate coords = rect.position();
			final Dimensions dim = rect.dimensions();
			final VkViewport viewport = new VkViewport();
			viewport.x = coords.x;
			viewport.y = coords.y;
			viewport.width = dim.width;
			viewport.height = dim.height;
			viewport.minDepth = isPercentile(min);
			viewport.maxDepth = isPercentile(max);
			viewports.add(viewport);
			return this;
		}

		/**
		 * Adds a viewport with default depths.
		 * @param rect Viewport rectangle
		 * @throws IllegalArgumentException if the viewport is not valid
		 */
		public Builder viewport(Rectangle rect) {
			return viewport(rect, 0, 1);
		}

		/**
		 * Adds a scissor rectangle.
		 * @param scissor rectangle
		 */
		public Builder scissor(Rectangle scissor) {
			// TODO - helper
			final VkRect2D rect = new VkRect2D();
			rect.offset.x = scissor.position().x;
			rect.offset.y = scissor.position().y;
			rect.extent.width = scissor.dimensions().width;
			rect.extent.height = scissor.dimensions().height;
			scissors.add(rect);
			return this;
		}

		/**
		 * Constructs this viewport descriptor.
		 * @return New viewport descriptor
		 * @throws IllegalArgumentException if no viewports or scissor rectangles have been specified
		 */
		public VkPipelineViewportStateCreateInfo.ByReference build() {
			// Init descriptor
			final VkPipelineViewportStateCreateInfo.ByReference info = new VkPipelineViewportStateCreateInfo.ByReference();

			// Populate viewports
			if(viewports.isEmpty()) throw new IllegalArgumentException("No viewports specified");
			info.viewportCount = viewports.size();
			info.pViewports = StructureHelper.allocate(viewports.toArray(VkViewport[]::new));

			// Populate scissors
			if(scissors.isEmpty()) throw new IllegalArgumentException("No scissor rectangles specified");
			info.scissorCount = scissors.size();
			info.pScissors = StructureHelper.allocate(scissors.toArray(VkRect2D[]::new));

			return info;
		}
	}
}

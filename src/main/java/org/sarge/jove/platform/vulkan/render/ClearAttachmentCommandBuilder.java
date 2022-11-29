package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.image.ClearValue;
import org.sarge.jove.platform.vulkan.util.VulkanUtility;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

/**
 * Builder for a command to clear attachments during a render pass.
 * @author Sarge
 */
public class ClearAttachmentCommandBuilder {
	private final RenderPass pass;

	/**
	 * Constructor.
	 * @param pass Render pass
	 */
	public ClearAttachmentCommandBuilder(RenderPass pass) {
		this.pass = notNull(pass);
	}

	/**
	 * A <i>clear attachment</i> specifies an attachment to be cleared.
	 */
	public class ClearAttachment {
		private final int index;
		private final Set<VkImageAspect> aspects;
		private final ClearValue clear;

		/**
		 * Constructor.
		 * @param attachment		Attachment to clear
		 * @param aspects			Image aspects
		 * @param clear				Clear value
		 * @throws IllegalArgumentException if {@link #attachment} is not used by this render pass
		 * @throws IllegalArgumentException if {@link #aspects} is not valid for the attachment
		 * @throws IllegalArgumentException if {@link #clear} is not the expected type for the attachment format
		 */
		public ClearAttachment(Attachment attachment, Set<VkImageAspect> aspects, ClearValue clear) {
			Check.notNull(attachment);
			Check.notEmpty(aspects);
			if(!aspects.contains(clear.aspect())) throw new IllegalArgumentException("Invalid clear value %s for attachment aspects %s".formatted(clear, aspects));
			this.index = index(attachment);
			this.aspects = Set.copyOf(aspects);
			this.clear = notNull(clear);
		}

		private int index(Attachment attachment) {
			final int index = pass.attachments().indexOf(attachment);
			if(index == -1) throw new IllegalArgumentException("Invalid attachment for this render pass: " + attachment);
			return index;
		}

		void populate(VkClearAttachment info) {
			info.aspectMask = BitMask.reduce(aspects);
			info.colorAttachment = index;
			clear.populate(info.clearValue);
		}
	}

	/**
	 * A <i>clear attachment region</i> specifies an area of the attachment(s) to clear.
	 */
	public record Region(Rectangle rect, int baseArrayLayer, int layerCount) {
		/**
		 * Constructor.
		 * @param rect					Clear rectangle
		 * @param baseArrayLayer		Image base array layer
		 * @param layerCount			Image layer count
		 */
		public Region {
			Check.notNull(rect);
			Check.zeroOrMore(baseArrayLayer);
			Check.oneOrMore(layerCount);
		}

		void populate(VkClearRect clear) {
			VulkanUtility.populate(rect, clear.rect);
			clear.baseArrayLayer = baseArrayLayer;
			clear.layerCount = layerCount;
		}
	}

	private final List<ClearAttachment> entries = new ArrayList<>();
	private final List<ClearAttachmentCommandBuilder.Region> regions = new ArrayList<>();

	/**
	 * Adds an attachment to be cleared.
	 * @param attachment Clear attachment descriptor
	 */
	public ClearAttachmentCommandBuilder attachment(ClearAttachment attachment) {
		Check.notNull(attachment);
		entries.add(attachment);
		return this;
	}

	/**
	 * Adds a region of the attachment to be cleared.
	 * @param region Region to clear
	 */
	public ClearAttachmentCommandBuilder region(ClearAttachmentCommandBuilder.Region region) {
		Check.notNull(region);
		regions.add(region);
		return this;
	}

	/**
	 * Constructs this command.
	 * @return New clear attachments command
	 */
	public Command build() {
		final VkClearAttachment attachments = StructureCollector.pointer(entries, new VkClearAttachment(), ClearAttachment::populate);
		final VkClearRect rects = StructureCollector.pointer(regions, new VkClearRect(), Region::populate);
		return (lib, buffer) -> lib.vkCmdClearAttachments(buffer, entries.size(), attachments, regions.size(), rects);
	}
}

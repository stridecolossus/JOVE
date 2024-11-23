package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.util.*;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.image.ClearValue;
import org.sarge.jove.util.BitMask;

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
		this.pass = requireNonNull(pass);
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
			requireNonNull(attachment);
			requireNotEmpty(aspects);
			if(!aspects.contains(clear.aspect())) throw new IllegalArgumentException("Invalid clear value %s for attachment aspects %s".formatted(clear, aspects));
			this.index = index(attachment);
			this.aspects = Set.copyOf(aspects);
			this.clear = requireNonNull(clear);
		}

		private int index(Attachment attachment) {
			final int index = pass.attachments().indexOf(attachment);
			if(index == -1) throw new IllegalArgumentException("Invalid attachment for this render pass: " + attachment);
			return index;
		}

		void populate(VkClearAttachment info) {
			final var value = new VkClearValue();
			clear.populate(value);
			info.aspectMask = new BitMask<>(aspects);
			info.colorAttachment = index;
			info.clearValue = value;
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
			requireNonNull(rect);
			requireZeroOrMore(baseArrayLayer);
			requireOneOrMore(layerCount);
		}

		void populate(VkClearRect clear) {
// TODO
//			VulkanLibrary.populate(rect, clear.rect);
			clear.baseArrayLayer = baseArrayLayer;
			clear.layerCount = layerCount;
		}
	}

	private final List<ClearAttachment> entries = new ArrayList<>();
	private final List<Region> regions = new ArrayList<>();

	/**
	 * Adds an attachment to be cleared.
	 * @param attachment Clear attachment descriptor
	 */
	public ClearAttachmentCommandBuilder attachment(ClearAttachment attachment) {
		requireNonNull(attachment);
		entries.add(attachment);
		return this;
	}

	/**
	 * Adds a region of the attachment to be cleared.
	 * @param region Region to clear
	 */
	public ClearAttachmentCommandBuilder region(Region region) {
		requireNonNull(region);
		regions.add(region);
		return this;
	}

	/**
	 * Constructs this command.
	 * @return New clear attachments command
	 */
	public Command build() {
		// TODO
		final VkClearAttachment[] attachments = null; // StructureCollector.pointer(entries, new VkClearAttachment(), ClearAttachment::populate);
		final VkClearRect[] rects = null; // StructureCollector.pointer(regions, new VkClearRect(), Region::populate);
		return (lib, buffer) -> lib.vkCmdClearAttachments(buffer, entries.size(), attachments, regions.size(), rects);
	}
}

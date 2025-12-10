package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

/**
 * A subpass <i>dependency</i> specifies a dependencies between the subpasses of a render pass.
 * @author Sarge
 */
public record Dependency(Properties source, Properties destination, Set<VkDependencyFlags> flags) {
	/**
	 * Marker for the <i>implicit</i> subpass before or after the render pass.
	 */
	public static final Subpass VK_SUBPASS_EXTERNAL = new Subpass(List.of(), null, Set.of());

	/**
	 * Properties of this dependency.
	 */
	public record Properties(Subpass subpass, Set<VkPipelineStageFlags> stages, Set<VkAccessFlags> access) {
		/**
		 * Constructor.
		 * @param subpass		Subpass
		 * @param stages		Pipeline stages
		 * @param access		Access flags
		 */
		public Properties {
			requireNonNull(subpass);
			requireNotEmpty(stages);
			stages = Set.copyOf(stages);
			access = Set.copyOf(access);
		}
	}

	/**
	 * Constructor.
	 * @param source			Source properties
	 * @param destination		Destination properties
	 * @param flags				Dependency flags
	 */
	public Dependency {
		requireNonNull(source);
		requireNonNull(destination);
		flags = Set.copyOf(flags);
	}

	/**
	 * Builds the descriptor for this dependency.
	 * @param subpasses Aggregated subpasses for the render pass
	 * @return Dependency descriptor
	 * @throws IllegalArgumentException if a subpass is not present
	 * @see #VK_SUBPASS_EXTERNAL
	 */
	VkSubpassDependency descriptor(List<Subpass> subpasses) {
		// Maps subpass indices
		final var indexer = new Object() {
			int index(Properties properties) {
				if(properties.subpass == VK_SUBPASS_EXTERNAL) {
					return -1;
				}
				else {
					final int index = subpasses.indexOf(properties.subpass);
					if(index < 0) {
						throw new IllegalArgumentException("Subpass not present: " + properties);
					}
					return index;
				}
			}
		};

		// Init subpass descriptor
		final var descriptor = new VkSubpassDependency();
		descriptor.dependencyFlags = new EnumMask<>(flags);

		// Populate source subpass properties
		descriptor.srcSubpass = indexer.index(source);
		descriptor.srcStageMask = new EnumMask<>(source.stages);
		descriptor.srcAccessMask = new EnumMask<>(source.access);

		// Populate destination subpass properties
		descriptor.dstSubpass = indexer.index(destination);
		descriptor.dstStageMask = new EnumMask<>(destination.stages);
		descriptor.dstAccessMask = new EnumMask<>(destination.access);

		return descriptor;
	}
}

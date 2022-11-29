package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.util.BitMask.reduce;
import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.stream.Stream;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

/**
 * A <i>sub-pass</i> specifies a stage of a {@link RenderPass}.
 * @author Sarge
 */
public class Subpass {
	private final RenderPass.Builder parent;
	private final int index;
	private final List<Dependency> dependencies = new ArrayList<>();
	private final List<Reference> colour = new ArrayList<>();
	private Reference depth;

	/**
	 * Constructor.
	 * @param parent	Parent builder
	 * @param index 	Subpass index
	 */
	Subpass(RenderPass.Builder parent, int index) {
		this.parent = notNull(parent);
		this.index = zeroOrMore(index);
	}

	/**
	 * @return Sub-pass dependencies
	 */
	Stream<Dependency> dependencies() {
		return dependencies.stream();
	}

	/**
	 * Creates a command to advance to the next subpass.
	 * @return Next subpass command
	 */
	public static Command next() {
		return (lib, buffer) -> lib.vkCmdNextSubpass(buffer, VkSubpassContents.INLINE);
	}

	/**
	 * Populates the descriptor for this sub-pass.
	 */
	void populate(VkSubpassDescription descriptor) {
		// Init descriptor
		descriptor.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

		// Populate colour attachments
		descriptor.colorAttachmentCount = colour.size();
		descriptor.pColorAttachments = StructureCollector.pointer(colour, new VkAttachmentReference(), Reference::populate);

		// Populate depth attachment
		if(depth != null) {
			final var ref = new VkAttachmentReference();
			depth.populate(ref);
			descriptor.pDepthStencilAttachment = ref;
		}
	}

	/**
	 * A <i>reference</i> specifies an attachment used by this subpass (by index).
	 */
	private record Reference(int index, Attachment attachment, VkImageLayout layout) {
		/**
		 * Constructor.
		 * @param index				Reference index
		 * @param attachment		Attachment
		 * @param layout			Image layout
		 */
		private Reference {
			Check.zeroOrMore(index);
			Check.notNull(attachment);
			Check.notNull(layout);
		}

		/**
		 * Populates the descriptor for this attachment reference.
		 */
		private void populate(VkAttachmentReference descriptor) {
			descriptor.attachment = index;
			descriptor.layout = layout;
		}
	}

	/**
	 * Allocates an attachment reference.
	 */
	private Reference reference(Attachment attachment, VkImageLayout layout) {
		final int index = parent.add(attachment);
		return new Reference(index, attachment, layout);
	}

	/**
	 * Adds a colour attachment.
	 * @param colour Colour attachment
	 * @param layout Layout
	 * @throws IllegalArgumentException for a duplicate attachment
	 */
	public Subpass colour(Attachment colour, VkImageLayout layout) {
		if(contains(colour)) throw new IllegalArgumentException("Colour attachment must be unique: " + colour);
		this.colour.add(reference(colour, layout));
		return this;
	}

	/**
	 * Convenience method to add a colour attachment with a {@link VkImageLayout#COLOR_ATTACHMENT_OPTIMAL} layout.
	 * @param colour Colour attachment
	 * @throws IllegalArgumentException for a duplicate attachment
	 */
	public Subpass colour(Attachment colour) {
		return colour(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
	}

	/**
	 * Sets the depth-stencil attachment.
	 * @param depth		Attachment
	 * @param layout	Layout
	 * @throws IllegalArgumentException if the depth-stencil has already been specified
	 */
	public Subpass depth(Attachment depth, VkImageLayout layout) {
		if(this.depth != null) throw new IllegalArgumentException("Depth-stencil attachment has already been specified");
		this.depth = reference(depth, layout);
		return this;
	}

	/**
	 * Starts a new subpass dependency.
	 * @return New subpass dependency builder
	 */
	public Dependency dependency() {
		return new Dependency();
	}

	/**
	 * @return Whether the given attachment is used by this subpass
	 */
	private boolean contains(Attachment attachment) {
		return colour.stream().map(Reference::attachment).anyMatch(attachment::equals);
	}

	/**
	 * @throws IllegalArgumentException for an empty subpass or duplicate attachment
	 */
	void verify() {
		if(colour.isEmpty() && (depth == null)) {
			throw new IllegalArgumentException("No attachments specified");
		}
		if((depth != null) && contains(depth.attachment)) {
			throw new IllegalArgumentException("Depth-stencil cannot refer to a colour attachment");
		}
	}

	/**
	 * Constructs this subpass.
	 * @return Parent builder
	 */
	public RenderPass.Builder build() {
		verify();
		return parent;
	}

	/**
	 * A <i>subpass dependency</i> configures this subpass to be dependant on a previous stage of the render-pass.
	 * <p>
	 * A subpass can be dependant on the implicit, external subpass using the {@link Dependency#external()} method.
	 * <br>
	 * A self-referential subpass is configured using {@link Dependency#self()}.
	 * <p>
	 */
	public class Dependency {
		/**
		 * Index of the implicit external subpass.
		 */
		private static final int VK_SUBPASS_EXTERNAL = ~0;

		private Integer subpass;
		private final Set<VkDependencyFlag> flags = new HashSet<>();
		private final Properties src = new Properties(this);
		private final Properties dest = new Properties(this);

		private Dependency() {
		}

		/**
		 * Sets the dependant subpass.
		 * @param subpass Dependant subpass
		 */
		public Dependency dependency(Subpass subpass) {
			this.subpass = subpass.index;
			return this;
		}

		/**
		 * Sets this as a dependency on the implicit external subpass before or after the render-pass.
		 */
		public Dependency external() {
			this.subpass = VK_SUBPASS_EXTERNAL;
			return this;
		}

		/**
		 * Sets this as a self-referential dependency.
		 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#synchronization-pipeline-barriers-subpass-self-dependencies">self-dependency</a>
		 */
		public Dependency self() {
			return dependency(Subpass.this);
		}

		/**
		 * Adds a dependency flag.
		 * @param flag Dependency flag
		 */
		public Dependency flag(VkDependencyFlag flag) {
			Check.notNull(flag);
			flags.add(flag);
			return this;
		}

		/**
		 * @return Source properties
		 */
		public Properties source() {
			return src;
		}

		/**
		 * @return Destination properties, i.e. this subpass
		 */
		public Properties destination() {
			return dest;
		}

		/**
		 * Constructs this dependency.
		 * @throws IllegalArgumentException if the dependant subpass has not been specified
		 * @throws IllegalArgumentException if the source or destination pipeline stages is empty
		 */
		public Subpass build() {
			if(subpass == null) throw new IllegalArgumentException("Dependant subpass has not been configured");
			if(src.stages.isEmpty()) throw new IllegalArgumentException("Source stages cannot be empty");
			if(dest.stages.isEmpty()) throw new IllegalArgumentException("Destination stages cannot be empty");
			dependencies.add(this);
			return Subpass.this;
		}

		/**
		 * Populates the descriptor for this dependency.
		 */
		void populate(VkSubpassDependency info) {
			info.dependencyFlags = BitMask.reduce(flags);
			info.srcSubpass = index;
			info.dstSubpass = Subpass.this.index;
			info.srcStageMask = reduce(src.stages);
			info.srcAccessMask = reduce(src.access);
			info.dstStageMask = reduce(dest.stages);
			info.dstAccessMask = reduce(dest.access);
		}
	}

	/**
	 * Source or destination properties of this dependency.
	 */
	public class Properties {
		private final Dependency dependency;
		private final Set<VkPipelineStage> stages = new HashSet<>();
		private final Set<VkAccess> access = new HashSet<>();

		private Properties(Dependency dependency) {
			this.dependency = dependency;
		}

		/**
		 * Adds a pipeline stage to this dependency.
		 * @param stage Pipeline stage
		 */
		public Properties stage(VkPipelineStage stage) {
			this.stages.add(notNull(stage));
			return this;
		}

		/**
		 * Adds an access flag to this dependency.
		 * @param access Access flag
		 */
		public Properties access(VkAccess access) {
			this.access.add(notNull(access));
			return this;
		}

		/**
		 * Constructs this set of dependency properties.
		 */
		public Dependency build() {
			return dependency;
		}
	}
}

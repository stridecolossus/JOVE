package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.platform.Resource;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * A <i>vertex buffer</i> (VBO) is used to upload vertex data to the hardware.
 * @author Sarge
 */
public interface DataBuffer extends Resource {
	/**
	 * Pushes data to the hardware.
	 * @param buffer Data buffer
	 */
	void push(ByteBuffer buffer);

	/**
	 * @return VBO
	 */
	VertexBuffer toVertexBuffer();

	/**
	 * @return Index buffer
	 */
	IndexBuffer toIndexBuffer();

	/**
	 * VBO layout descriptor.
	 */
	public static class Layout extends AbstractEqualsObject {
		/**
		 * VBO attribute descriptor.
		 */
		public static final class Attribute extends AbstractEqualsObject {
			private final int loc;
			private final Vertex.Component component;
			private final int offset;

			/**
			 * Constructor.
			 * @param loc				Shader location index
			 * @param component			Component descriptor
			 * @param offset			Offset into vertex (bytes)
			 */
			public Attribute(int loc, Component component, int offset) {
				this.loc = zeroOrMore(loc);
				this.component = notNull(component);
				this.offset = zeroOrMore(offset);
			}

			/**
			 * @return Shader location
			 */
			public int location() {
				return loc;
			}

			/**
			 * @return Component descriptor
			 */
			public Vertex.Component component() {
				return component;
			}

			/**
			 * @return Offset into vertex of this attribute (bytes)
			 */
			public int offset() {
				return offset;
			}
		}

		/**
		 * Vertex input rate.
		 */
		public enum Rate {
			VERTEX,
			INSTANCE
		}

		private final int binding;
		private final Rate rate;
		private final List<Attribute> layout;
		private final int stride;

		/**
		 * Constructor.
		 * @param binding		Binding index
		 * @param rate			Input rate
		 * @param layout		Layout
		 * @param stride		Stride per vertex (bytes)
		 * @throws IllegalArgumentException if the layout is empty
		 * @throws IllegalArgumentException for a duplicate attribute location
		 */
		public Layout(int binding, Rate rate, List<Attribute> layout, int stride) {
			this.binding = zeroOrMore(binding);
			this.rate = notNull(rate);
			this.layout = List.copyOf(notEmpty(layout));
			this.stride = oneOrMore(stride);
			verify();
		}

		private void verify() {
			if(layout.stream().map(Attribute::location).distinct().count() != layout.size()) {
				throw new IllegalArgumentException("Layout cannot contains duplicate attribute location(s)");
			}
		}

		/**
		 * @return Binding index
		 */
		public int binding() {
			return binding;
		}

		/**
		 * @return Stride per vertex (bytes)
		 */
		public int stride() {
			return stride;
		}

		/**
		 * @return Input rate
		 */
		public Rate rate() {
			return rate;
		}

		/**
		 * @return Layout attributes
		 */
		public List<Attribute> attributes() {
			return layout;
		}

		/**
		 * Builder for a VBO layout.
		 */
		public static class Builder {
			private int binding;
			private Rate rate = Rate.VERTEX;
			private final List<Attribute> layout = new ArrayList<>();
			private int offset;
			private int next = 0;

			/**
			 * Sets the binding index for this VBO.
			 * @param binding Binding index
			 */
			public Builder binding(int binding) {
				this.binding = binding;
				return this;
			}

			/**
			 * Sets the input rate for this VBO.
			 * @param rate Input rate
			 */
			public Builder rate(Rate rate) {
				this.rate = rate;
				return this;
			}

			/**
			 * Adds an attribute descriptor at the given location.
			 * @param loc			Location
			 * @param component		Component descriptor
			 */
			public Builder add(int loc, Component component) {
				final Attribute attr = new Attribute(loc, component, offset);
				layout.add(attr);
				offset += component.size() * component.bytes();
				next = loc + 1;
				return this;
			}

			/**
			 * Sets an attribute descriptor at the <i>next</i> location.
			 * @param component Component descriptor
			 */
			public Builder add(Component component) {
				return add(next, component);
			}

			/**
			 * Constructs this VBO layout.
			 * @return New layout
			 */
			public Layout build() {
				return new Layout(binding, rate, layout, offset);
			}
		}
	}
}

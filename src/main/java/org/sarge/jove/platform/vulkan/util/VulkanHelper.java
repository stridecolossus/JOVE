package org.sarge.jove.platform.vulkan.util;

import java.util.EnumMap;
import java.util.Map;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.VkColorComponentFlag;
import org.sarge.jove.platform.vulkan.VkPrimitiveTopology;

/**
 * General Vulkan utilities.
 * @author Sarge
 */
public final class VulkanHelper {
	private VulkanHelper() {
	}

	/**
	 * Default colour component flags containing all RGBA components.
	 * @see #mask(String)
	 */
	public static final int DEFAULT_COLOUR_COMPONENT = colourComponent("RGBA");

	/**
	 * Creates a colour component mask from the given string representation, e.g. <tt>RGBA</tt>.
	 * @param components Components string
	 * @return Colour component mask
	 * @throws IllegalArgumentException if a component is not a valid <tt>RGBA</tt> character
	 */
	public static int colourComponent(String components) {
		return components.chars().mapToObj(VulkanHelper::component).mapToInt(IntegerEnumeration::value).reduce(0, IntegerEnumeration.MASK);
	}

	/**
	 * Maps a colour component character.
	 */
	private static VkColorComponentFlag component(int ch) {
		switch(ch) {
		case 'R':	return VkColorComponentFlag.VK_COLOR_COMPONENT_R_BIT;
		case 'G':	return VkColorComponentFlag.VK_COLOR_COMPONENT_G_BIT;
		case 'B':	return VkColorComponentFlag.VK_COLOR_COMPONENT_B_BIT;
		case 'A':	return VkColorComponentFlag.VK_COLOR_COMPONENT_A_BIT;
		default:	throw new IllegalArgumentException("Invalid colour component: " + String.valueOf((char) ch));
		}
	}

	// TODO - move all this pipeline builder

	private static final Map<Primitive, VkPrimitiveTopology> PRIMITIVES;

	static {
		final EnumMap<Primitive, VkPrimitiveTopology> map = new EnumMap<>(Primitive.class);
		for(Primitive p : Primitive.values()) {
			final String name = "VK_PRIMITIVE_TOPOLOGY_" + p.name().toUpperCase();
			final VkPrimitiveTopology top = VkPrimitiveTopology.valueOf(name);
			map.put(p, top);
		}
		PRIMITIVES = Map.copyOf(map);
	}

	/**
	 * Maps a JOVE primitive to the Vulkan equivalent.
	 * @param primitive Primitive
	 * @return Topology
	 */
	public static VkPrimitiveTopology topology(Primitive primitive) {
		final VkPrimitiveTopology top = PRIMITIVES.get(primitive);
		if(top == null) throw new UnsupportedOperationException("Unsupported primitive: " + primitive);
		return top;
	}
}

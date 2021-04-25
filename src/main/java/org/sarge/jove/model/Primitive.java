package org.sarge.jove.model;

import org.sarge.jove.platform.vulkan.VkPrimitiveTopology;

/**
 * Drawing primitives.
 * @see VkPrimitiveTopology
 * @author Sarge
 */
public enum Primitive {
	/**
	 * Triangles.
	 */
	TRIANGLES(3, VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST),

	/**
	 * Strip of triangles.
	 */
	TRIANGLE_STRIP(3, VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP),

	/**
	 * Triangle fan.
	 */
	TRIANGLE_FAN(3, VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN),

	/**
	 * Points.
	 */
	POINTS(1, VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_POINT_LIST),

	/**
	 * Lines.
	 */
	LINES(2, VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_LIST),

	/**
	 * Strip of lines.
	 */
	LINE_STRIP(2, VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP);

	private final int size;
	private final VkPrimitiveTopology topology;

	/**
	 * Constructor.
	 * @param size 			Number of vertices per primitive
	 * @param topology		Vulkan topology
	 */
	private Primitive(int size, VkPrimitiveTopology topology) {
		this.size = size;
		this.topology = topology;
	}

	/**
	 * @return Number of vertices per primitive
	 */
	public int size() {
		return size;
	}

	/**
	 * @return Vulkan primitive topology
	 */
	public VkPrimitiveTopology topology() {
		return topology;
	}

	/**
	 * @return Whether this primitive is a strip
	 */
	public boolean isStrip() {
		return switch(this) {
			case TRIANGLE_STRIP, TRIANGLE_FAN, LINE_STRIP -> true;
			default -> false;
		};
	}

	/**
	 * @return Whether this primitive supports face normals
	 */
	public boolean isNormalSupported() {
		return switch(this) {
			case TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> true;
			default -> false;
		};
	}

	/**
	 * @param count Number of vertices
	 * @return Whether the given number of vertices is valid for this primitive
	 */
	public boolean isValidVertexCount(int count) {
		if(isStrip()) {
			return (count == 0) || (count >= size);
		}
		else {
			return (count % size) == 0;
		}
	}
}

package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Optional;

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
	TRIANGLES(3, VkPrimitiveTopology.TRIANGLE_LIST, Triangle.INDEX_TRIANGLES),

	/**
	 * Strip of triangles.
	 */
	TRIANGLE_STRIP(3, VkPrimitiveTopology.TRIANGLE_STRIP, Triangle.INDEX_STRIP),

	/**
	 * Triangle fan.
	 */
	TRIANGLE_FAN(3, VkPrimitiveTopology.TRIANGLE_FAN, null),		// TODO

	/**
	 * Points.
	 */
	POINTS(1, VkPrimitiveTopology.POINT_LIST, null),

	/**
	 * Lines.
	 */
	LINES(2, VkPrimitiveTopology.LINE_LIST, null),

	/**
	 * Strip of lines.
	 */
	LINE_STRIP(2, VkPrimitiveTopology.LINE_STRIP, null),

	/**
	 * Tesselation patch list.
	 */
	PATCH(1, VkPrimitiveTopology.PATCH_LIST, null);

	private final int size;
	private final VkPrimitiveTopology topology;
	private final IndexFactory index;

	/**
	 * Constructor.
	 * @param size 			Number of vertices per primitive
	 * @param topology		Vulkan topology
	 * @param index			Optional index factory
	 */
	private Primitive(int size, VkPrimitiveTopology topology, IndexFactory index) {
		this.size = zeroOrMore(size);
		this.topology = notNull(topology);
		this.index = index;
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
	 * @return Index factory for this primitive
	 */
	public Optional<IndexFactory> index() {
		return Optional.ofNullable(index);
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

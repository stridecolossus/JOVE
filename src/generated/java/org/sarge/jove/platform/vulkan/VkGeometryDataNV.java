package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkGeometryDataNV implements NativeStructure {
	public VkGeometryTrianglesNV triangles;
	public VkGeometryAABBNV aabbs;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				POINTER.withName("vertexData"),
				JAVA_LONG.withName("vertexOffset"),
				JAVA_INT.withName("vertexCount"),
				PADDING,
				JAVA_LONG.withName("vertexStride"),
				JAVA_INT.withName("vertexFormat"),
				PADDING,
				POINTER.withName("indexData"),
				JAVA_LONG.withName("indexOffset"),
				JAVA_INT.withName("indexCount"),
				JAVA_INT.withName("indexType"),
				POINTER.withName("transformData"),
				JAVA_LONG.withName("transformOffset")
			).withName("triangles"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				POINTER.withName("aabbData"),
				JAVA_INT.withName("numAABBs"),
				JAVA_INT.withName("stride"),
				JAVA_LONG.withName("offset")
			).withName("aabbs")
		);
	}
}

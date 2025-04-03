package org.sarge.jove.foreign;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.*;

public class ArrayTransformerTest {


	@Test
	void fields() throws Exception {

		final var structure = new VkQueueFamilyProperties();
		structure.minImageTransferGranularity.width = 42;

		final Lookup lookup = MethodHandles.lookup();

		final var min = lookup.findVarHandle(VkQueueFamilyProperties.class, "minImageTransferGranularity", VkExtent3D.class);
		System.out.println("min=" + min.get(structure));

		final var width = lookup.findVarHandle(VkExtent3D.class, "width", int.class);
		System.out.println("width=" + width.get(structure.minImageTransferGranularity));
		//System.out.println("width=" + width.get(structure));

	}

}

//	@Test
//	void path() {
//
//		//VkQueueFamilyProperties
//
//		final var structure = new VkQueueFamilyProperties();
//		final StructLayout layout = structure.layout();
////		final VarHandle handle = layout.varHandle(PathElement.groupElement("minImageTransferGranularity"), PathElement.groupElement("width"));
//
//		final MemoryLayout extent = layout.memberLayouts().get(3);
//		final VarHandle handle = layout.varHandle(PathElement.groupElement("width"));
//
//		final MemorySegment address = Arena.ofAuto().allocate(layout);
//		handle.set(address, 0L, 42);
//
//		System.out.println(handle.get(address, 0L));
//	}



//@Test
//void primitive() {
//	final Object array = new float[]{3, 4, 5};
//
//	final var layout = ValueLayout.JAVA_FLOAT;
//	final MemorySegment address = Arena.ofAuto().allocate(layout, 3);
//	System.out.println(address);
//
//	final var handle = layout.arrayElementVarHandle();
//	System.out.println(handle);
//
//	for(int n = 0; n < 3; ++n) {
//		handle.set(address, 0L, (long) n, Array.get(array, n));
//	}
//
////	address.setAtIndex(layout, 0, 3);
////	address.setAtIndex(layout, 1, 4);
////	address.setAtIndex(layout, 2, 5);
//
//	System.out.println(handle.get(address, 0L, 0L));
//	System.out.println(handle.get(address, 0L, 1L));
//	System.out.println(handle.get(address, 0L, 2L));
//
////	System.out.println(address.getAtIndex(layout, 0));
////	System.out.println(address.getAtIndex(layout, 1));
////	System.out.println(address.getAtIndex(layout, 2));
//}

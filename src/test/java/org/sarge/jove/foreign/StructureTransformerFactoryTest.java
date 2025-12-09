package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;

class StructureTransformerFactoryTest {
	private StructureTransformerFactory factory;

	@BeforeEach
	void before() {
		factory = new StructureTransformerFactory(DefaultRegistry.create());
	}

	@Test
	void transformer() {
		// Create structure transformer
		final var transformer = factory.transformer(MockStructure.class);
		assertEquals(MockStructure.LAYOUT, transformer.layout());
		assertEquals(MemorySegment.NULL, transformer.empty());

		// Init structure instance
		final var instance = new MockStructure();
		instance.field = 42;

		// Verify marshalling
		@SuppressWarnings("resource")
		final MemorySegment address = transformer.marshal(instance, Arena.ofAuto());
		final var result = (MockStructure) transformer.unmarshal().apply(address);
		assertEquals(42, result.field);
	}

	public static class ExpectedDefaultConstructor extends MockStructure {
		private ExpectedDefaultConstructor() {
		}
	}

	@Test
	void constructor() {
		final Exception e = assertThrows(IllegalArgumentException.class, () -> factory.transformer(ExpectedDefaultConstructor.class));
		assertTrue(e.getMessage().startsWith("Cannot find default constructor"));
	}

	public static class AnonymousField implements NativeStructure {
    	@Override
    	public StructLayout layout() {
    		return MemoryLayout.structLayout(JAVA_INT);
    	}
	}

	@DisplayName("A native structure field cannot be anonymous")
    @Test
    void anonymous() {
		final Exception e = assertThrows(IllegalArgumentException.class, () -> factory.transformer(AnonymousField.class));
		assertTrue(e.getMessage().startsWith("Anonymous structure member"));
    }

	public static class UnknownField implements NativeStructure {
    	@Override
    	public StructLayout layout() {
    		return MemoryLayout.structLayout(JAVA_INT.withName("cobblers"));
    	}
	}

	@DisplayName("The name of a native structure field must correspond to a member of the structure")
	@Test
	void unknown() {
		final Exception e = assertThrows(IllegalArgumentException.class, () -> factory.transformer(UnknownField.class));
		assertTrue(e.getMessage().startsWith("Unknown structure field"));
	}

	public static class UnsupportedField implements NativeStructure {
		public Object field;

    	@Override
    	public StructLayout layout() {
    		return MemoryLayout.structLayout(POINTER.withName("field"));
    	}
	}

	@DisplayName("A native structure field must have a registered transformer")
	@Test
	void unsupported() {
		final Exception e = assertThrows(IllegalArgumentException.class, () -> factory.transformer(UnsupportedField.class));
		assertTrue(e.getMessage().startsWith("Unsupported field type"));
	}

	// TODO - more explanation
	@Nested
	class LayoutTest {
		private SegmentAllocator allocator;

		@BeforeEach
		void before() {
			allocator = Arena.ofAuto();
		}

    	@DisplayName("A nested structure field can be marshalled")
    	@Test
    	void nested() {
    		// Init a structure containing a nested structure
    		final var structure = new VkQueueFamilyProperties();
    		structure.minImageTransferGranularity = new VkExtent3D();
    		structure.minImageTransferGranularity.width = 42;

    		// Marshal structure
    		final StructureTransformer transformer = factory.transformer(VkQueueFamilyProperties.class);
    		final MemorySegment address = transformer.marshal(structure, allocator);

    		// Unmarshal and compare
    		final var result = (VkQueueFamilyProperties) transformer.unmarshal().apply(address);
    		assertEquals(42, result.minImageTransferGranularity.width);
    	}

    	@DisplayName("A fixed-length array field can be marshalled")
    	@Test
    	void fixedLengthArray() {
    		// Init structure
    		final var structure = new VkClearColorValue();
    		structure.float32 = new float[]{0, 0, 0, 1};

    		// Marshal structure
    		final StructureTransformer transformer = factory.transformer(VkClearColorValue.class);
    		final MemorySegment address = transformer.marshal(structure, allocator);

    		// Unmarshal and compare
    		final var result = (VkClearColorValue) transformer.unmarshal().apply(address);
    		assertArrayEquals(structure.float32, result.float32);
    	}

    	@DisplayName("A variable-length array field can be marshalled")
    	@Test
    	void variableLengthArray() {
    		// Init structure
    		final var structure = new VkDeviceQueueCreateInfo();
    		structure.pQueuePriorities = new float[]{0.1f, 0.2f};

    		// Marshal structure
    		final StructureTransformer transformer = factory.transformer(VkDeviceQueueCreateInfo.class);
    		final MemorySegment address = transformer.marshal(structure, allocator);

    		// Check pointer to array
    		// TODO - we do not yet support unmarshalling a variable-length array?
    		final long offset = structure.layout().byteOffset(PathElement.groupElement("pQueuePriorities"));
    		assertEquals(32, offset);
    		final MemorySegment array = address.get(ValueLayout.ADDRESS, offset).reinterpret(8);
    		assertEquals(0.1f, array.getAtIndex(ValueLayout.JAVA_FLOAT, 0L));
    		assertEquals(0.2f, array.getAtIndex(ValueLayout.JAVA_FLOAT, 1L));
    	}

    	@DisplayName("A variable-length array char/byte field can be transparently marshalled as a string")
    	@Test
    	void string() {
    		// Init structure with a string mapped to a byte/char array
    		final var structure = new VkExtensionProperties();
    		structure.extensionName = "name";

    		// Marshal structure
    		final StructureTransformer transformer = factory.transformer(VkExtensionProperties.class);
    		final MemorySegment address = transformer.marshal(structure, allocator);

    		// Unmarshal and compare
    		final var result = (VkExtensionProperties) transformer.unmarshal().apply(address);
    		assertEquals("name", result.extensionName);
    	}
    }
}

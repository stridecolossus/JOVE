package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class StructureTransformerFactoryTest {
	private StructureTransformerFactory factory;

	@BeforeEach
	void before() {
		final var registry = new Registry();
		registry.register(int.class, new PrimitiveTransformer<>(JAVA_INT));
		factory = new StructureTransformerFactory(registry);
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
		public String field;

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
}

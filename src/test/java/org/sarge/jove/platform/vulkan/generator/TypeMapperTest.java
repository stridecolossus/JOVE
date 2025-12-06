package org.sarge.jove.platform.vulkan.generator;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.generator.TypeMapper.HANDLE;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TypeMapperTest {
	private TypeMapper mapper;

	@BeforeEach
	void before() {
		mapper = new TypeMapper();
	}

	@Test
	void unknown() {
		assertThrows(IllegalArgumentException.class, () -> mapper.map(new StructureField<>("field", "unknown")));
	}

	@Test
	void duplicate() {
		assertThrows(IllegalArgumentException.class, () -> mapper.add("int", NativeType.of(JAVA_INT)));
	}

	@Nested
	class Synonyms {
		@Test
		void typedef() {
			mapper.typedef("int", "synonym");
			assertEquals(NativeType.of(JAVA_INT), mapper.map(new StructureField<>("field", "int")));
		}

		@Test
		void unknown() {
			assertThrows(IllegalArgumentException.class, () -> mapper.typedef("unknown", "synoynm"));
		}
	}

	@Nested
	class Predefined {
		private static ValueLayout[] primitive() {
			return new ValueLayout[] {
					JAVA_BYTE,
					JAVA_SHORT,
					JAVA_INT,
					JAVA_LONG,
					JAVA_FLOAT,
					JAVA_DOUBLE
			};
		}

		@DisplayName("A primitive field is mapped to the equivalent Java primitve")
		@ParameterizedTest
		@MethodSource
		void primitive(ValueLayout layout) {
			final String typename = layout.carrier().getSimpleName();
			assertEquals(NativeType.of(layout), mapper.map(new StructureField<>("field", typename)));
		}

		@DisplayName("A primitive character is mapped to a byte")
		@Test
		void character() {
			assertEquals(NativeType.of(JAVA_BYTE), mapper.map(new StructureField<>("field", "char")));
		}

		@DisplayName("A Vulkan boolean is mapped to a Java boolean represented as a 4-byte integer")
		@Test
		void booleans() {
			final var flag = new StructureField<>("flag", "VkBool32");
			final var expected = new NativeType("boolean", JAVA_INT);
			assertEquals(expected, mapper.map(flag));
		}

		@DisplayName("A primitive array is mapped to a Java primitive array")
		@Test
		void primitiveArray() {
			final var expected = new NativeType("float[]", JAVA_FLOAT);
			assertEquals(expected, mapper.map(new StructureField<>("colour", "float", 4)));
		}

		@DisplayName("The standard C types are mapped to the equivalent Java primitives")
		@Test
		void types() {
			assertEquals(NativeType.of(JAVA_BYTE),  mapper.map(new StructureField<>("field", "uint8_t")));
			assertEquals(NativeType.of(JAVA_SHORT), mapper.map(new StructureField<>("field", "uint16_t")));
			assertEquals(NativeType.of(JAVA_INT),   mapper.map(new StructureField<>("field", "int32_t")));
			assertEquals(NativeType.of(JAVA_INT),   mapper.map(new StructureField<>("field", "uint32_t")));
			assertEquals(NativeType.of(JAVA_LONG),  mapper.map(new StructureField<>("field", "int64_t")));
			assertEquals(NativeType.of(JAVA_LONG),  mapper.map(new StructureField<>("field", "uint64_t")));
			assertEquals(NativeType.of(JAVA_LONG),  mapper.map(new StructureField<>("field", "size_t")));
		}
	}

	@Nested
	class Strings {
		@DisplayName("A char* is mapped to a Java string")
		@Test
		void pointer() {
			final var expected = new NativeType("String", ADDRESS);
			assertEquals(expected, mapper.map(new StructureField<>("pName", "char*")));
		}

		@DisplayName("A char[] is mapped to a Java string with a byte[] layout")
		@Test
		void characterArray() {
			final var expected = new NativeType("String", JAVA_BYTE);
			assertEquals(expected, mapper.map(new StructureField<>("name", "char", 16)));
		}

		@DisplayName("A char** is mapped to a Java string array")
		@Test
		void stringArray() {
			final var expected = new NativeType("String[]", ADDRESS);
			assertEquals(expected, mapper.map(new StructureField<>("pStringArray", "char**")));
		}
	}

	@Nested
	class Pointers {
		@DisplayName("A void* is represented as a handle")
		@Test
		void pointer() {
			assertEquals(HANDLE, mapper.map(new StructureField<>("pNext", "void*")));
		}

		@DisplayName("A pointer to a defined handle is represented by a JOVE handle")
		@Test
		void handle() {
			mapper.add("VkBufferView", HANDLE);
			assertEquals(HANDLE, mapper.map(new StructureField<>("pTexel", "VkBufferView*")));
		}

		@DisplayName("A pointer to a defined handle with a pluralised name is assumed to be an array of JOVE handles")
		@Test
		void array() {
			final var expected = new NativeType("Handle[]", ADDRESS);
			mapper.add("VkSemaphore", HANDLE);
			assertEquals(expected, mapper.map(new StructureField<>("pWaitSemaphores", "VkSemaphore*")));
		}

		@DisplayName("A pointer to a primitive type is mapped to a handle to that array")
		@Test
		void primitiveArray() {
			assertEquals(new NativeType("int[]", ADDRESS), mapper.map(new StructureField<>("pValues", "uint32_t*")));
			assertEquals(new NativeType("float[]", ADDRESS), mapper.map(new StructureField<>("pValues", "float*")));
		}

		@DisplayName("Shader code is mapped to a byte array")
		@Test
		void blob() {
			assertEquals(new NativeType("byte[]", JAVA_BYTE), mapper.map(new StructureField<>("pCode", "uint32_t*", 0)));
			assertEquals(new NativeType("byte[]", JAVA_BYTE), mapper.map(new StructureField<>("pData", "uint32_t*", 0)));
		}
	}

	@Nested
	class Structures {
		private NativeType structure;

		@BeforeEach
		void before() {
			structure = new NativeType("VkMemoryType", MemoryLayout.structLayout());
			mapper.add(structure);
		}

		@DisplayName("A pointer to a structure is mapped to a structure")
		@Test
		void pointer() {
			final var expected = new NativeType("VkMemoryType", ADDRESS);
			assertEquals(expected, mapper.map(new StructureField<>("pStructure", "VkMemoryType*")));
		}

		@DisplayName("A pointer to a structure with a pluralised name is mapped to a structure array")
		@Test
		void pointerStructureArray() {
			final var expected = new NativeType("VkMemoryType[]", ADDRESS);
			assertEquals(expected, mapper.map(new StructureField<>("pStructures", "VkMemoryType*")));
		}

		@DisplayName("A structure field that is not a pointer is a nested structure")
		@Test
		void nested() {
			assertEquals(structure, mapper.map(new StructureField<>("nested", "VkMemoryType")));
		}

		@DisplayName("An explictly specified structure array is mapped to a Java array")
		@Test
		void array() {
			final var expected = new NativeType("VkMemoryType[]", structure.layout());
			assertEquals(expected, mapper.map(new StructureField<>("array", "VkMemoryType", 16)));
		}
	}

	@Nested
	class Enumerations {
		@BeforeEach
		void before() {
			mapper.typedef("uint32_t", "VkFlags");
		}

		@DisplayName("An enumeration field is mapped an integer enumeration")
		@Test
		void enumeration() {
			final var enumeration = new NativeType("VkStructureType", JAVA_INT);
			mapper.add(enumeration);
			assertEquals(enumeration, mapper.map(new StructureField<>("sType", "VkStructureType")));
		}

		@DisplayName("All bitfields are mapped to an enumeration mask")
		@Test
		void mask() {
			final var expected = new NativeType("EnumMask<VkAccessFlags>", JAVA_INT);
			mapper.add(new NativeType("VkAccessFlags", JAVA_INT));
			assertEquals(expected, mapper.map(new StructureField<>("flags", "VkAccessFlags")));
			assertEquals(expected, mapper.map(new StructureField<>("accessMask", "VkAccessFlags")));
		}

		@DisplayName("A bitfield for an undefined enumeration is mapped to an integer")
		@Test
		void typedef() {
			mapper.typedef("VkFlags", "VkAccessFlags");
			assertEquals(NativeType.of(JAVA_INT), mapper.map(new StructureField<>("flags", "VkAccessFlags")));
		}

		@DisplayName("A bitfield for an unknown enumeration cannot be mapped")
		@Test
		void maskMissingTypeDefinition() {
			assertThrows(IllegalArgumentException.class, () -> mapper.map(new StructureField<>("flags", "VkAccessFlags")));
		}

		@Test
		void pointer() {
			mapper.add(new NativeType("VkPipelineStageFlags", JAVA_INT));
			assertEquals(new NativeType("int[]", JAVA_INT), mapper.map(new StructureField<>("mask", "VkPipelineStageFlags*")));
		}
	}
}

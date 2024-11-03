package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkSpecializationInfo;
import org.sarge.jove.platform.vulkan.pipeline.SpecialisationConstants.Constant;
import org.sarge.jove.platform.vulkan.pipeline.SpecialisationConstants.Constant.*;

class SpecialisationConstantsTest {
	private SpecialisationConstants constants;

	@BeforeEach
	void before() {
		final Map<Integer, Constant> map = Map.of(
				1, new IntegerConstant(1),
				2, new FloatConstant(2),
				3, new BooleanConstant(true)
		);
		constants = new SpecialisationConstants(map);
	}

	@Test
	void build() {
		final VkSpecializationInfo info = constants.build();
		final int size = 3 * 4;
		assertEquals(3, info.mapEntryCount);
		assertEquals(size, info.dataSize);
		assertEquals(size, info.pData.limit());
	}

	@Nested
	class ConstantTests {
		private ByteBuffer bb;

		@BeforeEach
		void before() {
			bb = ByteBuffer.allocate(4);
		}

    	@Test
    	void integer() {
    		final var constant = new IntegerConstant(1);
    		assertEquals(4, constant.size());
    		constant.buffer(bb);
    		bb.flip();
    		assertEquals(1, bb.getInt());
    	}

    	@Test
    	void floating() {
    		final var constant = new FloatConstant(2);
    		assertEquals(4, constant.size());
    		constant.buffer(bb);
    		bb.flip();
    		assertEquals(2f, bb.getFloat());
    	}

    	@Test
    	void bool() {
    		final var constant = new BooleanConstant(true);
    		assertEquals(4, constant.size());
    		constant.buffer(bb);
    		bb.flip();
    		assertEquals(1, bb.getInt());
    	}
    }

	@Nested
	class BuilderTests {
		private SpecialisationConstants.Builder builder;

		@BeforeEach
		void before() {
			builder = new SpecialisationConstants.Builder();
		}

		@Test
		void add() {
			builder.add(1, new IntegerConstant(1));
			builder.build();
		}

		@Test
		void compound() {
			builder.add(constants);
			assertEquals(constants, builder.build());
		}
	}
}

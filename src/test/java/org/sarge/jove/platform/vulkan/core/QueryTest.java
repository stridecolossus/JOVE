package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.platform.vulkan.VkQueryResultFlag.WAIT;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Query.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Structure;

public class QueryTest extends AbstractVulkanTest {
	private Pool pool;
	private Command.Buffer buffer;

	@BeforeEach
	void before() {
		buffer = mock(Command.Buffer.class);
	}

	@Nested
	class DefaultQueryTests {
		private DefaultQuery query;

		@BeforeEach
		void before() {
			pool = Pool.create(dev, VkQueryType.OCCLUSION, 1);
			query = pool.query(0);
		}

		@Test
		void constructor() {
			assertNotNull(query);
		}

		@Test
		void begin() {
			final Command begin = query.begin(VkQueryControlFlag.PRECISE);
			assertNotNull(begin);
			begin.execute(lib, buffer);
			verify(lib).vkCmdBeginQuery(buffer, pool, 0, VkQueryControlFlag.PRECISE.value());
		}

		@Test
		void end() {
			final Command end = query.end();
			assertNotNull(end);
			end.execute(lib, buffer);
			verify(lib).vkCmdEndQuery(buffer, pool, 0);
		}
	}

	@Nested
	class TimestampTests {
		private Timestamp timestamp;

		@BeforeEach
		void before() {
			pool = Pool.create(dev, VkQueryType.TIMESTAMP, 1);
			timestamp = pool.timestamp(0);
		}

		@Test
		void timestamp() {
			final Command cmd = timestamp.timestamp(VkPipelineStage.VERTEX_SHADER);
			assertNotNull(timestamp);
			cmd.execute(lib, buffer);
			verify(lib).vkCmdWriteTimestamp(buffer, VkPipelineStage.VERTEX_SHADER, pool, 0);
		}
	}

	@Nested
	class PoolTest {
		@BeforeEach
		void before() {
			pool = Pool.create(dev, VkQueryType.OCCLUSION, 2);
		}

		@Test
		void constructor() {
			assertNotNull(pool);
			assertEquals(dev, pool.device());
			assertEquals(false, pool.isDestroyed());
			assertEquals(2, pool.slots());
		}

		@Test
		void create() {
			final var expected = new VkQueryPoolCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					return dataEquals((Structure) obj);
				}
			};
			expected.queryType = VkQueryType.OCCLUSION;
			expected.queryCount = 1;
			expected.pipelineStatistics = 0;
			verify(lib).vkCreateQueryPool(dev, expected, null, POINTER);
		}

		@Test
		void queryInvalidSlot() {
			assertThrows(IllegalArgumentException.class, () -> pool.query(2));
			assertThrows(IllegalArgumentException.class, () -> pool.query(-1));
		}

		@Test
		void reset() {
			final Command reset = pool.reset();
			assertNotNull(reset);
			reset.execute(lib, buffer);
			verify(lib).vkCmdResetQueryPool(buffer, pool, 0, 2);
		}

		@Test
		void resetInvalidRange() {
			assertThrows(IllegalArgumentException.class, () -> pool.reset(1, 2));
		}

		@Test
		void destroy() {
			pool.destroy();
			assertEquals(true, pool.isDestroyed());
			verify(lib).vkDestroyQueryPool(dev, pool, null);
		}
	}

	@Test
	void statistics() {
		pool = Pool.create(dev, VkQueryType.PIPELINE_STATISTICS, 1, VkQueryPipelineStatisticFlag.VERTEX_SHADER_INVOCATIONS);
		final var expected = new VkQueryPoolCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				return dataEquals((Structure) obj);
			}
		};
		expected.queryType = VkQueryType.PIPELINE_STATISTICS;
		expected.queryCount = 1;
		expected.pipelineStatistics = VkQueryPipelineStatisticFlag.VERTEX_SHADER_INVOCATIONS.value();
		verify(lib).vkCreateQueryPool(dev, expected, null, POINTER);
	}

	@Nested
	class ResultBuilderTest {
		private ResultBuilder builder;

		@BeforeEach
		void before() {
			pool = Pool.create(dev, VkQueryType.OCCLUSION, 2);
			builder = pool.result();
		}

		@Test
		void constructor() {
			assertNotNull(builder);
		}

		@Test
		void startInvalid() {
			assertThrows(IllegalArgumentException.class, () -> builder.start(2));
		}

		@Test
		void countInvalid() {
			assertThrows(IllegalArgumentException.class, () -> builder.count(3));
		}

		@Test
		void buildInvalidStride() {
			builder.stride(3);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Nested
		class AccessorTests {
			private Consumer<ByteBuffer> accessor;
			private ByteBuffer bb;

			@BeforeEach
			void before() {
				accessor = builder.flag(WAIT).build();
				bb = ByteBuffer.allocate(42);
			}

			@Test
			void constructor() {
				assertNotNull(accessor);
			}

			@Test
			void accept() {
				accessor.accept(bb);
				verify(lib).vkGetQueryPoolResults(dev, pool, 0, 2, bb.remaining(), bb, 4, WAIT.value());
			}

			@Test
			void acceptLongValues() {
				final int flags = IntegerEnumeration.reduce(VkQueryResultFlag.LONG, WAIT);
				builder.longs().build().accept(bb);
				verify(lib).vkGetQueryPoolResults(dev, pool, 0, 2, bb.remaining(), bb, 8, flags);
			}

			@Test
			void acceptInvalidBufferLength() {
				bb.position(bb.limit());
				assertThrows(IllegalStateException.class, () -> accessor.accept(bb));
			}
		}

		@Test
		void copy() {
			// Create buffer for results
			final VulkanBuffer dest = mock(VulkanBuffer.class);
			when(dest.length()).thenReturn(2 * 4L);

			// Build copy command
			final Command copy = builder.build(dest, 0);
			assertNotNull(copy);
			verify(dest).require(VkBufferUsageFlag.TRANSFER_DST);
			verify(dest).validate(8L);

			// Execute command
			copy.execute(lib, buffer);
			verify(lib).vkCmdCopyQueryPoolResults(buffer, pool, 0, 2, dest, 0, 4, 0);
		}
	}
}

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

	@DisplayName("A measurement query...")
	@Nested
	class DefaultQueryTests {
		private DefaultQuery query;

		@BeforeEach
		void before() {
			pool = Pool.create(dev, VkQueryType.OCCLUSION, 1);
			query = pool.query(0);
			assertNotNull(query);
		}

		@DisplayName("is started by a command wrapping a segment of the render sequence")
		@Test
		void begin() {
			final Command begin = query.begin(VkQueryControlFlag.PRECISE);
			assertNotNull(begin);
			begin.execute(lib, buffer);
			verify(lib).vkCmdBeginQuery(buffer, pool, 0, VkQueryControlFlag.PRECISE.value());
		}

		@DisplayName("is ended by a command wrapping a segment of the render sequence")
		@Test
		void end() {
			final Command end = query.end();
			assertNotNull(end);
			end.execute(lib, buffer);
			verify(lib).vkCmdEndQuery(buffer, pool, 0);
		}
	}

	@DisplayName("A timestamp query is performed by a command injected into the render sequence")
	@Test
	void timestamp() {
		pool = Pool.create(dev, VkQueryType.TIMESTAMP, 1);
		final Timestamp timestamp = pool.timestamp(0);
		final Command cmd = timestamp.timestamp(VkPipelineStage.VERTEX_SHADER);
		assertNotNull(timestamp);
		cmd.execute(lib, buffer);
		verify(lib).vkCmdWriteTimestamp(buffer, VkPipelineStage.VERTEX_SHADER, pool, 0);
	}

	@DisplayName("A query pool...")
	@Nested
	class PoolTest {
		@BeforeEach
		void before() {
			pool = Pool.create(dev, VkQueryType.OCCLUSION, 2);
			assertNotNull(pool);
			assertEquals(dev, pool.device());
			assertEquals(false, pool.isDestroyed());
			assertEquals(2, pool.slots());
		}

		@DisplayName("is created via the Vulkan API")
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
			verify(lib).vkCreateQueryPool(dev, expected, null, factory.pointer());
		}

		@DisplayName("cannot allocate more queries than the available number of slots")
		@Test
		void queryInvalidSlot() {
			assertThrows(IllegalArgumentException.class, () -> pool.query(2));
			assertThrows(IllegalArgumentException.class, () -> pool.query(-1));
		}

		@DisplayName("cannot specify pipeline statistics for other types of query")
		@Test
		void createInvalidPipelineStatistic() {
			assertThrows(IllegalArgumentException.class, () -> Pool.create(dev, VkQueryType.OCCLUSION, 1, VkQueryPipelineStatisticFlag.VERTEX_SHADER_INVOCATIONS));
		}

		@DisplayName("must specify at least one pipeline statisticquery")
		@Test
		void createEmptyPipelineStatistics() {
			assertThrows(IllegalArgumentException.class, () -> Pool.create(dev, VkQueryType.PIPELINE_STATISTICS, 1));
		}

		@DisplayName("can be reset")
		@Test
		void reset() {
			final Command reset = pool.reset();
			assertNotNull(reset);
			reset.execute(lib, buffer);
			verify(lib).vkCmdResetQueryPool(buffer, pool, 0, 2);
		}

		@DisplayName("cannot reset query slots that are out-of-range")
		@Test
		void resetInvalidRange() {
			assertThrows(IllegalArgumentException.class, () -> pool.reset(1, 2));
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			pool.destroy();
			assertEquals(true, pool.isDestroyed());
			verify(lib).vkDestroyQueryPool(dev, pool, null);
		}
	}

	@DisplayName("A pipeline statistics query has additional statistics flags")
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
		verify(lib).vkCreateQueryPool(dev, expected, null, factory.pointer());
	}

	@DisplayName("The results for a query...")
	@Nested
	class ResultBuilderTest {
		private ResultBuilder builder;

		@BeforeEach
		void before() {
			pool = Pool.create(dev, VkQueryType.OCCLUSION, 2);
			builder = pool.result();
			assertNotNull(builder);
		}

		@DisplayName("cannot specify a starting query slot that is out-of-range for the pool")
		@Test
		void startInvalid() {
			assertThrows(IllegalArgumentException.class, () -> builder.start(2));
		}

		@DisplayName("cannot specify a number of slots that is out-of-range for the pool")
		@Test
		void countInvalid() {
			assertThrows(IllegalArgumentException.class, () -> builder.count(3));
		}

		@DisplayName("cannot specify a results stride that is not a multiple of the specified data type")
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
				bb = ByteBuffer.allocate(42);
				accessor = builder.flag(WAIT).build();
				assertNotNull(accessor);
			}

			@DisplayName("can be copied to an NIO buffer on demand")
			@Test
			void accept() {
				accessor.accept(bb);
				verify(lib).vkGetQueryPoolResults(dev, pool, 0, 2, bb.remaining(), bb, 4, WAIT.value());
			}

			@DisplayName("can be configured as long values")
			@Test
			void acceptLongValues() {
				final int flags = IntegerEnumeration.reduce(VkQueryResultFlag.LONG, WAIT);
				builder.longs().build().accept(bb);
				verify(lib).vkGetQueryPoolResults(dev, pool, 0, 2, bb.remaining(), bb, 8, flags);
			}

			@DisplayName("cannot be copied to a buffer that is too small for the results")
			@Test
			void acceptInvalidBufferLength() {
				bb.position(bb.limit());
				assertThrows(IllegalStateException.class, () -> accessor.accept(bb));
			}
		}

		@DisplayName("can be copied to a Vulkan buffer")
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

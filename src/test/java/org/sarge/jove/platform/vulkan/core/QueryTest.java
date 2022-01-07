package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.platform.vulkan.VkQueryResultFlag.WAIT;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkQueryControlFlag;
import org.sarge.jove.platform.vulkan.VkQueryPipelineStatisticFlag;
import org.sarge.jove.platform.vulkan.VkQueryPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkQueryResultFlag;
import org.sarge.jove.platform.vulkan.VkQueryType;
import org.sarge.jove.platform.vulkan.core.Query.Pool;
import org.sarge.jove.platform.vulkan.core.Query.Pool.Builder;
import org.sarge.jove.platform.vulkan.core.Query.ResultBuilder;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;

public class QueryTest extends AbstractVulkanTest {
	private Pool pool;
	private Query query;
	private Command.Buffer cmd;

	@BeforeEach
	void before() {
		pool = new Pool(new Pointer(1), dev, 2);
		query = pool.query(0);
		cmd = mock(Command.Buffer.class);
	}

	@Test
	void constructor() {
		assertNotNull(query);
	}

	@Test
	void reset() {
		final Command reset = query.reset();
		assertNotNull(reset);
		reset.execute(lib, cmd);
		verify(lib).vkCmdResetQueryPool(cmd, pool, 0, 1);
	}

	@Test
	void begin() {
		final Command begin = query.begin(VkQueryControlFlag.PRECISE);
		assertNotNull(begin);
		begin.execute(lib, cmd);
		verify(lib).vkCmdBeginQuery(cmd, pool, 0, VkQueryControlFlag.PRECISE.value());
	}

	@Test
	void end() {
		final Command end = query.end();
		assertNotNull(end);
		end.execute(lib, cmd);
		verify(lib).vkCmdEndQuery(cmd, pool, 0);
	}

	@Test
	void timestamp() {
		final Command timestamp = query.timestamp(VkPipelineStage.VERTEX_SHADER);
		assertNotNull(timestamp);
		timestamp.execute(lib, cmd);
		verify(lib).vkCmdWriteTimestamp(cmd, VkPipelineStage.VERTEX_SHADER, pool, 0);
	}

	@Nested
	class PoolTest {
		@Test
		void constructor() {
			assertEquals(dev, pool.device());
			assertEquals(false, pool.isDestroyed());
			assertEquals(2, pool.slots());
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
			reset.execute(lib, cmd);
			verify(lib).vkCmdResetQueryPool(cmd, pool, 0, 2);
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

	@Nested
	class BuilderTest {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		@Test
		void build() {
			// Create pool
			pool = builder
					.type(VkQueryType.OCCLUSION)
					.slots(3)
					.build(dev);

			// Check pool
			assertNotNull(pool);
			assertEquals(dev, pool.device());
			assertEquals(false, pool.isDestroyed());
			assertEquals(3, pool.slots());

			// Init expected descriptor
			final var expected = new VkQueryPoolCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkQueryPoolCreateInfo) obj;
					assertNotNull(info);
					assertEquals(0, info.flags);
					assertEquals(VkQueryType.OCCLUSION, info.queryType);
					assertEquals(3, info.queryCount);
					assertEquals(0, info.pipelineStatistics);
					return true;
				}
			};

			// Check API
			verify(lib).vkCreateQueryPool(dev, expected, null, POINTER);
		}

		@Test
		void buildPipelineStatistics() {
			// Create pool for pipeline statistics
			pool = builder
					.type(VkQueryType.PIPELINE_STATISTICS)
					.statistic(VkQueryPipelineStatisticFlag.CLIPPING_PRIMITIVES)
					.build(dev);

			// Init expected descriptor
			final var expected = new VkQueryPoolCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkQueryPoolCreateInfo) obj;
					assertEquals(VkQueryType.PIPELINE_STATISTICS, info.queryType);
					assertEquals(VkQueryPipelineStatisticFlag.CLIPPING_PRIMITIVES.value(), info.pipelineStatistics);
					return true;
				}
			};

			// Check API
			verify(lib).vkCreateQueryPool(dev, expected, null, POINTER);
		}

		@Test
		void buildMissingQueryType() {
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}

		@Test
		void buildEmptyPipelineStatistics() {
			builder.type(VkQueryType.PIPELINE_STATISTICS);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}

		@Test
		void buildIllogicalPipelineStatistics() {
			builder.type(VkQueryType.OCCLUSION);
			builder.statistic(VkQueryPipelineStatisticFlag.CLIPPING_PRIMITIVES);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}
	}

	@Nested
	class ResultBuilderTest {
		private ResultBuilder builder;

		@BeforeEach
		void before() {
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
		void buildInvalidRange() {
			builder.start(1);
			builder.count(2);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
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
				final int flags = IntegerEnumeration.mask(VkQueryResultFlag.LONG, WAIT);
				builder.longs().build().accept(bb);
				verify(lib).vkGetQueryPoolResults(dev, pool, 0, 2, bb.remaining(), bb, 8, flags);
			}

			@Test
			void acceptInvalidBufferLength() {
				bb.position(bb.limit());
				assertThrows(IllegalStateException.class, () -> accessor.accept(bb));
			}
		}

		@Nested
		class CopyBufferTests {
			private VulkanBuffer buffer;

			@BeforeEach
			void before() {
				buffer = mock(VulkanBuffer.class);
				when(buffer.length()).thenReturn(2 * 4L);
			}

			@Test
			void build() {
				// Build copy command
				final Command copy = builder.build(buffer, 0);
				assertNotNull(copy);
				verify(buffer).require(VkBufferUsageFlag.TRANSFER_DST);

				// Execute command
				copy.execute(lib, cmd);
				verify(lib).vkCmdCopyQueryPoolResults(cmd, pool, 0, 2, buffer, 0, 4, 0);
			}

			@Test
			void buildInvalidBufferLength() {
				when(buffer.length()).thenReturn(0L);
				assertThrows(IllegalStateException.class, () -> builder.build(buffer, 0));
			}

			@Test
			void buildInvalidBufferOffset() {
				assertThrows(IllegalStateException.class, () -> builder.build(buffer, 1));
			}
		}
	}
}

package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.MemorySegment;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Query.*;
import org.sarge.jove.util.EnumMask;

class QueryTest {
	private static class MockQueryLibrary implements Library {
		boolean reset;
		boolean start, end;
		boolean write;
		boolean destroyed;

		@Override
		public VkResult vkCreateQueryPool(LogicalDevice device, VkQueryPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pQueryPool) {
			assertEquals(VkStructureType.QUERY_POOL_CREATE_INFO, pCreateInfo.sType);
			assertEquals(0, pCreateInfo.flags);
			if(pCreateInfo.queryType == VkQueryType.PIPELINE_STATISTICS) {
				assertEquals(new EnumMask<>(VkQueryPipelineStatisticFlags.VERTEX_SHADER_INVOCATIONS), pCreateInfo.pipelineStatistics);
			}
			else {
				assertEquals(VkQueryType.OCCLUSION, pCreateInfo.queryType);
				assertEquals(new EnumMask<>(), pCreateInfo.pipelineStatistics);
			}
			assertEquals(2, pCreateInfo.queryCount);
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkDestroyQueryPool(LogicalDevice device, Pool queryPool, Handle pAllocator) {
			destroyed = true;
		}

		@Override
		public void vkCmdResetQueryPool(Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount) {
			assertEquals(0, firstQuery);
			assertEquals(2, queryCount);
			reset = true;
		}

		@Override
		public void vkCmdBeginQuery(Buffer commandBuffer, Pool queryPool, int query, EnumMask<VkQueryControlFlags> flags) {
			assertEquals(1, query);
			assertEquals(new EnumMask<>(VkQueryControlFlags.PRECISE), flags);
			start = true;
		}

		@Override
		public void vkCmdEndQuery(Buffer commandBuffer, Pool queryPool, int query) {
			assertEquals(1, query);
			end = true;
		}

		@Override
		public void vkCmdWriteTimestamp(Buffer commandBuffer, VkPipelineStageFlags pipelineStage, Pool queryPool, int query) {
			assertEquals(VkPipelineStageFlags.VERTEX_SHADER, pipelineStage);
			assertEquals(1, query);
			write = true;
		}

		@Override
		public VkResult vkGetQueryPoolResults(LogicalDevice device, Pool queryPool, int firstQuery, int queryCount, long dataSize, MemorySegment pData, long stride, EnumMask<VkQueryResultFlags> flags) {
			// TODO
			return null;
		}

		@Override
		public void vkCmdCopyQueryPoolResults(Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount, VulkanBuffer dstBuffer, long dstOffset, long stride, EnumMask<VkQueryResultFlags> flags) {
			assertEquals(0, firstQuery);
			assertEquals(2, queryCount);
			assertEquals(0L, dstOffset);
			assertEquals(8L, stride);
			assertEquals(new EnumMask<>(VkQueryResultFlags.RESULT_64), flags);

			dstBuffer.buffer().putLong(3).putLong(4);
		}
	}

	private Pool pool;
	private LogicalDevice device;
	private MockQueryLibrary library;

	@BeforeEach
	void before() {
		library = new MockQueryLibrary();
		device = new MockLogicalDevice();
		pool = new Pool(new Handle(1), device, VkQueryType.OCCLUSION, 2, library);
	}

	// TODO - create

	@Test
	void reset() {
		final Command reset = pool.reset();
		reset.execute(null);
		assertEquals(true, library.reset);
	}

	@Nested
	class MeasurementTest {
		private Query query;

		@BeforeEach
		void before() {
			query = pool.measurement(1);
		}

		@Test
		void begin() {
			final Command begin = query.begin(VkQueryControlFlags.PRECISE);
			begin.execute(null);
			assertEquals(true, library.start);
		}

		@Test
		void end() {
			final Command end = query.end();
			end.execute(null);
			assertEquals(true, library.end);
		}
	}

	@Test
	void timestamp() {
		final Pool pool = new Pool(new Handle(1), device, VkQueryType.TIMESTAMP, 2, library);
		final Command timestamp = pool.timestamp(1, VkPipelineStageFlags.VERTEX_SHADER);
		timestamp.execute(null);
		assertEquals(true, library.write);
	}

	@Test
	void results() {
		final var results = new QueryResult(pool, Set.of(VkQueryResultFlags.RESULT_64));
		final var buffer = new MockVulkanBuffer(new MockLogicalDevice(), 16L, VkBufferUsageFlags.TRANSFER_DST);
		results.buffer(buffer, 0L);
		// TODO
	}

	@Test
	void destroy() {
		pool.destroy();
		assertEquals(true, pool.isDestroyed());
		assertEquals(true, library.destroyed);
	}
}

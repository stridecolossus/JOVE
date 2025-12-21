package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Query.Pool;
import org.sarge.jove.util.*;

class QueryTest {
	private static class MockQueryLibrary extends MockLibrary implements Query.Library {
		@Override
		public VkResult vkCreateQueryPool(LogicalDevice device, VkQueryPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pQueryPool) {
			assertEquals(VkStructureType.QUERY_POOL_CREATE_INFO, pCreateInfo.sType);
			assertEquals(0, pCreateInfo.flags);
			if(pCreateInfo.queryType == VkQueryType.PIPELINE_STATISTICS) {
				assertEquals(new EnumMask<>(VkQueryPipelineStatisticFlags.VERTEX_SHADER_INVOCATIONS), pCreateInfo.pipelineStatistics);
			}
			else {
				assertEquals(new EnumMask<>(), pCreateInfo.pipelineStatistics);
			}
			assertEquals(2, pCreateInfo.queryCount);
			init(pQueryPool);
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkCmdResetQueryPool(Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount) {
			assertEquals(0, firstQuery);
			assertEquals(2, queryCount);
		}

		@Override
		public void vkCmdBeginQuery(Buffer commandBuffer, Pool queryPool, int query, EnumMask<VkQueryControlFlags> flags) {
			assertEquals(1, query);
			assertEquals(new EnumMask<>(VkQueryControlFlags.PRECISE), flags);
		}

		@Override
		public void vkCmdEndQuery(Buffer commandBuffer, Pool queryPool, int query) {
			assertEquals(1, query);
		}

		@Override
		public void vkCmdWriteTimestamp(Buffer commandBuffer, VkPipelineStageFlags pipelineStage, Pool queryPool, int query) {
			assertEquals(VkPipelineStageFlags.VERTEX_SHADER, pipelineStage);
			assertEquals(1, query);
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
		}

		@Override
		public void vkDestroyQueryPool(LogicalDevice device, Pool queryPool, Handle pAllocator) {
			// Empty
		}
	}

	private Pool pool;
	private LogicalDevice device;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(Query.Library.class);
		mockery.implement(new MockQueryLibrary());
		device = new MockLogicalDevice(mockery.proxy());
		pool = Pool.create(device, VkQueryType.OCCLUSION, 2);
	}

	@Test
	void constructor() {
		assertEquals(2, pool.slots());
	}

	// TODO - pipeline statistics

	@Test
	void reset() {
		final Command reset = pool.reset();
		reset.execute(null);
		assertEquals(1, mockery.mock("vkCmdResetQueryPool").count());
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
			assertEquals(1, mockery.mock("vkCmdBeginQuery").count());
		}

		@Test
		void end() {
			final Command end = query.end();
			end.execute(null);
			assertEquals(1, mockery.mock("vkCmdEndQuery").count());
		}
	}

	@Test
	void timestamp() {
		final Pool pool = Pool.create(device, VkQueryType.TIMESTAMP, 2);
		final Command timestamp = pool.timestamp(1, VkPipelineStageFlags.VERTEX_SHADER);
		timestamp.execute(null);
		assertEquals(1, mockery.mock("vkCmdWriteTimestamp").count());
	}

	@Test
	void results() {
//		final var results = new QueryResult(pool, Set.of(VkQueryResultFlags.RESULT_64));
//		final var buffer = new MockVulkanBuffer(new MockLogicalDevice(), 16L, VkBufferUsageFlags.TRANSFER_DST);
//		results.buffer(buffer, 0L);
		// TODO
	}

	@Test
	void destroy() {
		pool.destroy();
		assertEquals(true, pool.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyQueryPool").count());
	}
}

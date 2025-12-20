package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.*;

import java.lang.foreign.MemorySegment;
import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>query</i> is used to retrieve statistics from Vulkan during a render pass.
 * <p>
 * Generally a query is comprised of two commands that wrap a portion of a render sequence, e.g. to perform an {@link VkQueryType#OCCLUSION} query.
 * <p>
 * Queries are allocated as <i>slots</i> from a {@link Pool} created via the {@link Pool#create(LogicalDevice, VkQueryType, int, VkQueryPipelineStatisticFlag...)} factory method.
 * Note that a query pool <b>must</b> be reset before each sample <b>outside</b> the render pass.
 * <p>
 * The {@link QueryResult} configures the mechanism to retrieve the results of a query.
 * <p>
 * Example for an occlusion query:
 * <p>
 * {@snippet :
 * // Create an occlusion query pool with two slots
 * LogicalDevice device = ...
 * Pool pool = Pool.create(device, VkQueryType.OCCLUSION, 2);
 *
 * // Allocate a query for the second slot
 * DefaultQuery query = pool.query(1);
 *
 * // Instrument the render sequence with the query
 * FrameBuffer frame = ...
 * Command.Buffer buffer = ...
 * buffer								// @highlight region substring="query" type=highlighted
 *     .add(query.reset())
 *     .add(framebuffer.begin())
 *         .add(query.begin())
 *             ...
 *         .add(query.end())
 *     .add(framebuffer,end());			// @end
 *
 * // Retrieve the query results
 * QueryResult results = new QueryResults(pool);
 * MemorySegment memory = ...
 * results.get(memory);
 * }
 * <p>
 * @author Sarge
 */
public interface Query {
	/**
	 * Creates a command to begin a measurement query.
	 * @param flags Control flags
	 * @return Begin command
	 * @throws IllegalStateException if this query is not {@link State#UNAVAILABLE}
	 * @see Pool#reset()
	 */
	Command begin(VkQueryControlFlags... flags);

	/**
	 * Creates a command to end a measurement query.
	 * @return End command
	 * @throws IllegalStateException if this query is not {@link State#ACTIVE}
	 */
    Command end();

	/**
	 * A <i>query pool</i> allocates measurement and timestamp queries from an array of available <i>slots</i>.
	 */
	class Pool extends VulkanObject {
		/**
		 * Creates a query pool.
		 * @param device			Logical device
		 * @param type				Query type
		 * @param slots				Number of slots
		 * @param statistics		Pipeline statistics for a {@link VkQueryType#PIPELINE_STATISTICS} query
		 * @return Query pool
		 * @throws IllegalArgumentException if {@link #statistics} is empty for a {@link VkQueryType#PIPELINE_STATISTICS} query
		 */
		public static Pool create(LogicalDevice device, VkQueryType type, int slots, VkQueryPipelineStatisticFlags... statistics) {
			// Validate
			requireOneOrMore(slots);
			if((type == VkQueryType.PIPELINE_STATISTICS) ^ (statistics.length > 0)) {
				throw new IllegalArgumentException();
			}

			// Populate create descriptor
			final var info = new VkQueryPoolCreateInfo();
			info.sType = VkStructureType.QUERY_POOL_CREATE_INFO;
			info.queryType = type;
			info.queryCount = slots;
			info.pipelineStatistics = new EnumMask<>(statistics);

			// Create query pool
			final Library library = device.library();
			final var pointer = new Pointer();
			library.vkCreateQueryPool(device, info, null, pointer);

			// Init query pool
			return new Pool(pointer.handle(), device, type, slots, library);
		}

		private final VkQueryType type;
		private final int slots;
		private final Library library;

		/**
		 * Constructor.
		 * @param handle		Query pool handle
		 * @param device		Logical device
		 * @param type			Query type
		 * @param slots			Number of slots
		 * @param library		Query pool API
		 */
		Pool(Handle handle, LogicalDevice device, VkQueryType type, int slots, Library library) {
			super(handle, device);
			this.type = requireNonNull(type);
			this.slots = requireOneOrMore(slots);
			this.library = requireNonNull(library);
		}

		/**
		 * @return Number of slots
		 */
		public int slots() {
			return slots;
		}

		/**
		 * @throws IndexOutOfBoundsException for an invalid slot index
		 */
		private void validate(int slot) {
    		if((slot < 0) || (slot > slots)) {
    			throw new IndexOutOfBoundsException();
    		}
    	}

		/**
		 * Creates a command to reset a range of query results.
		 * The relevant queries are set to the {@link State#UNAVAILABLE} state.
		 * @param start			Starting slot
		 * @param number		Number of slots to reset
		 * @throws IndexOutOfBoundsException if {@link #start} or {@link #slots} is invalid for this pool
		 */
		public Command reset(int start, int number) {
			validate(start);
			validate(start + number);
			return buffer -> library.vkCmdResetQueryPool(buffer, this, start, number);
		}

		/**
		 * Creates a command to reset <b>all</b> slots in this pool.
		 * @see #reset(int, int)
		 */
		public Command reset() {
			return reset(0, slots);
		}

		/**
		 * Creates a measurement query.
		 * @param slot Query slot
		 * @return Measurement query
		 * @throws IndexOutOfBoundsException if {@link #slot} is invalid for this pool
		 * @throws IllegalStateException if this is a {@link VkQueryType#TIMESTAMP} pool
		 */
		public Query measurement(int slot) {
			validate(slot);
			if(type == VkQueryType.TIMESTAMP) {
				throw new IllegalArgumentException();
			}

			return new Query() {
				@Override
				public Command begin(VkQueryControlFlags... flags) {
					final var mask = new EnumMask<>(flags);
			    	return buffer -> library.vkCmdBeginQuery(buffer, Pool.this, slot, mask);
				}

				@Override
				public Command end() {
					return buffer -> library.vkCmdEndQuery(buffer, Pool.this, slot);
				}
			};
		}

		/**
		 * Creates a timestamp command.
		 * @param slot		Query slot
		 * @param stage		Pipeline stage
		 * @return Timestamp
		 * @throws IndexOutOfBoundsException if {@link #slot} is invalid for this pool
		 * @throws IllegalStateException if this is not a {@link VkQueryType#TIMESTAMP} pool
		 */
		public Command timestamp(int slot, VkPipelineStageFlags stage) {
			requireNonNull(stage);
			validate(slot);
			if(type != VkQueryType.TIMESTAMP) {
				throw new IllegalArgumentException();
			}

			return buffer -> library.vkCmdWriteTimestamp(buffer, stage, this, slot);
		}

		@Override
		protected Destructor<Pool> destructor() {
			return library::vkDestroyQueryPool;
		}
	}

	/**
	 * The <i>query result</i> configures the retrieval of query results.
	 * <p>
	 * Query results can either be copied directly to memory on-demand using {@link QueryResult#get(MemorySegment)}
	 * or retrieved asynchronously to a buffer via a {@link QueryResult#buffer(VulkanBuffer, long)} command.
	 */
	record QueryResult(Pool pool, int start, int slots, Set<VkQueryResultFlags> flags) {
		/**
		 * Constructor.
		 * @param pool		Query pool
		 * @param start		Starting slot
		 * @param slots		Number of slots
		 * @param flags		Flags
		 * @throws IndexOutOfBoundsException if the number of slots is invalid for the pool
		 */
		public QueryResult {
			requireNonNull(pool);
			requireZeroOrMore(start);
			requireOneOrMore(slots);
			pool.validate(start + slots - 1);
			flags = Set.copyOf(flags);
		}

		/**
		 * Convenience constructor for all slots in the given slots.
		 * @param pool		Query pool
		 * @param flags		Flags
		 */
		public QueryResult(Pool pool, Set<VkQueryResultFlags> flags) {
			this(pool, 0, pool.slots(), flags);
		}

		/**
		 * @return Results stride (bytes)
		 */
		public long stride() {
			if(flags.contains(VkQueryResultFlags.RESULT_64)) {
				return Long.BYTES;
			}
			else {
				return Integer.BYTES;
			}
		}

		/**
		 * TODO - MemorySegment transformer
		 * Retrieves query results to the given memory.
		 * @param results Results memory
		 */
		public void get(MemorySegment results) {
			final long stride = this.stride();
			final long length = slots * stride;
			if(results.byteSize() < length) {
				throw new IllegalArgumentException("Insufficient results size: " + results);
			}

			final var mask = new EnumMask<>(flags);

			pool.library.vkGetQueryPoolResults(
					pool.device(),
					pool,
					start,
					slots,
					length,
					results,
					stride,
					mask
			);
		}

		/**
		 * Creates a command that asynchronously copies query results to the given buffer.
		 * @param buffer		Results buffer
		 * @param offset		Buffer offset
		 * @return Asynchronous results command
		 * @throws IllegalStateException if the buffer is not a {@link VkBufferUsageFlags#TRANSFER_DST}
		 * @throws IllegalArgumentException if the buffer is too small for the results
		 */
		public Command buffer(VulkanBuffer buffer, long offset) {
			final long stride = this.stride();
			requireZeroOrMore(offset);
			buffer.require(VkBufferUsageFlags.TRANSFER_DST);
			buffer.checkOffset(offset - 1 + slots * stride);

			final var mask = new EnumMask<>(flags);

			return commandBuffer -> pool.library.vkCmdCopyQueryPoolResults(
					commandBuffer,
					pool,
					start,
					slots,
					buffer,
					offset,
					stride,
					mask
			);
		}
	}

	/**
	 * Query API.
	 */
	interface Library {
		/**
		 * Creates a query pool.
		 * @param device			Logical device
		 * @param pCreateInfo		Create descriptor
		 * @param pAllocator		Allocator
		 * @param pQueryPool		Returned query pool handle
		 */
		VkResult vkCreateQueryPool(LogicalDevice device, VkQueryPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pQueryPool);

		/**
		 * Destroys a query pool.
		 * @param device			Logical device
		 * @param queryPool			Query pool to destroy
		 * @param pAllocator		Allocator
		 */
		void vkDestroyQueryPool(LogicalDevice device, Pool queryPool, Handle pAllocator);

		/**
		 * Command to reset a query pool.
		 * @param commandBuffer		Command buffer
		 * @param queryPool			Query pool
		 * @param firstQuery		Index of the first query slot
		 * @param queryCount		Number of query slots to reset
		 */
		void vkCmdResetQueryPool(Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount);

		/**
		 * Starts a query.
		 * @param commandBuffer		Command buffer
		 * @param queryPool			Query pool
		 * @param query				Query slot
		 * @param flags				Flags
		 */
		void vkCmdBeginQuery(Buffer commandBuffer, Pool queryPool, int query, EnumMask<VkQueryControlFlags> flags);

		/**
		 * Ends a query.
		 * @param commandBuffer		Command buffer
		 * @param queryPool			Query pool
		 * @param query				Query slot
		 */
		void vkCmdEndQuery(Buffer commandBuffer, Pool queryPool, int query);

		/**
		 * Writes the device timestamp at the given pipeline stage to the query results.
		 * @param commandBuffer		Command buffer
		 * @param pipelineStage		Pipeline stage(s) mask
		 * @param queryPool			Query pool
		 * @param query				Query slot
		 */
		void vkCmdWriteTimestamp(Buffer commandBuffer, VkPipelineStageFlags pipelineStage, Pool queryPool, int query);

		/**
		 * Retrieves query results.
		 * @param device			Logical device
		 * @param queryPool			Query pool
		 * @param firstQuery		Index of the first query slot
		 * @param queryCount		Number of query slots to retrieve
		 * @param dataSize			Size of the data buffer
		 * @param pData				Data buffer
		 * @param stride			Data stride (bytes)
		 * @param flags				Query flags
		 */
		VkResult vkGetQueryPoolResults(LogicalDevice device, Pool queryPool, int firstQuery, int queryCount, long dataSize, MemorySegment pData, long stride, EnumMask<VkQueryResultFlags> flags);
		// TODO - MemorySegment transformer

		/**
		 * Writes query results to a Vulkan buffer.
		 * @param commandBuffer		Command buffer
		 * @param queryPool			Query pool
		 * @param firstQuery		Index of the first query slot
		 * @param queryCount		Number of query slots to retrieve
		 * @param dstBuffer			Results buffer
		 * @param dstOffset			Offset
		 * @param stride			Data stride (bytes)
		 * @param flags				Query flags
		 */
		void vkCmdCopyQueryPoolResults(Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount, VulkanBuffer dstBuffer, long dstOffset, long stride, EnumMask<VkQueryResultFlags> flags);
	}
}

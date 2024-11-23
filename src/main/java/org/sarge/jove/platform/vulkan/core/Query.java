package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.PointerReference;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.BitMask;

/**
 * A <i>query</i> is used to retrieve statistics from Vulkan during a render pass.
 * <p>
 * Generally a query is comprised of two commands that wrap a portion of a render sequence, e.g. to perform an {@link VkQueryType#OCCLUSION} query.
 * <p>
 * Queries are allocated as <i>slots</i> from a {@link Pool} created via the {@link Pool#create(DeviceContext, VkQueryType, int, VkQueryPipelineStatisticFlag...)} factory method.
 * Note that a query pool <b>must</b> be reset before each sample <b>outside</b> the render pass.
 * <p>
 * The {@link ResultBuilder} is used to configure the results of a query and to write the data to a destination buffer.
 * <p>
 * Example for an occlusion query:
 * <p>
 * {@snippet :
 * // Create an occlusion query pool with two slots
 * LogicalDevice dev = ...
 * Pool pool = Pool.create(dev, VkQueryType.OCCLUSION, 2);
 *
 * // Allocate a query for the second slot
 * DefaultQuery query = pool.query(1);
 *
 * // Instrument the render sequence with the query
 * FrameBuffer frame = ...
 * Command.Buffer buffer = ...
 * buffer								// @highlight region substring="query" type=highlighted
 *     .add(query.reset())
 *     .add(frame.begin())
 *         .add(query.begin())
 *             ...
 *         .add(query.end())
 *     .add(FrameBuffer#END);			// @end
 *
 * // Retrieve the query results
 * ByteBuffer results = ...
 * pool.result().build().accept(results);
 * }
 * <p>
 * @author Sarge
 */
public interface Query {
	/**
	 * Convenience method to create a command to reset this query.
	 * @return Reset command
	 * @see Pool#reset(int, int)
	 */
	Command reset();

	/**
	 * Default implementation for a measurement query wrapping a portion of the render sequence.
	 */
	interface DefaultQuery extends Query {
		/**
		 * Creates a command to begin this query.
		 * @param flags Control flags
		 * @return Begin query command
		 */
		Command begin(VkQueryControlFlag... flags);

		/**
		 * Creates a command to end this query.
		 * @return End query command
		 */
		Command end();
	}

	/**
	 * Timestamp query.
	 */
	interface Timestamp extends Query {
		/**
		 * Creates a command to write the device timestamp at the given pipeline stage.
		 * @param stage Pipeline stage
		 * @return Timestamp command
		 */
		Command timestamp(VkPipelineStage stage);
	}

	/**
	 * A <i>query pool</i> is comprised of a number of <i>slots</i> used to execute queries.
	 */
	final class Pool extends VulkanObject {
		/**
		 * Creates a query pool.
		 * @param dev		Device
		 * @param type		Query type
		 * @param slots		Number of slots
		 * @param stats		Pipeline statistics to gather for a {@link VkQueryType#PIPELINE_STATISTICS} query
		 * @return New query pool
		 * @throws IllegalArgumentException for a pipeline statistics query with an empty set of flags
		 */
		public static Pool create(DeviceContext dev, VkQueryType type, int slots, VkQueryPipelineStatisticFlag... stats) {
			// Validate
			if((type == VkQueryType.PIPELINE_STATISTICS) != (stats.length > 0)) {
				throw new IllegalArgumentException("Empty or superfluous pipeline statistics");
			}

			// Init create descriptor
			final var info = new VkQueryPoolCreateInfo();
			info.queryType = requireNonNull(type);
			info.queryCount = requireOneOrMore(slots);
			info.pipelineStatistics = BitMask.of(stats);

			// Instantiate query pool
			final Vulkan vulkan = dev.vulkan();
			final PointerReference ref = vulkan.factory().pointer();
			vulkan.library().vkCreateQueryPool(dev, info, null, ref);

			// Create pool
			return new Pool(ref.handle(), dev, type, slots);
		}

		private final VkQueryType type;
		private final int slots;

		/**
		 * Constructor.
		 * @param handle		Handle
		 * @param dev			Logical device
		 * @param type			Query type
		 * @param slots			Number of query slots
		 */
		Pool(Handle handle, DeviceContext dev, VkQueryType type, int slots) {
			super(handle, dev);
			this.type = requireNonNull(type);
			this.slots = requireOneOrMore(slots);
		}

		/**
		 * @return Number of slots in this pool
		 */
		public int slots() {
			return slots;
		}

		/**
		 * @throws IllegalArgumentException if the slot is invalid for this pool
		 */
		private void validate(int slot) {
			requireZeroOrMore(slot);
			if(slot >= slots) throw new IllegalArgumentException(String.format("Invalid query slot: slot=%d pool=%s", slot, this));
		}

		/**
		 * Creates a query.
		 * @param slot Query slot
		 * @return New query
		 * @throws IllegalArgumentException if the slot is invalid for this pool
		 */
		public DefaultQuery query(int slot) {
			validate(slot);
			return new DefaultQuery() {
				@Override
				public Command reset() {
					return Pool.this.reset(slot, 1);
				}

				@Override
				public Command begin(VkQueryControlFlag... flags) {
					final BitMask<VkQueryControlFlag> mask = BitMask.of(flags);
					return (lib, buffer) -> lib.vkCmdBeginQuery(buffer, Pool.this, slot, mask);
				}

				@Override
				public Command end() {
					return (lib, buffer) -> lib.vkCmdEndQuery(buffer, Pool.this, slot);
				}
			};
		}

		/**
		 * Creates a timestamp.
		 * @param slot Query slot
		 * @return New timestamp
		 * @throws IllegalArgumentException if the slot is invalid for this pool
		 */
		public Timestamp timestamp(int slot) {
			validate(slot);
			return new Timestamp() {
				@Override
				public Command reset() {
					return Pool.this.reset(slot, 1);
				}

				@Override
				public Command timestamp(VkPipelineStage stage) {
					requireNonNull(stage);
					return (lib, buffer) -> lib.vkCmdWriteTimestamp(buffer, stage, Pool.this, slot);
				}
			};
		}

		/**
		 * Creates a reset command a segment of this pool.
		 * @param start		Starting slot
		 * @param num		Number of slots
		 * @return Reset command
		 * @throws IllegalArgumentException if the given range is out-of-bounds for this pool
		 */
		public Command reset(int start, int num) {
			requireZeroOrMore(start);
			validate(start + num - 1);
			return (lib, buffer) -> lib.vkCmdResetQueryPool(buffer, this, start, num);
		}

		/**
		 * Convenience factory to create a reset command for <b>all</b> slots in this pool.
		 * @return Reset command
		 * @see #reset(int, int)
		 */
		public Command reset() {
			return reset(0, slots);
		}

		/**
		 * Creates a result builder for this query pool.
		 * @return New result builder
		 */
		public ResultBuilder result() {
			return new ResultBuilder(this);
		}

		@Override
		protected Destructor<Pool> destructor(VulkanLibrary lib) {
			return lib::vkDestroyQueryPool;
		}
	}

	/**
	 * A <i>result builder</i> is used to configure and retrieve the results of a query.
	 * <p>
	 * The results are written to a data buffer which is essentially an array divided into the configured number of {@link ResultBuilder#stride(long)} bytes.
	 * <p>
	 * Note that by default the results have an <b>integer</b> data type with contiguous values.
	 * The {@link VkQueryResultFlag#LONG} flag is used to specify a query with a {@code long} data type.
	 * Additionally the convenience {@link ResultBuilder#longs()} method configures a query result for {@code long} values with an appropriate stride.
	 * <p>
	 * Note this builder provides <b>two</b> build methods variants:
	 * <ul>
	 * <li>{@link ResultBuilder#build()} creates an accessor to retrieve query results to an on-demand consumer</li>
	 * <li>{@link ResultBuilder#build(VulkanBuffer, long)} creates a command that asynchronously writes the results to a destination Vulkan buffer</li>
	 * </ul>
	 * <p>
	 * Example usage for an on-demand buffer:
	 * <pre>
	 * // Create a result builder
	 * ResultBuilder builder = pool.result();
	 *
	 * // Construct the accessor
	 * Consumer&lt;ByteBuffer&gt; accessor = builder
	 * 	.start(1)
	 * 	.count(2)
	 * 	.flag(VkQueryResultFlag.WAIT)
	 * 	.build();
	 *
	 * // Copy query results to a buffer
	 * ByteBuffer bb = ...
	 * accessor.accept(bb);
	 * </pre>
	 */
	class ResultBuilder {
		private final Pool pool;
		private int start;
		private int count;
		private long stride = Integer.BYTES;
		private final Set<VkQueryResultFlag> flags = new HashSet<>();

		private ResultBuilder(Pool pool) {
			this.pool = pool;
			this.count = pool.slots;
		}

		/**
		 * Sets the starting slot (default is the <i>first</i> slot).
		 * @param start Starting slot
		 * @throws IllegalArgumentException if {@code start} exceeds the number of query slots
		 */
		public ResultBuilder start(int start) {
			pool.validate(start);
			this.start = requireZeroOrMore(start);
			return this;
		}

		/**
		 * Sets the number of query slots to retrieve (default is <b>all</b> slots in this pool).
		 * @param count Number of slots
		 * @throws IllegalArgumentException if {@code count} exceeds the number of query slots
		 */
		public ResultBuilder count(int count) {
			pool.validate(count);
			this.count = requireOneOrMore(count);
			return this;
		}

		/**
		 * Sets the stride between results within the buffer (default is {@link Integer#BYTES}).
		 * @param stride Results stride (bytes)
		 */
		public ResultBuilder stride(long stride) {
			this.stride = requireOneOrMore(stride);
			return this;
		}

		/**
		 * Adds a flag to this query.
		 * @param flag Query flag
		 */
		public ResultBuilder flag(VkQueryResultFlag flag) {
			flags.add(requireNonNull(flag));
			return this;
		}

		/**
		 * Convenience method to configure this builder to retrieve {@code long} query results with a stride of {@link Long#BYTES}.
		 */
		public ResultBuilder longs() {
			flag(VkQueryResultFlag.LONG);
			stride(Long.BYTES);
			return this;
		}

		/**
		 * Constructs an accessor that retrieves the query results on-demand.
		 * @return Query results accessor
		 * @throws IllegalArgumentException if the query range is invalid for this pool
		 * @throws IllegalArgumentException if the stride is not a multiple of the data type of this query
		 * @throws IllegalArgumentException if the buffer is too small for this query
		 */
		public Consumer<ByteBuffer> build() {
			// Validate query result
			final BitMask<VkQueryResultFlag> mask = validate();

			// Init library
			final DeviceContext dev = pool.device();
			final Library lib = dev.vulkan().library();

			// Create accessor
			return buffer -> {
				// Validate buffer
				final int size = buffer.remaining();
				if(count * stride > size) throw new IllegalStateException(String.format("Insufficient buffer space for query: query=%s buffer=%s", ResultBuilder.this, buffer));
				pool.validate(start + count - 1);

				// Execute query
				lib.vkGetQueryPoolResults(dev, pool, start, count, size, buffer, stride, mask);
			};
		}

		/**
		 * Constructs a command that asynchronously copies query results to the given buffer.
		 * @param buffer 		Vulkan buffer
		 * @param offset		Buffer offset
		 * @return Query results command
		 * @throws IllegalArgumentException if the query range is invalid for this pool
		 * @throws IllegalArgumentException if the stride is not a multiple of the data type of this query
		 * @throws IllegalStateException if the {@link #offset} is invalid for the given buffer
		 * @throws IllegalStateException if {@link #buffer} is not a {@link VkBufferUsageFlag#TRANSFER_DST}
		 */
		public Command build(VulkanBuffer buffer, long offset) {
			// Validate buffer
			requireNonNull(buffer);
			requireZeroOrMore(offset);
			buffer.require(VkBufferUsageFlag.TRANSFER_DST);
			buffer.checkOffset(offset - 1 + count * stride);

			// Validate query result
			final BitMask<VkQueryResultFlag> mask = validate();

			// Create results command
			return (lib, cmd) -> {
				// TODO - rewind?
				lib.vkCmdCopyQueryPoolResults(cmd, pool, start, count, buffer, offset, stride, mask);
			};
		}

		/**
		 * Validates this query result.
		 * @return Flags bit-field
		 */
		private BitMask<VkQueryResultFlag> validate() {
			// Validate query range
			if(start + count > pool.slots) {
				throw new IllegalArgumentException(String.format("Invalid query slot range: start=%d count=%d pool=%s", start, count, pool));
			}

			// Validate stride
			final long multiple = flags.contains(VkQueryResultFlag.LONG) ? Long.BYTES : Integer.BYTES;
			if((stride % multiple) != 0) {
				throw new IllegalArgumentException(String.format("Stride must be a multiple of the data type for this query: multiple=%d stride=%d", multiple, stride));
			}

			// Build flags mask
			return new BitMask<>(flags);
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
		 * @param pQueryPool		Returned query pool
		 * @return Result
		 */
		int vkCreateQueryPool(DeviceContext device, VkQueryPoolCreateInfo pCreateInfo, Handle pAllocator, PointerReference pQueryPool);

		/**
		 * Destroys a query pool.
		 * @param device			Logical device
		 * @param queryPool			Query pool to destroy
		 * @param pAllocator		Allocator
		 */
		void vkDestroyQueryPool(DeviceContext device, Pool queryPool, Handle pAllocator);

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
		void vkCmdBeginQuery(Buffer commandBuffer, Pool queryPool, int query, BitMask<VkQueryControlFlag> flags);

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
		 * @param pipelineStage		Pipeline stage
		 * @param queryPool			Query pool
		 * @param query				Query slot
		 */
		void vkCmdWriteTimestamp(Buffer commandBuffer, VkPipelineStage pipelineStage, Pool queryPool, int query);

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
		 * @return Result
		 */
		int vkGetQueryPoolResults(DeviceContext device, Pool queryPool, int firstQuery, int queryCount, long dataSize, ByteBuffer pData, long stride, BitMask<VkQueryResultFlag> flags);

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
		void vkCmdCopyQueryPoolResults(Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount, VulkanBuffer dstBuffer, long dstOffset, long stride, BitMask<VkQueryResultFlag> flags);
	}
}

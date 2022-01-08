package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkQueryControlFlag;
import org.sarge.jove.platform.vulkan.VkQueryPipelineStatisticFlag;
import org.sarge.jove.platform.vulkan.VkQueryPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkQueryResultFlag;
import org.sarge.jove.platform.vulkan.VkQueryType;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>query</i> is used to retrieve statistics from Vulkan.
 * <p>
 * Generally a query is comprised of two commands that wrap a portion of a render sequence, e.g. to perform an {@link VkQueryType#OCCLUSION} query.
 * <p>
 * Queries are allocated as <i>slots</i> from a {@link Pool}.
 * Note that a query pool <b>must</b> be reset before each sample <b>outside</b> the render pass.
 * <p>
 * The {@link ResultBuilder} is used to configure the results of a query and to write the data to a destination buffer.
 * <p>
 * Usage:
 * <pre>
 * // Create an occlusion query pool with two slots
 * LogicalDevice dev = ...
 * Pool pool = new Pool.Builder()
 *     .type(VkQueryType.OCCLUSION)
 *     .slots(2)
 *     .build(dev);
 *
 * // Create a query for the second slot
 * Query query = pool.query(1);
 *
 * // Instrument the render sequence with the query
 * FrameBuffer frame = ...
 * Command.Buffer buffer = ...
 * buffer
 *     .add(pool.reset())
 *     .add(frame.begin())
 *         .add(query.begin())
 *             ...
 *         .add(query.end())
 *     .add(FrameBuffer#END);
 *
 * // Retrieve the query results
 * ByteBuffer results = ...
 * pool.result().build().accept(results);
 * </pre>
 * <p>
 * @author Sarge
 */
public class Query {
	private final int slot;
	private final Pool pool;

	/**
	 * Constructor.
	 * @param slot Query slot
	 * @param pool Pool
	 */
	private Query(int slot, Pool pool) {
		this.slot = slot;
		this.pool = pool;
	}

	/**
	 * Convenience factory method to create a command to reset this query.
	 * @return Reset query command
	 * @see Pool#reset(int, int)
	 */
	public Command reset() {
		return pool.reset(slot, 1);
	}

	/**
	 * Creates a command to begin this query.
	 * @param flags Control flags
	 * @return Begin query command
	 */
	public Command begin(VkQueryControlFlag... flags) {
		final int mask = IntegerEnumeration.mask(flags);
		return (lib, buffer) -> lib.vkCmdBeginQuery(buffer, pool, slot, mask);
	}

	/**
	 * Creates a command to end this query.
	 * @return End query command
	 */
	public Command end() {
		return (lib, buffer) -> lib.vkCmdEndQuery(buffer, pool, slot);
	}

	/**
	 * Creates a command to write the device timestamp at the given pipeline stage.
	 * @param stage Pipeline stage
	 * @return Timestamp command
	 */
	public Command timestamp(VkPipelineStage stage) {
		Check.notNull(stage);
		return (lib, buffer) -> lib.vkCmdWriteTimestamp(buffer, stage, pool, slot);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("slot", slot)
				.append(pool)
				.build();
	}

	/**
	 * A <i>query pool</i> is comprised of a number of <i>slots</i> used to execute queries.
	 */
	public static class Pool extends AbstractVulkanObject {
		private final int slots;

		/**
		 * Constructor.
		 * @param handle		Handle
		 * @param dev			Logical device
		 * @param slots			Number of query slots
		 */
		Pool(Pointer handle, DeviceContext dev, int slots) {
			super(handle, dev);
			this.slots = oneOrMore(slots);
		}

		/**
		 * @return Number of slots in this pool
		 */
		public int slots() {
			return slots;
		}

		/**
		 * Creates a query.
		 * @param slot Query slot
		 * @return New query
		 * @throws IllegalArgumentException if the slot is invalid for this pool
		 */
		public Query query(int slot) {
			Check.zeroOrMore(slot);
			if(slot >= slots) throw new IllegalArgumentException(String.format("Invalid query slot: slot=%d pool=%s", slot, this));
			return new Query(slot, this);
		}

		/**
		 * Creates a reset command for this pool.
		 * @param start		Starting slot
		 * @param num		Number of slots
		 * @return Reset command
		 * @throws IllegalArgumentException if the specified range exceeds the number of query slots in this pool
		 */
		public Command reset(int start, int num) {
			Check.zeroOrMore(start);
			Check.oneOrMore(num);
			if(start + num > slots) throw new IllegalArgumentException(String.format("Invalid reset range: start=%d num=%d pool=%s", start, num, this));
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

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append("slots", slots)
					.build();
		}

		/**
		 * Builder for a query pool.
		 */
		public static class Builder {
			private VkQueryType type;
			private int slots = 1;
			private final Set<VkQueryPipelineStatisticFlag> stats = new HashSet<>();

			/**
			 * Sets the query type.
			 * @param type Query type
			 */
			public Builder type(VkQueryType type) {
				this.type = notNull(type);
				return this;
			}

			/**
			 * Sets the number of slots in this pool.
			 * @param slots Query slots
			 */
			public Builder slots(int slots) {
				this.slots = oneOrMore(slots);
				return this;
			}

			/**
			 * Adds a pipeline statistic to this query.
			 * @param stat Pipeline statistic
			 */
			public Builder statistic(VkQueryPipelineStatisticFlag stat) {
				stats.add(notNull(stat));
				return this;
			}

			/**
			 * Constructs this query pool.
			 * @param dev Logical device
			 * @return New query pool
			 * @throws IllegalArgumentException if the query type is not specified
			 * @throws IllegalArgumentException if the query type is {@link VkQueryType#PIPELINE_STATISTICS} but no pipeline statistics were configured
			 */
			public Pool build(DeviceContext dev) {
				// Init create descriptor
				if(type == null) throw new IllegalArgumentException("Query type must be specified");
				final var info = new VkQueryPoolCreateInfo();
				info.queryType = type;
				info.queryCount = slots;

				// Init pipeline statistics
				if(type == VkQueryType.PIPELINE_STATISTICS) {
					if(stats.isEmpty()) throw new IllegalArgumentException("No statistics specified for pipeline query");
					info.pipelineStatistics = IntegerEnumeration.mask(stats);
				}
				else {
					if(!stats.isEmpty()) throw new IllegalArgumentException("Superfluous pipeline statistics specified");
				}

				// Instantiate query pool
				final PointerByReference ref = dev.factory().pointer();
				final VulkanLibrary lib = dev.library();
				check(lib.vkCreateQueryPool(dev, info, null, ref));

				// Create pool
				return new Pool(ref.getValue(), dev, slots);
			}
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
	 * <li>{@link ResultBuilder#build(DeviceContext)} creates an accessor to retrieve query results to a buffer on-demand</li>
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
	public static class ResultBuilder {
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
			if(start >= pool.slots) throw new IllegalArgumentException(String.format("Invalid start slot: start=%d slots=%d", start, pool.slots));
			this.start = zeroOrMore(start);
			return this;
		}

		/**
		 * Sets the number of query slots to retrieve (default is <b>all</b> slots in this pool).
		 * @param count Number of slots
		 * @throws IllegalArgumentException if {@code count} exceeds the number of query slots
		 */
		public ResultBuilder count(int count) {
			if(count > pool.slots) throw new IllegalArgumentException(String.format("Invalid slot count: count=%d slots=%d", start, pool.slots));
			this.count = oneOrMore(count);
			return this;
		}

		/**
		 * Sets the stride between results within the buffer (default is {@link Integer#BYTES}).
		 * @param stride Results stride (bytes)
		 */
		public ResultBuilder stride(long stride) {
			this.stride = oneOrMore(stride);
			return this;
		}

		/**
		 * Adds a flag to this query.
		 * @param flag Query flag
		 */
		public ResultBuilder flag(VkQueryResultFlag flag) {
			flags.add(notNull(flag));
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
			final int mask = validate();

			// Init library
			final DeviceContext dev = pool.device();
			final Library lib = dev.library();

			// Create accessor
			return buffer -> {
				// Validate buffer
				final int size = buffer.remaining();
				if(count * stride > size) throw new IllegalStateException(String.format("Insufficient buffer space for query: query=%s buffer=%s", ResultBuilder.this, buffer));

				// Execute query
				check(lib.vkGetQueryPoolResults(dev, pool, start, count, size, buffer, stride, mask));
			};
		}

		/**
		 * Constructs a command that asynchronously copies query results to the given buffer.
		 * @param buffer 		Vulkan buffer
		 * @param offset		Buffer offset
		 * @return Query results command
		 * @throws IllegalArgumentException if the query range is invalid for this pool
		 * @throws IllegalArgumentException if the stride is not a multiple of the data type of this query
		 * @throws IllegalStateException if the offset is invalid for the given buffer
		 * @throws IllegalStateException if the buffer is not a {@link VkBufferUsageFlag#TRANSFER_DST}
		 */
		public Command build(VulkanBuffer buffer, long offset) {
			// Validate buffer
			Check.notNull(buffer);
			Check.zeroOrMore(offset);
			buffer.require(VkBufferUsageFlag.TRANSFER_DST);
			buffer.validate(offset + count * stride);

			// Validate query result
			final int mask = validate();

			// Create results command
			return (lib, cmd) -> {
				// TODO - rewind?
				lib.vkCmdCopyQueryPoolResults(cmd, pool, start, count, buffer, offset, stride, mask);
			};
		}

		/**
		 * Validates this query result.
		 * @return Flags mask
		 */
		private int validate() {
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
			return IntegerEnumeration.mask(flags);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("start", start)
					.append("count", count)
					.append("stride", stride)
					.append(pool)
					.build();
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
		 * @return Result code
		 */
		int vkCreateQueryPool(DeviceContext device, VkQueryPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pQueryPool);

		/**
		 * Destroys a query pool.
		 * @param device			Logical device
		 * @param queryPool			Query pool to destroy
		 * @param pAllocator		Allocator
		 */
		void vkDestroyQueryPool(DeviceContext device, Pool queryPool, Pointer pAllocator);

		/**
		 * Command to reset a query pool.
		 * @param commandBuffer		Command buffer
		 * @param queryPool			Query pool
		 * @param firstQuery		Index of the first query slot
		 * @param queryCount		Number of query slots to reset
		 */
		void vkCmdResetQueryPool(Command.Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount);

		/**
		 * Starts a query.
		 * @param commandBuffer		Command buffer
		 * @param queryPool			Query pool
		 * @param query				Query slot
		 * @param flags				Flags
		 */
		void vkCmdBeginQuery(Command.Buffer commandBuffer, Pool queryPool, int query, int flags);

		/**
		 * Ends a query.
		 * @param commandBuffer		Command buffer
		 * @param queryPool			Query pool
		 * @param query				Query slot
		 */
		void vkCmdEndQuery(Command.Buffer commandBuffer, Pool queryPool, int query);

		/**
		 * Writes the device timestamp at the given pipeline stage to the query results.
		 * @param commandBuffer		Command buffer
		 * @param pipelineStage		Pipeline stage
		 * @param queryPool			Query pool
		 * @param query				Query slot
		 */
		void vkCmdWriteTimestamp(Command.Buffer commandBuffer, VkPipelineStage pipelineStage, Pool queryPool, int query);

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
		 * @return Result code
		 */
		int vkGetQueryPoolResults(DeviceContext device, Pool queryPool, int firstQuery, int queryCount, long dataSize, ByteBuffer pData, long stride, int flags);

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
		void vkCmdCopyQueryPoolResults(Command.Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount, VulkanBuffer dstBuffer, long dstOffset, long stride, int flags);
	}
}

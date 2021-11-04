package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.lib.util.Check;

/**
 * The <i>allocation service</i> processes memory requests.
 * <p>
 * The service composes:
 * <ol>
 * <li>selection of the appropriate memory type for an allocation request</li>
 * <li>routing of the request to a target allocator</li>
 * </ol>
 * <p>
 * To support different allocation strategies for different use-cases the service implements a <i>routing policy</i>:
 * Allocation requests are routed to the appropriate allocator depending on the {@link MemoryProperties} of the request, see {@link #route(MemoryProperties, Allocator)}.
 * Otherwise requests are delegated to the <i>default</i> allocator specified in the constructor.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * 	// Create service with default allocator
 * 	Allocator def = ...
 * 	AllocationService service = new AllocationService(new MemorySelector(), allocator);
 *
 * 	// Route to a different allocator
 * 	Predicate&lt;MemoryProperties&gt; predicate = ...
 * 	Allocator other = ...
 * 	service.route(predicate, other);
 *
 * 	// Allocate memory
 * 	VkMemoryRequirements reqs = ...
 * 	DeviceMemory mem = service.allocator(reqs, props);
 * </pre>
 * <p>
 * @author Sarge
 */
public class AllocationService {
	/**
	 * Route descriptor.
	 */
	private record Route(Predicate<MemoryProperties<?>> predicate, Allocator allocator) {
		private Route {
			Check.notNull(predicate);
			Check.notNull(allocator);
		}
	}

	private final MemorySelector selector;
	private final Allocator def;
	private final Deque<Route> routes = new LinkedList<>();

	/**
	 * Constructor.
	 * @param selector		Memory selector
	 * @param allocator		Default memory allocator
	 * @see #route(MemoryProperties, Allocator)
	 */
	public AllocationService(MemorySelector selector, Allocator allocator) {
		this.selector = notNull(selector);
		this.def = notNull(allocator);
	}

	/**
	 * Routes allocation requests matching the given memory properties predicate to the specified allocator.
	 * @param props			Memory properties predicate
	 * @param allocator		Allocator
	 */
	public void route(Predicate<MemoryProperties<?>> predicate, Allocator allocator) {
		routes.addFirst(new Route(predicate, allocator));
	}

	/**
	 * Allocates device memory for the given request.
	 * @param reqs			Requirements
	 * @param props			Memory properties
	 * @return New device memory
	 * @throws AllocationException if the memory cannot be allocated
	 */
	public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		// Route request
		final Allocator allocator = routes
				.stream()
				.filter(r -> r.predicate().test(props))
				.findAny()
				.map(r -> r.allocator())
				.orElse(def);

		// Select memory type and delegate request
		final MemoryType type = selector.select(reqs, props);
		return allocator.allocate(type, reqs.size);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("default", def)
				.append("routes", routes)
				.append("selector", selector)
				.build();
	}
}

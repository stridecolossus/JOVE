package org.sarge.jove.platform.vulkan.memory;

import java.util.*;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * The <i>allocation routing service</i> implements a <i>routing policy</i> to support different memory allocation use-cases.
 * <p>
 * Allocation requests are routed to the appropriate allocator depending on the {@link MemoryProperties} of the request, see {@link #route(MemoryProperties, Allocator)}.
 * Otherwise requests are delegated to the <i>default</i> allocator specified in the constructor.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * // Create service with default allocator
 * Allocator allocator = ...
 * var service = new AllocationRoutingService(new MemorySelector(), allocator);
 *
 * // Route to a different allocator
 * Predicate&lt;MemoryProperties&gt; predicate = ...
 * Allocator other = ...
 * service.route(predicate, other);
 *
 * // Allocate memory
 * VkMemoryRequirements reqs = ...
 * DeviceMemory mem = service.allocate(reqs, props);
 * </pre>
 * <p>
 * @author Sarge
 */
public class AllocationRoutingService extends AllocationService {
	/**
	 * Route descriptor.
	 */
	private record Route(Predicate<MemoryProperties<?>> predicate, Allocator allocator) {
		private Route {
			Check.notNull(predicate);
			Check.notNull(allocator);
		}
	}

	private final List<Route> routes = new ArrayList<>();

	/**
	 * Constructor.
	 * @param selector		Memory selector
	 * @param allocator		Default memory allocator
	 */
	public AllocationRoutingService(MemorySelector selector, Allocator allocator) {
		super(selector, allocator);
	}

	/**
	 * Routes allocation requests matching the given properties to the specified allocator.
	 * @param props			Memory properties predicate
	 * @param allocator		Allocator
	 */
	public void route(Predicate<MemoryProperties<?>> predicate, Allocator allocator) {
		routes.add(new Route(predicate, allocator));
	}

	@Override
	protected Allocator allocator(MemoryProperties<?> props) {
		return routes
				.stream()
				.filter(r -> r.predicate().test(props))
				.findAny()
				.map(Route::allocator)
				.orElseGet(() -> super.allocator(props));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("routes", routes)
				.build();
	}
}

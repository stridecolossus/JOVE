package org.sarge.jove.platform.vulkan.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;

/**
 * The <i>device limits</i> is a helper class for querying the limits of the supported hardware.
 * <p>
 * This class is essentially a wrapper for the {@link VkPhysicalDeviceLimits} structure.
 * Generally it is assumed that the user will prefer to query hardware limits using string keys rather than programatically via structure fields.
 * <p>
 * Example for an indirect multi-draw command:
 * <pre>
 * LogicalDevice dev = ...
 * DeviceLimits limits = dev.limits();
 * limits.require("multiDrawIndirect");
 * float max = limits.value("maxDrawIndirectCount");
 * if(count > max) throw ...</pre>
 * <p>
 * Some limits are a quantised range of values which can be queried using the {@link #range(String, String)} method:
 * <pre>
 * float[] sizes = limits.range("pointSizeRange", "pointSizeGranularity");</pre>
 * <p>
 * @author Sarge
 */
public class DeviceLimits {
	private final VkPhysicalDeviceLimits limits;

	/**
	 * Constructor.
	 * @param limits 		Device limits
	 * @param features		Supported features
	 */
	public DeviceLimits(VkPhysicalDeviceLimits limits) {
		this.limits = notNull(limits);
		limits.write();
	}

	/**
	 * Retrieves a device limit by name.
	 * @param <T> Data type
	 * @param name Limit name
	 * @return Limit
	 */
	@SuppressWarnings("unchecked")
	public <T> T value(String name) {
		return (T) limits.readField(name);
	}

	/**
	 * Retrieves a quantised device limit range.
	 * @param name				Limit name
	 * @param granularity		Granularity name
	 * @return Quantised range
	 */
	public float[] range(String name, String granularity) {
		// Lookup range bounds
		final float[] bounds = value(name);
		final float min = bounds[0];
		final float max = bounds[1];

		// Lookup granularity step
		final float step = value(granularity);

		// Determine number of values
		final int num = (int) ((max - min) / step);

		// Build quantised range
		final float[] range = new float[num + 1];
		for(int n = 0; n < num; ++n) {
			range[n] = min + n * step;
		}
		range[num] = max;

		return range;
	}
}

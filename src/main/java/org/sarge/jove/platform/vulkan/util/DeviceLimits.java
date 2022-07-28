package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;

/**
 * The <i>device limits</i> is a helper class for querying the limits of the supported hardware.
 * <p>
 * Example for an indirect multi-draw command:
 * <pre>
 * LogicalDevice dev = ...
 * DeviceLimits limits = dev.limits();
 * limits.require("multiDrawIndirect");
 * float max = limits.value("maxDrawIndirectCount");</pre>
 * <p>
 * Some limits are a quantised range of values which can be queried using the {@link #range(String, String)} helper method:
 * <pre>
 * float[] sizes = limits.range("pointSizeRange", "pointSizeGranularity");</pre>
 * @see VkPhysicalDeviceLimits
 * @author Sarge
 */
public class DeviceLimits {
	private final VkPhysicalDeviceLimits limits;
	private final DeviceFeatures features;

	/**
	 * Constructor.
	 * @param limits 		Device limits
	 * @param features		Supported features
	 */
	public DeviceLimits(VkPhysicalDeviceLimits limits, DeviceFeatures features) {
		this.limits = notNull(limits);
		this.features = notNull(features);
		limits.write();
	}

	/**
	 * @return Device features
	 */
	public DeviceFeatures features() {
		return features;
	}

	/**
	 * Helper - Checks that the given device feature is supported by the hardware.
	 * @param name Feature name
	 * @throws IllegalStateException if the feature is not supported
	 */
	public void require(String name) {
		if(!features.features().contains(name)) {
			throw new IllegalStateException("Feature not supported: " + name);
		}
	}

	/**
	 * Retrieves a device limit by name.
	 * @param <T> Limit type
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

package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPastPresentationTimingGOOGLE implements NativeStructure {
	public int presentID;
	public long desiredPresentTime;
	public long actualPresentTime;
	public long earliestPresentTime;
	public long presentMargin;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("presentID"),
			PADDING,
			JAVA_LONG.withName("desiredPresentTime"),
			JAVA_LONG.withName("actualPresentTime"),
			JAVA_LONG.withName("earliestPresentTime"),
			JAVA_LONG.withName("presentMargin")
		);
	}
}

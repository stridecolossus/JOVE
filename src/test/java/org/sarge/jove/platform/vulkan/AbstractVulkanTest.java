package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatcher;
import org.sarge.jove.platform.Handle;
import org.sarge.jove.platform.vulkan.Vulkan.ReferenceFactory;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public abstract class AbstractVulkanTest {
	protected VulkanLibrary library;
	protected ReferenceFactory factory;
	protected LogicalDevice device;

	@BeforeEach
	public void beforeVulkanTest() {
		// Create library
		library = mock(VulkanLibrary.class);

		// Create mock reference factory
		factory = new ReferenceFactory() {
			private final IntByReference counter = new IntByReference(1);
			private final PointerByReference ptr = new PointerByReference(new Pointer(42));
			private final Pointer[] array = new Pointer[]{new Pointer(42)};

			@Override
			public IntByReference integer() {
				return counter;
			}

			@Override
			public PointerByReference reference() {
				return ptr;
			}

			@Override
			public Pointer[] pointers(int size) {
				assertEquals(1, size, "Mock pointer array length must be one");
				return array;
			}
		};

		// Initialise mock implementation
		final Vulkan vulkan = mock(Vulkan.class);
		when(vulkan.library()).thenReturn(library);
		when(vulkan.factory()).thenReturn(factory);
		Vulkan.init(vulkan);

		// Create logical device
		device = mock(LogicalDevice.class);
		when(device.handle()).thenReturn(mock(Pointer.class));
		when(device.semaphore()).thenReturn(mock(Handle.class));
	}

	/**
	 * @return Mock instance
	 */
	protected VulkanInstance createInstance() {
		final VulkanInstance instance = mock(VulkanInstance.class);
		when(instance.handle()).thenReturn(mock(Pointer.class));
		return instance;
	}

	/**
	 * @return Mock physical device
	 */
	protected PhysicalDevice createPhysicalDevice() {
		final VulkanInstance instance = createInstance();
		final PhysicalDevice dev = mock(PhysicalDevice.class);
		when(dev.handle()).thenReturn(mock(Pointer.class));
		when(dev.instance()).thenReturn(instance);
		return dev;
	}

	/**
	 * Creates a structure argument matcher.
	 * @param expected Expected structure
	 * @return Structure matcher
	 * @see Structure#dataEquals(Structure)
	 */
	protected static <T extends Structure> ArgumentMatcher<T> structure(T expected) {
		return arg -> arg.dataEquals(expected);
	}
}
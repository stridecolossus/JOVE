package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.*;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.foreign.Callback.CallbackTransformerFactory;

/**
 * The <i>desktop</i> service manages windows and input devices implemented using the GLFW native library.
 * <p>
 * Note that many GLFW operations <b>must</b> be executed on the <b>main</b> thread, these are marked by the {@link MainThread} annotation.
 * See <a href="https://www.glfw.org/docs/latest/intro.html#thread_safety">Thread Constraints</a>
 * <p>
 * @see <a href="https://www.glfw.org/docs/latest/index.html">GLFW documentation</a>
 * @see <a href="https://github.com/badlogic/jglfw/blob/master/jglfw/jni/glfw-3.0/include/GL/glfw3.h">C header</a>
 * <p>
 * @author Sarge
 */
public class Desktop implements TransientObject {
	/**
	 * Creates the desktop service.
	 * @return Desktop service
	 * @throws RuntimeException if GLFW cannot be loaded or initialised
	 */
	@MainThread
	public static Desktop create() {
//		// Determine library name
//		final String name = switch(Platform.getOSType()) {
//			case Platform.WINDOWS -> "C:/GLFW/lib-mingw-w64/glfw3.dll";		// <--- TODO - path!
//			case Platform.LINUX -> "libglfw";
//			default -> throw new RuntimeException("Unsupported platform for GLFW: " + Platform.getOSType());
//		};

		// TODO
		final var registry = DefaultRegistry.create();
		registry.add(Callback.class, new CallbackTransformerFactory(registry));

		// Load native library
		final var factory = new NativeLibraryFactory("C:/GLFW/lib-mingw-w64/glfw3.dll", registry); // TODO - name
		final Class<?>[] api = {
				DesktopLibrary.class,
				WindowLibrary.class,
				MonitorLibrary.class,
				DeviceLibrary.class,
		};
		final var library = (DesktopLibrary) factory.build(List.of(api));
		// TODO - JoystickManager.init(lib);

		// Create desktop service
		return new Desktop(library);
	}

	/**
	 * Marker interface for methods that <b>must</b> be called from the main thread.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.METHOD)
	@interface MainThread {
		// Marker
	}

	private final DesktopLibrary library;
	private boolean destroyed;

	/**
	 * Constructor.
	 * @param library GLFW library
	 * @throws RuntimeException if GLFW fails to initialise
	 */
	Desktop(DesktopLibrary library) {
		this.library = requireNonNull(library);
		init();
	}

	private void init() {
		final int result = library.glfwInit();
		if(result != 1) {
			throw new RuntimeException("Cannot initialise GLFW: code=" + result);
		}
	}

	/**
	 * @return GLFW library
	 */
	@SuppressWarnings("unchecked")
	public <T> T library() {
		return (T) library;
	}

	public void poll() {
		final DeviceLibrary library = this.library();
		library.glfwPollEvents();
	}

	/**
	 * @return GLFW version
	 */
	public String version() {
		return library.glfwGetVersionString();
	}

	/**
	 * @return Whether Vulkan is supported on the current hardware
	 */
	public boolean isVulkanSupported() {
		return library.glfwVulkanSupported();
	}

	/**
	 * Returns and clears the last GLFW error.
	 * @return Error message
	 */
	public Optional<String> error() {
		final var description = new Pointer();
		final int code = library.glfwGetError(description);
		if(code == 0) {
			return Optional.empty();
		}
		else {
			final Handle handle = description.handle();
			final String message = StringTransformer.unmarshal(handle.address());
			final String error = String.format("[%d] %s", code, message);
			return Optional.of(error);
		}
	}

	/**
	 * @return Vulkan extensions supported by this desktop
	 */
	public List<String> extensions() {
		final var count = new IntegerReference();
		final Handle handle = library.glfwGetRequiredInstanceExtensions(count);
		return AbstractArrayTransformer.unmarshal(handle.address(), count.get(), StringTransformer::unmarshal);
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	@MainThread
	public void destroy() {
		if(destroyed) throw new IllegalStateException();
		library.glfwTerminate();
		destroyed = true;
	}
}

package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.*;
import java.lang.foreign.*;
import java.util.function.*;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Handle.HandleNativeTransformer;
import org.sarge.jove.common.NativeObject.NativeObjectTransformer;
import org.sarge.jove.foreign.*;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;

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
public final class Desktop implements TransientObject {
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

		final var registry = new NativeRegistry();
		registry.add(int.class, new IdentityNativeTransformer<>(ValueLayout.JAVA_INT));
		registry.add(boolean.class, new IdentityNativeTransformer<>(ValueLayout.JAVA_BOOLEAN));
		registry.add(String.class, new StringNativeTransformer());
		registry.add(NativeReference.class, new NativeReferenceTransformer());
		registry.add(Handle.class, new HandleNativeTransformer());
		registry.add(NativeObject.class, new NativeObjectTransformer());

		// TODO
		registry.add(MemorySegment.class, new IdentityNativeTransformer<>(ValueLayout.ADDRESS));

		// Load native library
		final var factory = new NativeLibraryBuilder("C:/GLFW/lib-mingw-w64/glfw3.dll", registry); // TODO - name
		final DesktopLibrary lib = factory.build(DesktopLibrary.class);
		// TODO - JoystickManager.init(lib);

		// Init GLFW
		final int result = lib.glfwInit();
		if(result != 1) throw new RuntimeException("Cannot initialise GLFW: code=" + result);

		// Create desktop service
		return new Desktop(lib, new NativeReference.Factory());
	}

	/**
	 * Marker interface for methods that <b>must</b> be called from the main thread.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.METHOD)
	@interface MainThread {
		// Marker
	}

	private final DesktopLibrary lib;
	private final NativeReference.Factory factory;

	/**
	 * Constructor.
	 * @param lib 			GLFW library
	 * @param factory		Reference factory
	 */
	Desktop(DesktopLibrary lib, NativeReference.Factory factory) {
		this.lib = requireNonNull(lib);
		this.factory = requireNonNull(factory);
	}

	/**
	 * @return GLFW library
	 */
	DesktopLibrary library() {
		return lib;
	}

	/**
	 * @return Reference factory
	 */
	NativeReference.Factory factory() {
		return factory;
	}

	/**
	 * @return GLFW version
	 */
	public String version() {
		return lib.glfwGetVersionString();
	}

	/**
	 * @return Whether Vulkan is supported on the current hardware
	 */
	public boolean isVulkanSupported() {
		return lib.glfwVulkanSupported();
	}

	/**
	 * Processes pending input events.
	 */
	@MainThread
	public void poll() {
//		lib.glfwPollEvents();
	}

	/**
	 * @return Vulkan extensions supported by this desktop
	 */
	public String[] extensions() {
		final NativeReference<Integer> count = factory.integer();
		final MemorySegment address = lib.glfwGetRequiredInstanceExtensions(count);
		return array(address, count.get(), String[]::new, StringNativeTransformer::unmarshal);
	}

	// TODO
	static <T> T[] array(MemorySegment address, int length, IntFunction<T[]> factory, Function<MemorySegment, T> mapper) {
		// Allocate array
		final T[] array = factory.apply(length);

		// Resize address to array
		// TODO - this only works for array of address, e.g. wouldn't work for structures
		final MemorySegment segment = address.reinterpret(ValueLayout.ADDRESS.byteSize() * length);

		// Extract and transform array elements
		for(int n = 0; n < length; ++n) {
			final MemorySegment element = segment.getAtIndex(ValueLayout.ADDRESS, n);
			array[n] = mapper.apply(element);
		}

		return array;
	}

//	/**
//	 * Sets the handler for GLFW errors.
//	 * @param handler Error handler
//	 */
//	@MainThread
//	public void setErrorHandler(Consumer<String> handler) {
//		final ErrorCallback callback = (error, description) -> {
//			final String message = String.format("GLFW error: [%d] %s", error, description);
//			handler.accept(message);
//		};
//		lib.glfwSetErrorCallback(callback);
//	}

	@Override
	@MainThread
	public void destroy() {
		lib.glfwTerminate();
	}
}

package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.*;
import java.lang.foreign.ValueLayout;

import org.sarge.jove.common.*;
import org.sarge.jove.common.NativeObject.NativeObjectTransformer;
import org.sarge.jove.foreign.*;

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

//		// Init type mapper
//		final var mapper = new DefaultTypeMapper();
//		mapper.addTypeConverter(Handle.class, Handle.CONVERTER);
//		mapper.addTypeConverter(Window.class, NativeObject.CONVERTER);

		final var registry = TransformerRegistry.create();
		registry.add(new NativeObjectTransformer());
		//registry.add(new DefaultNativeMapper<>(null, null)
		// TODO - window mapper?

		// Load native library
		final var factory = new NativeFactory(registry);
		final DesktopLibrary lib = factory.build("C:/GLFW/lib-mingw-w64/glfw3.dll", DesktopLibrary.class); // TODO - name
		// TODO - JoystickManager.init(lib);

		// Init GLFW
		final int result = lib.glfwInit();
		if(result != 1) throw new RuntimeException("Cannot initialise GLFW: code=" + result);

		// Create desktop service
		return new Desktop(lib, new ReferenceFactory());
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
	private final ReferenceFactory factory;

	/**
	 * Constructor.
	 * @param lib 			GLFW library
	 * @param factory		Reference factory
	 */
	Desktop(DesktopLibrary lib, ReferenceFactory factory) {
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
	ReferenceFactory factory() {
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
		lib.glfwPollEvents();
	}

	/**
	 * @return Vulkan extensions supported by this desktop
	 */
	public String[] extensions() {
		final IntegerReference count = factory.integer();
		final Handle handle = lib.glfwGetRequiredInstanceExtensions(count);
		return handle.array(count.value(), ValueLayout.ADDRESS, String[]::new, StringNativeTransformer::unmarshal);
		// TODO
//		final ArrayReturnValue<String> value = lib.glfwGetRequiredInstanceExtensions(count);
//		return value.array(count.value(), String[]::new);
	}

//		final MemorySegment address = array.address().reinterpret(ValueLayout.ADDRESS.byteSize() * count.value());
//
//		final String[] str = new String[count.value()];
//		for(int n = 0; n < count.value(); ++n) {
//
//			final MemorySegment e = address.getAtIndex(ValueLayout.ADDRESS, n);
//			str[n ] = StringNativeMapper.unmarshal(e);
//
//					//root.getString(n * ValueLayout.ADDRESS.byteSize());
//					//getAtIndex(ValueLayout.ADDRESS, n).getString(0);
//		}
//
////		return array.get(count.value());
////		return new String[]{"", ""};
//
//		return str;
//	}

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

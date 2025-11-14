package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireNotEmpty;

import java.lang.foreign.*;
import java.util.List;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.desktop.Desktop.MainThread;
import org.sarge.jove.platform.desktop.MonitorLibrary.DesktopDisplayMode;

/**
 * A <i>monitor</i> describes a physical monitor attached to this system.
 * @author Sarge
 */
public record Monitor(Handle handle, String name, Dimensions size, List<DisplayMode> modes) implements NativeObject {
	/**
	 * Display mode.
	 */
	public record DisplayMode(Dimensions size, int red, int green, int blue, int refresh) {
		/**
		 * Constructor.
		 * @param size			Size
		 * @param depth			RGB bit depth
		 * @param refresh		Refresh rate (Hz)
		 * @throws IllegalArgumentException if the given bit depth is not an RGB array
		 */
		public DisplayMode {
			requireNonNull(size);
//			requireOneOrMore(refresh);
		}

		/**
		 * Converts structure to display mode.
		 */
		private static DisplayMode of(DesktopDisplayMode mode) {
			return new DisplayMode(new Dimensions(mode.width, mode.height), mode.red, mode.green, mode.blue, mode.refresh);
		}
	}

	/**
	 * Constructor.
	 * @param handle	Handle
	 * @param name		Monitor name
	 * @param size		Physical dimensions
	 * @param modes		Display modes supported by this monitor
	 */
	public Monitor {
		requireNonNull(handle);
		requireNotEmpty(name);
		requireNonNull(size);
		modes = List.copyOf(modes);
	}

	/**
	 * Retrieves the current display mode for this monitor.
	 * @param desktop Desktop service
	 * @return Current display mode
	 */
	@MainThread
	public DisplayMode mode(Desktop desktop) {
		final var library = (MonitorLibrary) desktop.library();
		final Handle handle = library.glfwGetVideoMode(this);

		final var registry = new Registry();
		registry.add(int.class, new PrimitiveTransformer<>(ValueLayout.JAVA_INT));
		final var factory = new StructureTransformerFactory(registry);
		final var transformer = factory.transformer(DesktopDisplayMode.class);

		final MemorySegment mem = handle.address().reinterpret(transformer.layout().byteSize());
		final NativeStructure result = transformer.unmarshal().apply(mem);

		return DisplayMode.of((DesktopDisplayMode) result);

//		final DesktopDisplayMode mode = library.glfwGetVideoMode(this);
//		return DisplayMode.of(mode);
	}

	/**
	 * Enumerates the monitors attached to this system.
	 * The <i>primary</i> monitor is the first in the array.
	 * @param desktop Desktop service
	 * @return Monitors
	 */
	@MainThread
	public static List<Monitor> monitors(Desktop desktop) {
		// Retrieve pointer to monitors
		final var library = (MonitorLibrary) desktop.library();
		final var count = new IntegerReference();
		final Handle handle = library.glfwGetMonitors(count);

		final var builder = new Object() {

			Monitor create(MemorySegment address) {
				final Handle monitor = new Handle(address);
				final String name = library.glfwGetMonitorName(monitor);
				final Dimensions size = size(monitor);
				//final List<DisplayMode> modes = modes(monitor);
				return new Monitor(monitor, name, size, List.of()); // TODO - modes);
			}

			private Dimensions size(Handle monitor) {
				final var width = new IntegerReference();
				final var height = new IntegerReference();
				library.glfwGetMonitorPhysicalSize(monitor, width, height);
				return new Dimensions(width.get(), height.get());
			}

			/*
			private List<DisplayMode> modes(Handle monitor) {
				final var registry = new Registry();
				registry.register(int.class, new PrimitiveTransformer<>(ValueLayout.JAVA_INT));

				final var factory = new StructureTransformerFactory(registry);
				final var transformer = factory.transformer(DesktopDisplayMode.class);

				final Function<MemorySegment, DisplayMode> mapper = address -> {
					final var structure = transformer.unmarshal().apply(address);
					return DisplayMode.of((DesktopDisplayMode) structure);
				};

				final var count = new IntegerReference();
				final Handle handle = library.glfwGetVideoModes(monitor, count);
				return AbstractArrayTransformer.unmarshal(handle.address(), count.get(), mapper);
			}
			*/
		};

		return AbstractArrayTransformer.unmarshal(handle.address(), count.get(), builder::create);
	}
}

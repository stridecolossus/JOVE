package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;

import java.util.Arrays;
import java.util.List;

import org.sarge.jove.common.Dimensions;

/**
 * A <i>monitor</i> describes a physical monitor attached to this system.
 */
public final class Monitor {
	/**
	 * Display mode.
	 */
	public static final class DisplayMode { // TODO - record
		private final Dimensions size;
		private final int[] bits;
		private final int refresh;

		/**
		 * Constructor.
		 * @param size			Size
		 * @param bits			RGB bit depth
		 * @param refresh		Refresh rate (Hz)
		 * @throws IllegalArgumentException if the given bit depth is not an RGB array
		 */
		public DisplayMode(Dimensions size, int[] bits, int refresh) {
			if(bits.length != 3) throw new IllegalArgumentException("Invalid RGB bit depth array");
			this.size = notNull(size);
			this.bits = Arrays.copyOf(bits, bits.length);
			this.refresh = oneOrMore(refresh);
		}

		/**
		 * @return Size
		 */
		public Dimensions size() {
			return size;
		}

		/**
		 * @return RGB bit depth
		 */
		public int[] depth() {
			return Arrays.copyOf(bits, bits.length);
		}

		/**
		 * @return Refresh rate (Hz)
		 */
		public int refresh() {
			return refresh;
		}
	}

	private final Object handle;
	private final String name;
	private final Dimensions size;
	private final List<DisplayMode> modes;

	/**
	 * Constructor.
	 * @param handle	Handle
	 * @param name		Monitor name
	 * @param size		Physical dimensions
	 * @param modes		Display modes supported by this monitor
	 */
	public Monitor(Object handle, String name, Dimensions size, List<DisplayMode> modes) {
		this.handle = notNull(handle);
		this.name = notEmpty(name);
		this.size = notNull(size);
		this.modes = List.copyOf(modes);
	}

	/**
	 * @return Monitor handle
	 */
	public Object handle() {
		return handle;
	}

	/**
	 * @return Monitor name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Physical dimensions of this monitor
	 */
	public Dimensions size() {
		return size;
	}

	/**
	 * @return Display modes supported by this monitor
	 */
	public List<DisplayMode> modes() {
		return modes;
	}
}

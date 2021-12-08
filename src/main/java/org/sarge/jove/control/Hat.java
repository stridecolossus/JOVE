package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.control.Button.AbstractButton;
import org.sarge.jove.util.IntegerEnumeration;

/**
 * A <i>hat</i> is a joystick controller that can be pointed in the compass directions.
 * @author Sarge
 */
public class Hat extends AbstractButton {
	/**
	 * Hat actions.
	 * <p>
	 * Note that hat diagonals are represented as a bit-mask of this enumeration, e.g. 3 maps to <code>[UP, RIGHT]</code>
	 */
	public enum HatAction implements IntegerEnumeration {
		CENTERED(0),
		UP(1),
		RIGHT(2),
		DOWN(4),
		LEFT(8);

		private static final IntegerEnumeration.ReverseMapping<HatAction> MAPPING = IntegerEnumeration.mapping(HatAction.class);
		private static final Set<HatAction> DEFAULT = Set.of(CENTERED);

		private final int value;

		private HatAction(int value) {
			this.value = value;
		}

		@Override
		public int value() {
			return value;
		}
	}

	private final String id;
	private final String name;
	private final Set<HatAction> action;

	/**
	 * Constructor.
	 * @param id Hat identifier
	 */
	public Hat(String id) {
		this(id, HatAction.DEFAULT);
	}

	/**
	 * Constructor.
	 * @param id			Hat identifier
	 * @param action		Hat action(s)
	 */
	private Hat(String id, Set<HatAction> action) {
		this.id = notEmpty(id);
		this.action = notNull(action);
		this.name = Button.name(id, Button.name(action.toArray()));
	}

	/**
	 * @return Hat identifier
	 */
	public String id() {
		return id;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Set<HatAction> action() {
		return action;
	}

	@Override
	public Hat resolve(int action, int mods) {
		checkUnmodified(mods);
		final Set<HatAction> set = action == 0 ? HatAction.DEFAULT : HatAction.MAPPING.enumerate(action);
		return new Hat(id, set);
	}
}

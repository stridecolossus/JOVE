package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.control.Button.AbstractButton;
import org.sarge.jove.util.IntegerEnumeration;

/**
 * A <i>hat</i> is a joystick controller that can be pointed in the compass directions.
 * <p>
 * When used as a template the sets of hat actions can be empty (but not {@code null}) to match <b>any</b> combination.
 * <p>
 * Note that a hat with a super-set of the specified actions is considered a match.
 * For example the following match test would pass:
 * <pre>
 * Hat template = new Hat("id", Set.of(HatAction.UP));
 * template.matches(new Hat("id", Set.of(HatAction.UP, HatAction.RIGHT));
 * </pre>
 * <p>
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
	protected Hat(String id, Set<HatAction> action) {
		super(id);
		this.action = notNull(action);
		this.name = Button.name(id, Button.name(action.toArray()));
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
	public boolean matches(Button button) {
		return
				super.matches(button) &&
				(button instanceof Hat that) &&
				that.action.containsAll(this.action);
	}

	@Override
	public Hat resolve(int action) {
		final Set<HatAction> actions = action == 0 ? HatAction.DEFAULT : HatAction.MAPPING.enumerate(action);
		return new Hat(id, actions);
	}
}

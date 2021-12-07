package org.sarge.jove.control;

import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Set;
import java.util.TreeSet;

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
		private static final TreeSet<HatAction> EMPTY = new TreeSet<>(Set.of(CENTERED));

		private final int value;

		private HatAction(int value) {
			this.value = value;
		}

		@Override
		public int value() {
			return value;
		}
	}

	private final int id;
	private final String name;
	private final Set<HatAction> action;

	/**
	 * Constructor.
	 * @param id			Hat identifier
	 * @param action		Hat action mask
	 */
	public Hat(int id, int action) {
		this.id = zeroOrMore(id);
		this.action = action == 0 ? HatAction.EMPTY : HatAction.MAPPING.enumerate(action);
		this.name = Button.name("Hat", id, Button.name(this.action.toArray()));
	}

	/**
	 * @return Hat identifier
	 */
	public int id() {
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

	/**
	 * Resolves this hat.
	 * @param action Action mask
	 * @return New hat
	 */
	public Hat resolve(int action) {
		return new Hat(id, action);
	}
}

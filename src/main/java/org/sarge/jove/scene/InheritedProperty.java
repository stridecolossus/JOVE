package org.sarge.jove.scene;

import java.util.function.Function;

import org.sarge.jove.scene.Node.Visitor;

/**
 *
 * @param <T>
 * @author Sarge
 */
public abstract class InheritedProperty<T> {
	/**
	 *
	 * @return
	 */
	public abstract boolean isDirty();

	/**
	 *
	 * @param parent
	 */
	abstract void update(T parent);

	/**
	 *
	 * @param <T>
	 * @param mapper
	 * @return
	 */
	public static <T extends InheritedProperty<T>> Visitor visitor(Function<Node, T> mapper) {
		return new Visitor() {
			private T parent;

			@Override
			public void visit(Node node) {
				final T value = mapper.apply(node);
				if(value.isDirty()) {
					value.update(parent);
				}
				parent = value;
			}
		};
	}
}

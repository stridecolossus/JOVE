package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.List;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.model.Model.AbstractModel;

/**
 * A <i>buffered model</i>
 * TODO
 * @author Sarge
 */
public class BufferedModel extends AbstractModel {
	/**
	 * Creates a buffered model from the given mutable model.
	 * @param model Model
	 * @return New buffered model
	 */
	public static BufferedModel of(Model model) {
		final Bufferable index = model.isIndexed() ? model.index() : null;
		return new BufferedModel(model.layout(), model.primitive(), model.count(), model.vertices(), index);
	}

	private final List<Layout> layout;
	private final int count;
	private final Bufferable vertices;
	private final Bufferable index;

	/**
	 * Constructor.
	 * @param layout			Vertex layout
	 * @param primitive			Drawing primitive
	 * @param count				Draw count
	 * @param vertices			Vertices
	 * @param index				Optional index
	 */
	public BufferedModel(List<Layout> layout, Primitive primitive, int count, Bufferable vertices, Bufferable index) {
		super(primitive);
		this.layout = List.copyOf(layout);
		this.count = zeroOrMore(count);
		this.vertices = notNull(vertices);
		this.index = index;
	}

	@Override
	public List<Layout> layout() {
		return layout;
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public boolean isIndexed() {
		return index != null;
	}

	@Override
	public Bufferable vertices() {
		return vertices;
	}

	@Override
	public Bufferable index() {
		return index;
	}
}

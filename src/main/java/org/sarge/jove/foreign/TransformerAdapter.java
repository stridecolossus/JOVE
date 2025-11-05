package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.*;

/**
 * A <i>transformer adapter</i> caches the return value and by-reference operations of a transformer.
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
class TransformerAdapter implements Transformer {
	private final Transformer delegate;
	private Function unmarshal;
	private BiConsumer update;

	/**
	 * Constructor.
	 * @param delegate Delegate transformer
	 */
	public TransformerAdapter(Transformer delegate) {
		this.delegate = requireNonNull(delegate);
	}

	/**
	 * @return Delegate transformer
	 */
	public Transformer delegate() {
		return delegate;
	}

	@Override
	public MemoryLayout layout() {
		return delegate.layout();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object marshal(Object arg, SegmentAllocator allocator) {
		return delegate.marshal(arg, allocator);
	}

	@Override
	public Object empty() {
		return delegate.empty();
	}

	@Override
	public Function unmarshal() {
		if(unmarshal == null) {
			unmarshal = delegate.unmarshal();
		}
		return unmarshal;
	}

	@Override
	public BiConsumer update() {
		if(update == null) {
			update = delegate.update();
		}
		return update;
	}

	@Override
	public Transformer array() {
		return delegate.array();
	}
}

package org.sarge.jove.io;

/**
 * Convenience compound type for a persistent resource.
 * @param <IN>		Input type
 * @param <OUT>		Output type
 * @param <R>		Resource type
 * @author Sarge
 */
public interface ResourceLoaderWriter<IN, OUT, R> extends ResourceLoader<IN, R>, ResourceWriter<OUT, R> {
	// Marker interface
}

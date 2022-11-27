package org.sarge.jove.platform.audio;

import java.util.List;

import org.sarge.jove.common.NativeObject;

import com.sun.jna.ptr.IntByReference;

/**
 * An <i>audio queue</i> is a source that streams multiple buffers.
 * @author Sarge
 */
public class AudioQueue extends AudioSource {
	/**
	 * Constructor.
	 * @param src Underlying source
	 */
	public AudioQueue(AudioSource src) {
		super(src);
	}

	/**
	 * @throws UnsupportedOperationException Buffers <b>must</b> be enqueued
	 * @see #queue(List)
	 */
	@Override
	public void buffer(AudioBuffer buffer) {
		throw new UnsupportedOperationException("Buffers must be enqueued");
	}

	/**
	 * @return Buffers processed by this queue
	 */
	public List<AudioBuffer> processed() {
		// Count number of processed buffers
		final var count = new IntByReference();
		lib.alGetSourcei(this, AudioParameter.BUFFERS_PROCESSED, count);
		System.out.println(count.getValue());

//		// Skip if none
//		if(count.getValue() == 0) {
//			return List.of();
//		}
//
//		// Retrieve buffers
////		final var ref = new PointerByReference();
//		final int[] array = new int[count.getValue()];
//		lib.alGetSourceiv(this, AudioParameter.BUFFERS_PROCESSED, array); //ref);
//
//		//final int[] id = ref.getValue().getIntArray(0, count.getValue());

		return null;
	}

	/**
	 * Adds the given buffers to this queue.
	 * @param buffers Buffers to enqueue
	 * @throws IllegalArgumentException if all {@link #buffers} do not have the same audio format
	 */
	public void queue(List<AudioBuffer> buffers) {
		// TODO - check format
		lib.alSourceQueueBuffers(this, buffers.size(), NativeObject.array(buffers));
		dev.check();
		this.buffers.addAll(buffers);
	}

	/**
	 * Removes the given buffers from this queue.
	 * @param buffers Buffers to remove
	 * @throws IllegalArgumentException if any buffer is not in this queue
	 */
	public void dequeue(List<AudioBuffer> buffers) {
		if(!this.buffers.containsAll(buffers)) throw new IllegalArgumentException("Invalid buffers for this queue");
		lib.alSourceUnqueueBuffers(this, buffers.size(), NativeObject.array(buffers));
		dev.check();
		this.buffers.removeAll(buffers);
	}
}

//public List<AudioBuffer> queued() {
//	final var count = new IntByReference();
//	lib.alGetSourcei(this, BUFFERS_QUEUED, count);
//
//	final var ref = new PointerByReference();
//	lib.alGetSourceiv(this, BUFFERS_QUEUED, ref);
//
//	return null;
//}
//
//public List<AudioBuffer> processed() {
//	return null;
//}
//
//
//public void queue(List<AudioBuffer> buffers) {
//
//}
//
//
//// TODO - queues
//// - alSourceQueueBuffers
//// - alSourceUnqueueBuffers
//// - sub-class?
//// * Buffers Queued (Query only)       AL_BUFFERS_QUEUED       ALint
//// * Buffers Processed (Query only)    AL_BUFFERS_PROCESSED    ALint

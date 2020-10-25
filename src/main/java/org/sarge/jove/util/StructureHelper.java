package org.sarge.jove.util;

import java.util.Collection;

import com.sun.jna.Memory;
import com.sun.jna.Structure;

/**
 * JNA helper utilities.
 * @author Sarge
 */
public final class StructureHelper {
	private StructureHelper() {
	}

// TODO
//	However, I realize there may be a more elegant solution now, rather than write/read/write...
	//we could just write once.
	//We just change the structure's memory backing from its autoallocated location to the contiguous location and then write to it.
	//Untested, but this might work: for (int n....) { structures[n].useMemory(mem.share(n * size)); structures[n].write(); }
	//(JNA keeps track of references to the native memory and will free it when it's no longer backing a java object, you don't need to do that yourself.)
	//– Daniel Widdis Mar 18 at 16:06

	/**
	 * Allocates a contiguous memory block for a pointer-to-structure array.
	 * @param structures Structures
	 * @return Contiguous memory block or <tt>null</tt> for an empty list
	 * @param <T> Structure type
	 */
	public static <T extends Structure> Memory structures(Collection<T> structures) {
		// Check for empty case
		if(structures.isEmpty()) {
			return null;
		}

		// Allocate contiguous memory block
		final Structure[] array = structures.toArray(Structure[]::new);
		final int size = array[0].size();
		final Memory mem = new Memory(array.length * size);

		// Copy structures
		for(int n = 0; n < array.length; ++n) {
			if(array[n] != null) {
				array[n].write(); // TODO - what is this actually doing? following line returns zeros unless write() is invoked
				final byte[] bytes = array[n].getPointer().getByteArray(0, size);
				mem.write(n * size, bytes, 0, bytes.length);
			}
		}

		return mem;
	}
}

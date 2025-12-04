package org.sarge.jove.platform.vulkan.generator;

import java.util.SequencedMap;

/**
 * Metadata for an enumeration.
 * @author Sarge
 */
record EnumerationData(String name, SequencedMap<String, Integer> values) {
}

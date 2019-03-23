package org.sarge.test;

/**
 * HEADER COMMENT
 */
public enum TestEnumeration {
 	ONE(1), 	
 	TWO(2); 	

	private final long value;
	
	private TestEnumeration(long value) {
		this.value = value;
	}

	/**
	 * @return Enum literal
	 */
	public long value() {
		return value;
	}
}

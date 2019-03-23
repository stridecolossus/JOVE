package $package;

/**
 * HEADER COMMENT
 */
public enum $name {
#foreach($entry in $values.entrySet())
 	${entry.key}($entry.value)#if($foreach.hasNext),#else;#end
 	
#end

	private final long value;
	
	private $name(long value) {
		this.value = value;
	}

	/**
	 * @return Enum literal
	 */
	public long value() {
		return value;
	}
}

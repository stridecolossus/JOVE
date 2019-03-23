package $package;

import com.sun.jna.Structure;
import com.sun.jna.Structure.ByValue;
import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

#foreach($import in $imports)
import $import.name;
#end

/**
 * HEADER COMMENT
 */
@FieldOrder({
#foreach($field in $fields)
"$field.name"#if($foreach.hasNext),
#end
#end

})
public class $name extends Structure {
	public static class ByValue extends $name implements Structure.ByValue { }
	public static class ByReference extends $name implements Structure.ByReference { }
	
#foreach($field in $fields)
	public $field.type.getSimpleName() $field.name;
#end
}

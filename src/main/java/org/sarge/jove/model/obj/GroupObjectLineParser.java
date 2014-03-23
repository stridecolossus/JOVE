package org.sarge.jove.model.obj;

/**
 * Polygon group / object parser.
 * @author Sarge
 */
public class GroupObjectLineParser implements ObjectLineParser {
	@Override
	public void parse( String[] args, ObjectModelData data ) {
		final String name;
		if( args == null ) {
			name = "group";
		}
		else {
			name = args[ 0 ];
		}
		data.startNode( name );
	}
}

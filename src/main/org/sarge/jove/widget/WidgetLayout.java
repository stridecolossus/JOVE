package org.sarge.jove.widget;

import java.util.List;

import org.sarge.jove.common.Location;

/**
 * Widget layout.
 * @see ContainerWidget
 * @author Sarge
 */
public interface WidgetLayout {
	/**
	 * Applies this layout to a group of widgets.
	 * @param origin		Layout origin (top-left)
	 * @param widgets		Widgets
	 * @return Size of this layout for the given widgets
	 */
	Dimensions apply( Location origin, List<Widget> widgets );
}

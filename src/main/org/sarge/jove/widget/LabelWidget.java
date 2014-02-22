package org.sarge.jove.widget;

/**
 * Label widget.
 * @see Drawable
 * @author Sarge
 */
public class LabelWidget extends Widget {
	private final Widget peer;
	
	private Drawable label;
	
	/**
	 * Constructor.
	 * @param label		Label
	 * @param peer		Peer widget of this label
	 */
	public LabelWidget( Drawable label, Widget peer ) {
		setLabel( label );
		this.peer = peer;
	}
	
	/**
	 * Constructor for a label without a peer.
	 * @param label Label
	 */
	public LabelWidget( Drawable label ) {
		this( label, null );
	}
	
	/**
	 * Sets the label.
	 * @param label Label
	 */
	public void setLabel( Drawable label ) {
		Check.notNull( label );
		this.label = label;
	}
	
	/**
	 * @return Peer widget of this label or <tt>null</tt> if none
	 */
	public Widget getPeer() {
		return peer;
	}
	
	@Override
	public Dimensions getDimensions() {
		return label.getDimensions();
	}
	
	@Override
	public void render( Object obj ) {
		label.render( null );
	}
}

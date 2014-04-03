package org.sarge.jove.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.FaceCulling;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.input.ActionBindings;
import org.sarge.jove.input.Device;
import org.sarge.jove.input.InputEvent;
import org.sarge.jove.input.InputEventBuffer;
import org.sarge.jove.material.DepthTestProperty;
import org.sarge.jove.scene.PerspectiveProjection;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.scene.RenderManager;
import org.sarge.jove.scene.RenderQueue;
import org.sarge.jove.scene.Scene;
import org.sarge.jove.scene.SceneNode;
import org.sarge.jove.task.RenderThreadTaskQueue;
import org.sarge.jove.util.ImageLoader;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.io.FileDataSource;
import org.sarge.lib.util.ToString;

/**
 * Base-class for demo applications.
 * @author Sarge
 */
public abstract class AbstractDemo implements Application {
	private final List<Scene> scenes = new ArrayList<>();
	private final FrameListenerGroup update = new FrameListenerGroup();
	private final DataSource src = new FileDataSource( new File( "./resource" ) );
	private final ActionBindings bindings = new ActionBindings();
	private final InputEventBuffer handler = new InputEventBuffer( bindings );
	private final RenderThreadTaskQueue queue = new RenderThreadTaskQueue();

	private ImageLoader imageLoader;

	@Override
	public String getTitle() {
		return this.getClass().getName();
	}

	@Override
	public final void init( Dimensions size, RenderingSystem sys ) throws Exception {
		// Create image loader
		imageLoader = sys.getImageLoader( src );

		// Create render manager
		final RenderManager mgr = new RenderManager( Arrays.asList( getRenderQueues() ) );

		// Create scene
		final Scene scene = new Scene( sys.createViewport(), new Rectangle( size ), new PerspectiveProjection(), mgr );
		scenes.add( scene );

		// Create root node
		final SceneNode root = new SceneNode( "root" );
		scene.setRoot( root );

		// Init default render properties
		sys.setFaceCulling( FaceCulling.BACK );
		sys.setDepthTest( new DepthTestProperty( "<" ) );

		// Init devices
		for( Device dev : sys.getDevices() ) {
			dev.start( handler );
		}

		// Init scene
		init( scene, root, sys );
	}

	/**
	 * Initialises this demo.
	 * @param scene		Default scene
	 * @param root		Scene-graph root node
	 * @param sys		Rendering system
	 * @throws Exception
	 */
	protected abstract void init( Scene scene, SceneNode root, RenderingSystem sys ) throws Exception;

	/**
	 * @return Render queues for this demo
	 */
	protected RenderQueue[] getRenderQueues() {
		return new RenderQueue[] {
				RenderQueue.Default.OPAQUE,
				RenderQueue.Default.SKY,
				RenderQueue.Default.TRANSLUCENT
		};
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public void render( RenderContext ctx ) {
		for( Scene scene : scenes ) {
			scene.render( ctx );
		}
	}

	@Override
	public void update( RenderContext ctx ) {
		update.update( ctx.getTime(), ctx.getElapsed() );
		queue.execute( ctx );
		handler.execute();
	}

	/**
	 * @return Action bindings for this demo
	 */
	protected ActionBindings getBindings() {
		return bindings;
	}

	@Override
	public void handle( InputEvent event ) {
		bindings.handle( event );
	}

	/**
	 * Adds a frame listener.
	 * @param listener Listener to add
	 */
	protected void add( FrameListener listener ) {
		update.add( listener );
	}

	/**
	 * @return Demo data source
	 */
	protected DataSource getDataSource() {
		return src;
	}

	/**
	 * @return Image/texture loader
	 */
	protected ImageLoader getImageLoader() {
		return imageLoader;
	}

	@Override
	public void close() {
		// Does nowt
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}

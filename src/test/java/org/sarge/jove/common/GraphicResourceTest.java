package org.sarge.jove.common;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class GraphicResourceTest {

	static class Thing {

	}

	//@Test
	public static void main(String[] args) {

		Thing t = new Thing();

		final ReferenceQueue<Thing> queue = new ReferenceQueue<>();

		//new WeakReference<Thing>( t, queue );
		final WeakReference<Thing> ref = new WeakReference<>( t, queue );



		System.out.println(" poll="+queue.poll());


		t = null;
		System.out.println(" poll="+queue.poll());


		while(true){
			System.gc();
			Object result = queue.poll();
			System.out.println(" poll="+result);

		if(result!=null)break;
		}
	}

}

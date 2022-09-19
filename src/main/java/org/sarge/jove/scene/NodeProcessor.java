package org.sarge.jove.scene;

import java.util.List;

public class NodeProcessor {

	/**
	 *
	 * @param root
	 * @return
	 */
	public List<Renderable> translate(SceneGraph scene) {

		scene.nodes();


		return null;
	}

}

/**
 *
 * scene graph
 * root.node flatten -> stream renderables
 *
 * group by render queue
 * apply ordering
 *
 * group by material
 * => bind material
 *
 * group by descriptor set
 * => bind descriptor set
 *
 * => renderables
 *
 * => collection render commands
 *
 *
 *
 */


//
//	private record MaterialGroup(Material mat, List<Renderable> objects) {
//
//		private static Collector<Renderable, ?, Map<Material, List<Renderable>>> collector() {
//			return groupingBy(Renderable::material);
//		}
//
//	}
//
//	private record RenderQueueGroup(RenderQueue queue, List<MaterialGroup> objects) {
//
//
//		private static Collector<Renderable, ?, Map<RenderQueue, Map<Material, List<Renderable>>>> collector() {
//			return groupingBy(r -> r.material().queue(), MaterialGroup.collector());
//		}
//
//	}
//
//
//
//	public void process(Node root) {
//
//		final Collector<Renderable, ?, Map<Material, List<Renderable>>> materials = Collectors.groupingBy(Renderable::material);
//		final Collector<Renderable, ?, Map<RenderQueue, Map<Material, List<Renderable>>>> queues = Collectors.groupingBy(r -> r.material().queue(), materials);
//
//		final Map<RenderQueue, Map<Material, List<Renderable>>> map = root
//				.render()
//				// TODO - culling
//				.collect(queues); //groupingBy(e -> e.material().queue()));
//	}
//}
//
//
////Map<City, Set<String>> lastNamesByCity
////*   = people.stream().collect(
////*     groupingBy(Person::getCity,
////*                mapping(Person::getLastName,
////*                        toSet())));

package org.sarge.jove.model.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.sarge.jove.model.Vertex;
import org.sarge.lib.util.Check;

/**
 * Polygon group.
 */
public class Group {
	private final String name;
	private final List<Vertex> vertices = new ArrayList<>();
	
	private ObjectMaterial mat;
	
	Group(String name) {
		Check.notNull(name);
		this.name = name;
	}
	
	/**
	 * @return Group name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Material for this group
	 */
	public Optional<ObjectMaterial> getMaterial() {
		return Optional.ofNullable(mat);
	}

	/**
	 * Assigns a material to this group.
	 * @param mat Material
	 * @throws IllegalArgumentException if this group already has a material
	 */
	void setMaterial(ObjectMaterial mat) {
		Check.notNull(mat);
		if(this.mat != null) throw new IllegalArgumentException("Material already set for this group");
		this.mat = mat;
	}
	
	/**
	 * Adds a vertex to this group.
	 * @param v Vertex
	 */
	void add(Vertex v) {
		vertices.add(v);
	}
	
	@Override
	public String toString() {
		return name;
	}
}

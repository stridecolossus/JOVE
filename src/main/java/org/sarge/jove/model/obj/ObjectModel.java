package org.sarge.jove.model.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sarge.jove.common.TextureCoordinate;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.ToString;

/**
 * Mutable <tt>OBJ</tt> model data.
 * @author Sarge
 */
public class ObjectModel {
	private final List<Group> groups = new ArrayList<>();
	private final Map<String, ObjectMaterial> materials = new StrictMap<>();
	private final List<Point> vertices = new ArrayList<>();
	private final List<Vector> normals = new ArrayList<>();
	private final List<TextureCoordinate> coords = new ArrayList<>();

	private int groupIndex = 1;
	private Group group = newGroup("group");
	private ObjectMaterial mat;

	/**
	 * Starts a new group.
	 * @param name Group name or <tt>null</tt> for an anonymous group
	 * @return New group
	 */
	public Group newGroup(String name) {
		// Build group name
		final String groupName;
		if(name == null) {
			groupName = "group-" + groupIndex;
			++groupIndex;
		}
		else {
			groupName = name;
		}

		// Start new group
		group = new Group(groupName);
		groups.add(group);
		return group;
	}
	
	/**
	 * @return Current group
	 */
	public Group getGroup() {
		return group;
	}

	/**
	 * Starts a new material.
	 * @param name Material name
	 * @return New material
	 */
	public ObjectMaterial newMaterial(String name) {
		mat = new ObjectMaterial();
		materials.put(name, mat);
		return mat;
	}

	/**
	 * @return Current material or <tt>null</tt> if none
	 * TODO - optional?
	 */
	public ObjectMaterial getMaterial() {
		return mat;
	}

	// TODO
	public ObjectMaterial getMaterial(String name) {
		return materials.get(name);
	}
	
	/**
	 * Adds a new vertex.
	 * @param pos Vertex position
	 */
	public void add(Point pos) {
		vertices.add(pos);
	}

	/**
	 * Looks up a vertex position by index.
	 * @param idx Index
	 * @return Vertex position
	 */
	public Point getVertex(int idx) {
		return get(vertices, idx);
	}

	/**
	 * Adds a vertex normal.
	 * @param normal normal
	 */
	public void add(Vector normal) {
		normals.add(normal);
	}

	/**
	 * Looks up a normal.
	 * @param idx Index
	 * @return Normal
	 */
	public Vector getNormal(int idx) {
		return get(normals, idx);
	}

	/**
	 * Adds texture coordinates.
	 * @param tc Texture coordinates
	 */
	public void add(TextureCoordinate tc) {
		coords.add(tc);
	}

	/**
	 * Looks up a texture coordinates.
	 * @param idx Index
	 * @return Texture coordinate
	 */
	public TextureCoordinate getTextureCoord(int idx) {
		return get(coords, idx);
	}

	/**
	 * Looks up a item from the given list by index.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>index can be negative relative to the size of the list</li>
	 * <li>indices start at <b>one</b></li>
	 * </ul>
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	private static <T> T get(List<T> list, int idx) {
		// Determine actual index
		final int actual;
		if(idx > 0) {
			// Convert to zero-based index
			actual = idx - 1;
		}
		else {
			// Index backwards
			actual = list.size() - Math.abs(idx);
		}

		// Verify index
		if((actual < 0) || (actual >= list.size())) {
			throw new IndexOutOfBoundsException(String.format("Invalid index: index=%d size=%d", idx, list.size()));
		}

		// Lookup
		return list.get(actual);
	}

	@Override
	public String toString() {
		final ToString ts = new ToString(this);
		ts.append("groups", groups.size());
		ts.append("materials", materials.size());
		ts.append("vertices", vertices.size());
		ts.append("normals", normals.size());
		ts.append("coords", coords.size());
		ts.append("current.group", group);
		// TODO - current material
		return ts.toString();
	}
}

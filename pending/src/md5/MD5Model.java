package org.sarge.jove.model.md5;

import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Quaternion;

public class MD5Model {
	static class Joint {
		String name;
		int parent;
		Point pos;
		Quaternion rot;

		int flags;
		int start;
		Point basePosition;
		Quaternion baseRot;
	}

	static class VertexData {
		Point pos;
		TextureCoord coords;
		int start;
		int count;
	}

	static class Weight {
		int jointIndex;
		float bias;
		Point pos;
	}

	static class Triangle {
		int[] index = new int[ 3 ];
	}

	static class MeshData {
		String texture;
		VertexData[] vertices;
		Triangle[] triangles;
		Weight[] weights;
	}

	Joint[] joints;
	MeshData[] meshes;

	public MD5Model( int numJoints, int numMeshes ) {
		joints = new Joint[ numJoints ];
		meshes = new MeshData[ numMeshes ];
	}
}

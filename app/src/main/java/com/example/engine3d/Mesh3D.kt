package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.max

data class Vertex3D(
    val position: Vector3,
    val normal: Vector3 = Vector3.UP
)

data class Polygon3D(
    val vertexIndices: IntArray,
    val baseColor: Color,
    val normal: Vector3 = Vector3.UP,
    val isDoubleSided: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Polygon3D) return false
        return vertexIndices.contentEquals(other.vertexIndices) && baseColor == other.baseColor
    }

    override fun hashCode(): Int {
        return 31 * vertexIndices.contentHashCode() + baseColor.hashCode()
    }
}

class Mesh3D {
    val vertices = mutableListOf<Vertex3D>()
    val polygons = mutableListOf<Polygon3D>()

    fun addBox(
        center: Vector3,
        size: Vector3,
        color: Color,
        topColor: Color = color,
        sideColor: Color = color
    ) {
        val half = size * 0.5f
        val startIndex = vertices.size

        // 8 corner vertices
        val corners = arrayOf(
            Vector3(center.x - half.x, center.y - half.y, center.z - half.z), // 0: LBB
            Vector3(center.x + half.x, center.y - half.y, center.z - half.z), // 1: RBB
            Vector3(center.x + half.x, center.y + half.y, center.z - half.z), // 2: RTB
            Vector3(center.x - half.x, center.y + half.y, center.z - half.z), // 3: LTB
            Vector3(center.x - half.x, center.y - half.y, center.z + half.z), // 4: LBF
            Vector3(center.x + half.x, center.y - half.y, center.z + half.z), // 5: RBF
            Vector3(center.x + half.x, center.y + half.y, center.z + half.z), // 6: RTF
            Vector3(center.x - half.x, center.y + half.y, center.z + half.z)  // 7: LTF
        )

        for (c in corners) {
            vertices.add(Vertex3D(c))
        }

        // Top face
        polygons.add(Polygon3D(intArrayOf(startIndex + 3, startIndex + 2, startIndex + 6, startIndex + 7), topColor, Vector3.UP))
        // Front face
        polygons.add(Polygon3D(intArrayOf(startIndex + 7, startIndex + 6, startIndex + 5, startIndex + 4), sideColor, Vector3.FORWARD))
        // Back face
        polygons.add(Polygon3D(intArrayOf(startIndex + 2, startIndex + 3, startIndex + 0, startIndex + 1), sideColor, Vector3(0f, 0f, -1f)))
        // Left face
        polygons.add(Polygon3D(intArrayOf(startIndex + 3, startIndex + 7, startIndex + 4, startIndex + 0), sideColor, Vector3(-1f, 0f, 0f)))
        // Right face
        polygons.add(Polygon3D(intArrayOf(startIndex + 6, startIndex + 2, startIndex + 1, startIndex + 5), sideColor, Vector3(1f, 0f, 0f)))
        // Bottom face
        polygons.add(Polygon3D(intArrayOf(startIndex + 4, startIndex + 5, startIndex + 1, startIndex + 0), sideColor, Vector3(0f, -1f, 0f)))
    }

    fun addQuad(
        p0: Vector3,
        p1: Vector3,
        p2: Vector3,
        p3: Vector3,
        color: Color,
        doubleSided: Boolean = false
    ) {
        val startIndex = vertices.size
        val edge1 = p1 - p0
        val edge2 = p3 - p0
        val norm = edge1.cross(edge2).normalized()

        vertices.add(Vertex3D(p0, norm))
        vertices.add(Vertex3D(p1, norm))
        vertices.add(Vertex3D(p2, norm))
        vertices.add(Vertex3D(p3, norm))

        polygons.add(
            Polygon3D(
                intArrayOf(startIndex, startIndex + 1, startIndex + 2, startIndex + 3),
                color,
                norm,
                doubleSided
            )
        )
    }

    fun addTriangle(
        p0: Vector3,
        p1: Vector3,
        p2: Vector3,
        color: Color,
        doubleSided: Boolean = true
    ) {
        val startIndex = vertices.size
        val edge1 = p1 - p0
        val edge2 = p2 - p0
        val norm = edge1.cross(edge2).normalized()

        vertices.add(Vertex3D(p0, norm))
        vertices.add(Vertex3D(p1, norm))
        vertices.add(Vertex3D(p2, norm))

        polygons.add(
            Polygon3D(
                intArrayOf(startIndex, startIndex + 1, startIndex + 2),
                color,
                norm,
                doubleSided
            )
        )
    }
}

// Renderable polygon ready to draw onto screen
data class ScreenPolygon(
    val points: List<Offset>,
    val depth: Float,
    val color: Color
)

package org.lwjgl.demo.opengl;


import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.demo.util.Color4D;
import org.lwjgl.demo.util.OGLApp;
import org.lwjgl.demo.util.OGLModel3D;
import org.lwjgl.demo.util.OGLObject;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.nio.FloatBuffer;

import static org.joml.Math.PI;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_LINES;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20C.glUniform3fv;
import static org.lwjgl.opengl.GL20C.glUniform4fv;
import static org.lwjgl.opengl.GL20C.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20C.glUniformMatrix4fv;


public class Icosidodecahedron3D extends OGLApp<Icosidodecahedron> {
    public Icosidodecahedron3D(Icosidodecahedron model) {
        super(model);

        m_keyCallback = (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            else if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_LEFT:
                        model.changeYangle(0.125);
                        break;
                    case GLFW_KEY_RIGHT:
                        model.changeYangle(-0.125);
                        break;
                    case GLFW_KEY_UP:
                        model.changeXangle(0.125);
                        break;
                    case GLFW_KEY_DOWN:
                        model.changeXangle(-0.125);
                        break;
                    case GLFW_KEY_SPACE:
                        model.setM_dxAngle(0);
                        model.setM_dyAngle(0);
                }
            }
        };
    }

    public static void main(String[] args) {
        new Icosidodecahedron3D(new Icosidodecahedron()).run("Icosidodecahedron", 800, 800, new Color4D(0.6f, 0.6f, 0.6f, 1));
    }
}


class Icosidodecahedron extends OGLModel3D {
    // configuration parameters
    // length of one Icosidodecahedron edge
    final static float _s = 2f;
    final static double deg2rad = PI / 180;

    // Phi from the formula (golden ratio)
    final static float _phi = (float) ((1 + Math.sqrt(5)) / 2);

    private final Matrix3d m_vm = new Matrix3d();
    private final Vector3d m_light = new Vector3d();
    private final FloatBuffer m_vec3f = BufferUtils.createFloatBuffer(3);
    private final FloatBuffer m_mat3f = BufferUtils.createFloatBuffer(3 * 3);
    private final FloatBuffer m_mat4f = BufferUtils.createFloatBuffer(4 * 4);

    private TriangleSide t_side;
    private Triangle tri;
    private double m_startTime = System.currentTimeMillis() / 1000.0;
    private double m_distance = 15.0f;    // camera distance
    private double m_dxAngle = 0;        // degrees

    public void setM_dxAngle(double m_dxAngle) {
        this.m_dxAngle = m_dxAngle;
    }

    public void setM_dyAngle(double m_dyAngle) {
        this.m_dyAngle = m_dyAngle;
    }

    private double m_dyAngle = 0;        // degrees
    private double m_xAngle = 0;        // degrees
    private double m_yAngle = 0;        // degrees
    private double m_zAngle = 0;        // degrees
    private long m_count;

    private static final Color4D[] COLORS = {
            new Color4D(1, 0, 0, 0.5f),        // Red
            new Color4D(0, 1, 0, 0.5f),        // Green
            new Color4D(0, 0, 1, 0.5f),        // Blue
            new Color4D(1, 1, 0, 0.5f),        // Yellow
            new Color4D(1, 0, 1, 0.5f),        // Magenta
            new Color4D(0, 1, 1, 0.5f),        // Cyan
            new Color4D(1, 0.5f, 0, 0.5f),     // Orange
            new Color4D(0.5f, 0, 1, 0.5f),     // Purple
            new Color4D(0, 0.5f, 1, 0.5f),     // Light Blue
            new Color4D(0.5f, 1, 0, 0.5f),     // Lime
            new Color4D(1, 0, 0.5f, 0.5f),     // Pink
            new Color4D(0.5f, 0.5f, 0.5f, 0.5f), // Gray
            new Color4D(0, 0, 0, 0.5f),        // Black
            new Color4D(1, 1, 1, 0.5f),        // White
            new Color4D(0.8f, 0.2f, 0.6f, 0.5f), // Orchid
            new Color4D(0.2f, 0.6f, 0.8f, 0.5f), // Sky Blue
            new Color4D(0.4f, 0.2f, 0.8f, 0.5f), // Indigo
            new Color4D(0.8f, 0.8f, 0.2f, 0.5f), // Yellow Green
            new Color4D(0.8f, 0.2f, 0.2f, 0.5f), // Crimson
            new Color4D(0.2f, 0.8f, 0.2f, 0.5f)  // Lime Green
    };

    @Override
    public void init(int width, int height) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        super.init(width, height);
        this.tri = new Triangle(new Color4D(1, 1, 1, 1));
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // VIEW
        V.translation(0.0, 0.0, -m_distance).rotateX(m_xAngle * deg2rad).rotateY(m_yAngle * deg2rad).rotateZ(m_zAngle * deg2rad); // V = T*Rx*Ry*Rz

        // Light
        glUniform3fv(u_LIGHT, m_light.set(0.0, 0.0, 10.0).normalize().get(m_vec3f)); // V*m_light


        // Lichtquellenpositionen
        Vector3f lightPositions[] = {
                new Vector3f(1 * _s, 1 * _s, 1 * _s),
        };

        // Lichtfarben
        Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

        for (int i = 0; i < lightPositions.length; i++) {
            glUniform3f(u_LIGHT, lightPositions[i].x, lightPositions[i].y, lightPositions[i].z);
            glUniform3f(u_LIGHT, color.x, color.y, color.z);
        }

        Vector3f[] verticesIcosahedron = {
                new Vector3f(0, _s / 2, _s * _phi / 2),
                new Vector3f(0, _s / 2, -_s * _phi / 2),
                new Vector3f(0, -_s / 2, _s * _phi / 2),
                new Vector3f(0, -_s / 2, -_s * _phi / 2),
                new Vector3f(_s * _phi / 2, 0, _s / 2),
                new Vector3f(_s * _phi / 2, 0, -_s / 2),
                new Vector3f(-_s * _phi / 2, 0, _s / 2),
                new Vector3f(-_s * _phi / 2, 0, -_s / 2),
                new Vector3f(_s / 2, _s * _phi / 2, 0),
                new Vector3f(-_s / 2, _s * _phi / 2, 0),
                new Vector3f(_s / 2, -_s * _phi / 2, 0),
                new Vector3f(-_s / 2, -_s * _phi / 2, 0)
        };

        int[][] indices = {
                {0, 8, 9},
                {0, 4, 8},
                {4, 5, 8},
                {5, 1, 8},
                {1, 9, 8},
                {7, 9, 1},
                {7, 6, 9},
                {6, 0, 9},
                {2, 4, 0},
                {2, 10, 4},
                {10, 5, 4},
                {10, 3, 5},
                {3, 1, 5},
                {3, 7, 1},
                {11, 7, 3},
                {11, 6, 7},
                {11, 2, 6},
                {2, 0, 6},
                {10, 11, 3},
                {11, 10, 2},
        };

        renderCoordinateSystem();
        // source is directly initialized at z = 0. therefore the normal points towards z+ which is 1.
        Vector3f norm_source = new Vector3f(0, 0, 1);

        for (int i = 0; i < indices.length; i++) {
            Vector3f target_center = new Vector3f();
            target_center.x = (verticesIcosahedron[indices[i][0]].x + verticesIcosahedron[indices[i][1]].x + verticesIcosahedron[indices[i][2]].x) / 3;
            target_center.y = (verticesIcosahedron[indices[i][0]].y + verticesIcosahedron[indices[i][1]].y + verticesIcosahedron[indices[i][2]].y) / 3;
            target_center.z = (verticesIcosahedron[indices[i][0]].z + verticesIcosahedron[indices[i][1]].z + verticesIcosahedron[indices[i][2]].z) / 3;

            Vector3f norm_target = new Vector3f(target_center.x, target_center.y, target_center.z);

            // TODO rules for rotation of x, y and z
            float y_rotation = (float) Math.toRadians(0);
            float x_rotation = (float) Math.toRadians(0);
            float z_rotation = (float) Math.toRadians(60);

            /*
            // pieces on one axis == 0: front
            if (target_center.x == 0 || target_center.y == 0 || target_center.z == 0) {
                if (target_center.x == 0) {
                    x_rotation = -norm_source.angle(norm_target);
                    M.translation(norm_target).rotateX(x_rotation).rotateZ(z_rotation);
                }

                if (target_center.y == 0) {
                    x_rotation = -norm_source.angle(norm_target);
                    z_rotation = (float) -(Math.PI / 2);

                    M.translation(norm_target).rotateZ(z_rotation).rotateX(x_rotation);
                    drawTri(tri.setRGBA(0, 1, 0, 1));
                }

                if (target_center.z == 0) {
                    y_rotation = (float) (Math.PI / 2);

                    Vector3f norm_dash = new Vector3f(1, 0, 0);

                    x_rotation = -norm_dash.angle(norm_target);
                    M.translation(norm_target).rotateY(y_rotation).rotateX(x_rotation);
                }

                drawTri(tri.setRGBA(0, 1, 0, 1));
            }


            // Schräges Element vorne Rechts, 45° von jeder Ebene => equals to Vector that points to 1,1,1 (normalized)
            Vector3f norm = new Vector3f(1, 1, 1).normalize();
            System.out.println(norm);
            System.out.println(norm_target);
            if (norm.dot(norm_target) > 0) {
                // Schnittlinie berechnen
                Vector3f cross = norm_source.cross(norm_target);

                float new_norm = (float) (1/Math.sqrt(3));

                M.translation(norm_target);//.rotate(Math.toRadians(60), cross);
                drawTri(tri.setRGBA(0, 1, 0, 1));
            }
             */

            // Schnittgerade
            Vector3f cross = norm_source.cross(norm_target).normalize();

            M.translation(norm_target).rotate(Math.toRadians(0), cross);
            drawTri(tri.setRGBA(0, 0, 1, 1));
        }


        M.translation(0, 0, 0);
        for (int i = 0; i < 20; i++) {
            Vector3f[] v = {
                    verticesIcosahedron[indices[i][0]],
                    verticesIcosahedron[indices[i][1]],
                    verticesIcosahedron[indices[i][2]]
            };

            drawTriangle(new TriangleSide(COLORS[i], v));
        }



        //fps
        m_count++;

        double theTime = System.currentTimeMillis() / 1000.0;
        if (theTime >= m_startTime + 1.0) {
            System.out.format("%d fps\n", m_count); // falls die fps zu hoch sind: https://www.khronos.org/opengl/wiki/Swap_Interval#In_Windows
            m_startTime = theTime;
            m_count = 0;
        }

        // animation
        m_xAngle -= m_dxAngle;
        m_yAngle -= m_dyAngle;
    }

    private void renderCoordinateSystem() {
        glPushMatrix(); // Save the current matrix

        // Enable line smoothing
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        // Set line width
        GL11.glLineWidth(2.0f);
        glBegin(GL_LINES);

        // X axis
        glColor3f(1.0f, 0.0f, 0.0f);
        glVertex3f(-100.0f, 0.0f, 0.0f);
        glVertex3f(100.0f, 0.0f, 0.0f);

        // Y axis
        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(0.0f, -100.0f, 0.0f);
        glVertex3f(0.0f, 100.0f, 0.0f);

        // Z axis
        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex3f(0.0f, 0.0f, -100.0f);
        glVertex3f(0.0f, 0.0f, 100.0f);

        glEnd();
        glPopMatrix(); // Restore the previous matrix
    }

    public void changeXangle(double delta) {
        m_dxAngle += delta;
    }

    public void changeYangle(double delta) {
        m_dyAngle += delta;
    }

    private void drawTri(Triangle side) {
        // set geometric transformation matrices for all vertices of this model
        glUniformMatrix3fv(u_VM, false, V.mul(M, VM).normal(m_vm).get(m_mat3f));
        glUniformMatrix4fv(u_PVM, false, P.mul(VM, PVM).get(m_mat4f)); // get: stores in and returns m_mat4f

        // set color for all vertices of this model
        glUniform4fv(u_COLOR, side.getColor());

        // draw a triangle
        side.setupPositions(m_POSITIONS);
        side.setupNormals(m_NORMALS);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, side.getVertexCount());
    }

    private void drawTriangle(TriangleSide side) {
        // set geometric transformation matrices for all vertices of this model
        glUniformMatrix3fv(u_VM, false, V.mul(M, VM).normal(m_vm).get(m_mat3f));
        glUniformMatrix4fv(u_PVM, false, P.mul(VM, PVM).get(m_mat4f)); // get: stores in and returns m_mat4f

        // set color for all vertices of this model
        glUniform4fv(u_COLOR, side.getColor());

        // draw a triangle
        side.setupPositions(m_POSITIONS);
        side.setupNormals(m_NORMALS);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, side.getVertexCount());
    }

    // the second form that is a side of a Icosidodecahedron is a triangle (each side same length)
    private static class Triangle extends OGLObject {
        final static int coordinatesPerVertex = 3;

        protected Triangle(Color4D color) {
            super(color);

            final int nVertices = 3; // three points for a triangle
            final int nCoordinates = nVertices * coordinatesPerVertex; // each coordinate triple used for one vertex

            // Allocate vertex positions and normals
            allocatePositionBuffer(nCoordinates);
            allocateNormalBuffer(nCoordinates);

            // we want to cut all the edges in half to get vertices for Icosidodecahedron
            float h = (float) ((Math.sqrt(3) / 2) * _s);
            float v1[] = {-_s / 2, -h / 3, 0};
            float v2[] = {_s / 2, -h / 3, 0};
            float v3[] = {0, h - h / 3, 0};

            addVertex(v1[0], v1[1], v1[2]);
            addVertex(v2[0], v2[1], v2[2]);
            addVertex(v3[0], v3[1], v3[2]);

            // Bind vertex positions and normals
            bindPositionBuffer();
            bindNormalBuffer();
        }

        private void addVertex(float x, float y, float z) {
            m_positions.put(m_vertexCount * coordinatesPerVertex + 0, x);
            m_positions.put(m_vertexCount * coordinatesPerVertex + 1, y);
            m_positions.put(m_vertexCount * coordinatesPerVertex + 2, z);

            m_normals.put(m_vertexCount * coordinatesPerVertex + 0, 0);
            m_normals.put(m_vertexCount * coordinatesPerVertex + 1, 0);
            m_normals.put(m_vertexCount * coordinatesPerVertex + 2, 1);

            m_vertexCount++;
        }

        public Triangle setRGBA(float r, float g, float b, float a) {
            m_color.put(0, r);
            m_color.put(1, g);
            m_color.put(2, b);
            m_color.put(3, a);
            return this;
        }
    }

    // the second form that is a side of a Icosidodecahedron is a triangle (each side same length)
    private static class TriangleSide extends OGLObject {
        final static int coordinatesPerVertex = 3;

        protected TriangleSide(Color4D color, Vector3f[] v_coords) {
            super(color);

            final int nVertices = 3; // three points for a triangle
            final int nCoordinates = nVertices * coordinatesPerVertex; // each coordinate triple used for one vertex

            // Allocate vertex positions and normals
            allocatePositionBuffer(nCoordinates);
            allocateNormalBuffer(nCoordinates);


            for (int i = 0; i < nVertices; i++) {
                Vector3f v = v_coords[i];
                addVertex(v.x, v.y, v.z);
            }

            // Bind vertex positions and normals
            bindPositionBuffer();
            bindNormalBuffer();
        }

        private void addVertex(float x, float y, float z) {
            m_positions.put(m_vertexCount * coordinatesPerVertex + 0, x);
            m_positions.put(m_vertexCount * coordinatesPerVertex + 1, y);
            m_positions.put(m_vertexCount * coordinatesPerVertex + 2, z);

            m_normals.put(m_vertexCount * coordinatesPerVertex + 0, 0);
            m_normals.put(m_vertexCount * coordinatesPerVertex + 1, 0);
            m_normals.put(m_vertexCount * coordinatesPerVertex + 2, 1);

            m_vertexCount++;
        }

        public TriangleSide setRGBA(float r, float g, float b, float a) {
            m_color.put(0, r);
            m_color.put(1, g);
            m_color.put(2, b);
            m_color.put(3, a);
            return this;
        }
    }
}

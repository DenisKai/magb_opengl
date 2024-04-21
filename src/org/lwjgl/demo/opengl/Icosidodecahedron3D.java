package org.lwjgl.demo.opengl;


import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.demo.util.Color4D;
import org.lwjgl.demo.util.OGLApp;
import org.lwjgl.demo.util.OGLModel3D;
import org.lwjgl.demo.util.OGLObject;

import java.nio.FloatBuffer;

import static org.joml.Math.PI;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_POLYGON;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
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
    final static float _radius = 1f;
    final static double deg2rad = PI / 180;

    // Phi from the formula (golden ratio)
    final static float _phi = (float) ((1 + Math.sqrt(5)) / 2);
    //side length of pentagon a
    final static double _a_p = _radius * Math.sqrt((5 - Math.sqrt(5)) / 2);
    // height of triangle
    final static float _h_t = (float) (_a_p * Math.sqrt(3) / 2);

    private final Matrix3d m_vm = new Matrix3d();
    private final Vector3d m_light = new Vector3d();
    private final FloatBuffer m_vec3f = BufferUtils.createFloatBuffer(3);
    private final FloatBuffer m_mat3f = BufferUtils.createFloatBuffer(3 * 3);
    private final FloatBuffer m_mat4f = BufferUtils.createFloatBuffer(4 * 4);

    private PentagonSide p_side;
    private TriangleSide t_side;
    private double m_startTime = System.currentTimeMillis() / 1000.0;
    private double m_distance = 15.0f;    // camera distance
    private double m_dxAngle = 0;        // degrees
    private double m_dyAngle = 0;        // degrees
    private double m_xAngle = 0;        // degrees
    private double m_yAngle = 0;        // degrees
    private double m_zAngle = 0;        // degrees
    private long m_count;

    @Override
    public void init(int width, int height) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        super.init(width, height);
        t_side = new TriangleSide(new Color4D(0, 0, 0, 1));
        p_side = new PentagonSide(new Color4D(0, 0, 0, 1));
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // VIEW
        V.translation(0.0, 0.0, -m_distance).rotateX(m_xAngle * deg2rad).rotateY(m_yAngle * deg2rad).rotateZ(m_zAngle * deg2rad); // V = T*Rx*Ry*Rz

        // LIGHT
        glUniform3fv(u_LIGHT, m_light.set(0.0, 0.0, 10.0).normalize().get(m_vec3f)); // V*m_light


        // Render outside
        //inner-circle radius pentagon r_i
        double r_i = _a_p * 0.5 * Math.sqrt(1 + (2 / Math.sqrt(5)));

        for (int i = 0; i < 5; i++) {
            M.rotationZ(Math.toRadians(i * 72)).translate(0, -(r_i + _h_t), 1).rotateX(Math.toRadians(10)).translate(0, 0, -Math.sin(Math.toRadians(10)));
            drawTriangle(t_side.setRGBA(1, 0.5f, 0, 0.5f));
        }


        M.translation(0, 0, 1);
        drawPentagon(p_side.setRGBA(1, 0, 0, 1f));

        // use radians for rotation.
        //M.rotationZ(Math.PI).rotateX(Math.PI/4).translate(0, 0, 1.1f);
        //drawPentagon(p_side.setRGBA(0, 0, 1, 0.5f));

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

    public void changeXangle(double delta) {
        m_dxAngle += delta;
    }

    public void changeYangle(double delta) {
        m_dyAngle += delta;
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

    private void drawPentagon(PentagonSide side) {
        // set geometric transformation matrices for all vertices of this model
        glUniformMatrix3fv(u_VM, false, V.mul(M, VM).normal(m_vm).get(m_mat3f));
        glUniformMatrix4fv(u_PVM, false, P.mul(VM, PVM).get(m_mat4f)); // get: stores in and returns m_mat4f

        // set color for all vertices of this model
        glUniform4fv(u_COLOR, side.getColor());

        // draw a triangle
        side.setupPositions(m_POSITIONS);
        side.setupNormals(m_NORMALS);
        glDrawArrays(GL_POLYGON, 0, side.getVertexCount());
    }

    // the first form that a Icosidodecahedron has is a pentagon (each side same length)
    private static class PentagonSide extends OGLObject {
        final static int coordinatesPerVertex = 3;
        // inner angle: 360/5 = 72
        final double innerAngle = Math.toRadians(72);

        protected PentagonSide(Color4D color) {
            super(color);

            final int nVertices = 5; // Five points for a pentagon
            final int nCoordinates = nVertices * coordinatesPerVertex; // Each coordinate triple used for one vertex

            // Allocate vertex positions and normals
            allocatePositionBuffer(nCoordinates);
            allocateNormalBuffer(nCoordinates);


            float vertices[][] = new float[nVertices][coordinatesPerVertex];
            // calculate vertices
            for (int i = 0; i < nVertices; i++) {
                // change x/y calculation due to right-handed system(?)
                float x = (float) (Math.sin(innerAngle * i) * _radius);
                float y = (float) (Math.cos(innerAngle * i) * _radius);

                vertices[i][0] = x;
                vertices[i][1] = y;
                vertices[i][2] = 0f;
            }

            // add vertices in counter-clockwise fashion
            addVertex(vertices[0][0], vertices[0][1], vertices[0][2]); // top, needs to be added manually
            System.out.println(vertices[0][0]);
            System.out.println(vertices[0][1]);
            for (int i = vertices.length - 1; i > 0; i--) {
                addVertex(vertices[i][0], vertices[i][1], vertices[i][2]);
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

        public PentagonSide setRGBA(float r, float g, float b, float a) {
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

        protected TriangleSide(Color4D color) {
            super(color);

            final int nVertices = 3; // three points for a triangle
            final int nCoordinates = nVertices * coordinatesPerVertex; // each coordinate triple used for one vertex

            // Allocate vertex positions and normals
            allocatePositionBuffer(nCoordinates);
            allocateNormalBuffer(nCoordinates);


            addVertex(0, 0, 0);
            addVertex((float) (_a_p / 2), _h_t, 0);
            addVertex((float) (-_a_p / 2), _h_t, 0);


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

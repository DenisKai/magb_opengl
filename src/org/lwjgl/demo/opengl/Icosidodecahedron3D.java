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

import java.nio.FloatBuffer;

import static org.joml.Math.PI;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_U;
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
                        break;
                    case GLFW_KEY_U:
                        model.scaleUp(0.2f);
                        break;
                    case GLFW_KEY_J:
                        model.scaleDown(0.2f);
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
    // length of one Icosidodecahedron edge
    static float _s = 1f;
    // Phi from the formula (golden ratio)
    final static float _phi = (float) ((1 + Math.sqrt(5)) / 2);
    // Phi with factor of length (use this in calculations of vertices)
    static float s_phi = _s * _phi;


    final static double deg2rad = PI / 180;


    private final Matrix3d m_vm = new Matrix3d();
    private final Vector3d m_light = new Vector3d();
    private final FloatBuffer m_vec3f = BufferUtils.createFloatBuffer(3);
    private final FloatBuffer m_mat3f = BufferUtils.createFloatBuffer(3 * 3);
    private final FloatBuffer m_mat4f = BufferUtils.createFloatBuffer(4 * 4);

    private TriangleSide t_side;
    private Triangle triangle;
    private Pentagon pentagon;
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

    @Override
    public void init(int width, int height) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        super.init(width, height);
        this.triangle = new Triangle(new Color4D(0, 0, 0, 1));
        this.pentagon = new Pentagon(new Color4D(0, 0, 0, 1));
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // VIEW
        V.translation(0.0, 0.0, -m_distance).rotateX(m_xAngle * deg2rad).rotateY(m_yAngle * deg2rad).rotateZ(m_zAngle * deg2rad); // V = T*Rx*Ry*Rz

        // Light
        glUniform3fv(u_LIGHT, m_light.set(0.0, 0.0, 10.0).normalize().get(m_vec3f)); // V*m_light

        // rotation test
        Vector3f a = new Vector3f(_s / 2, s_phi / 2, (s_phi * s_phi) / 2);
        a.rotateX((float) (Math.PI / 2)).rotateY((float) (Math.PI / 2));

        renderCoordinateSystem();
        M.translation(1, 0, 0);
        drawTriangle(triangle.setRGBA(1, 0, 0, 1));

        M.translation(0, 0, 0.1);
        drawPentagon(pentagon.setRGBA(0, 0, 1, 1));


        // Definition: Aussenradius gleich phi wenn Edge = 1
        // Ein Set der Ecken liegt auf einem Oktaeder, welche eine innere Länge/Breite/Höhe zwischen zwei gegenüberliegenden Ecken von 2 phi besitzt
        // Daher sind die Eckpunkte bei (+- _s * phi, 0, 0) und seinen Permutationen. (6 Vertices)
        Vector3f[] octahedron_vertices = getOctahedron_vertices();

        // Ein anderes Set liegt auf den Ecken eines goldenen Rechteckes.
        // Alle Punkte sind mit sign Flip + geraden Permutation dieser Formel zu berechnen: (+-_s/2, +- _s*_phi / 2,+- _s*_phi**2) (24 Vertices)
        Vector3f[] goldenRect_vertices = getGoldenRectangleVertices();

        // Normale zeigt Richtung Z-Positiv (Vertices), gilt für jede neue Seite.
        Vector3f normal_side = new Vector3f(0, 0, 1);

        //Upper Pentagon (facing z-positive) (IGWCZ)
        Vector3f[] p_vertices = {
                goldenRect_vertices[1],
                goldenRect_vertices[0],
                goldenRect_vertices[16],
                octahedron_vertices[2],
                goldenRect_vertices[17],
        };
        // Normale berechnen die durch Mittelpunkt geht
        Vector3f p_normal = calculateMiddlePoint(p_vertices);
        // Rotation um Seite anzugleichen
        float p_angle = -normal_side.angle(p_normal);


        // Zeichnen und Verschieben/Drehen der Pentagons
        M.translation(p_normal.x, p_normal.y, p_normal.z).rotateX(p_angle);
        drawPentagon(pentagon.setRGBA(1, 0, 0, 1));

        M.translation(p_normal.x, p_normal.y, -p_normal.z).rotateY(Math.PI).rotateX(p_angle);
        drawPentagon(pentagon.setRGBA(1, 1, 0, 1));

        M.translation(p_normal.x, -p_normal.y, p_normal.z).rotateZ(Math.PI).rotateX(p_angle);
        drawPentagon(pentagon.setRGBA(1, 0, 0, 1));

        M.translation(-p_normal.x, -p_normal.y, -p_normal.z).rotateY(Math.PI).rotateZ(Math.PI).rotateX(p_angle);
        drawPentagon(pentagon.setRGBA(1, 0, 0, 1));


        // Upper Triangle (facing Z-positive) (EGI)
        Vector3f[] vertices_t = {
                octahedron_vertices[4],
                goldenRect_vertices[0],
                goldenRect_vertices[1]
        };
        Vector3f t_normal = calculateMiddlePoint(vertices_t);
        float t_angle = normal_side.angle(t_normal);

        //Zeichnen und Verschieben/Drehen der Triangles
        M.translation(t_normal.x, t_normal.y, t_normal.z).rotateZ(Math.PI).rotateX(t_angle);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));

        M.translation(t_normal.x, -t_normal.y, t_normal.z).rotateX(t_angle);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));

        M.translation(t_normal.x, t_normal.y, -t_normal.z).rotateY(Math.PI).rotateZ(Math.PI).rotateX(t_angle);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));

        M.translation(t_normal.x, -t_normal.y, -t_normal.z).rotateY(Math.PI).rotateX(t_angle);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));


        // Mit dem Vertauschen der Achsen können auch die Dreiecke gezeichnet werden, die nicht Symmetrisch auf einer/zwei Achsen bzw. Mittelpunkt sind.
        // liegende Box
        Vector3f t_normal_lie = new Vector3f();
        t_normal.rotateY((float) (Math.PI / 2), t_normal_lie).rotateX((float) (Math.PI / 2), t_normal_lie);
        // Gegenwinkel des Originals (90 - normal_side.angle(t_normal))
        float t_angle_lie = -normal_side.angle(t_normal_lie);

        M.translation(t_normal_lie.x, t_normal_lie.y, t_normal_lie.z).rotateY(-t_angle_lie).rotateZ(-Math.PI / 2);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));

        M.translation(t_normal_lie.x, t_normal_lie.y, -t_normal_lie.z).rotateY(Math.PI).rotateY(t_angle_lie).rotateZ(Math.PI / 2);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));

        M.translation(-t_normal_lie.x, t_normal_lie.y, t_normal_lie.z).rotateY(t_angle_lie).rotateZ(Math.PI / 2);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));

        M.translation(-t_normal_lie.x, t_normal_lie.y, -t_normal_lie.z).rotateY(-Math.PI).rotateY(-t_angle_lie).rotateZ(-Math.PI / 2);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));


        // Das gleiche für die stehende Box
        Vector3f t_normal_s = new Vector3f();
        t_normal.rotateX((float) (Math.PI / 2), t_normal_s).rotateY((float) (Math.PI / 2), t_normal_s);

        //+90, da Achse gedreht ist und Winkel von Original kann übernommen werden
        M.translation(t_normal_s.x, t_normal_s.y, t_normal_s.z).rotateY(-Math.PI / 2).rotateX((Math.PI / 2) + t_angle);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));

        M.translation(-t_normal_s.x, t_normal_s.y, t_normal_s.z).rotateY(Math.PI / 2).rotateX((Math.PI / 2) + t_angle);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));

        M.translation(t_normal_s.x, -t_normal_s.y, t_normal_s.z).rotateY(Math.PI / 2).rotateX(-(Math.PI / 2) + t_angle);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));

        M.translation(-t_normal_s.x, -t_normal_s.y, t_normal_s.z).rotateY(-Math.PI / 2).rotateX(-(Math.PI / 2) + t_angle);
        drawTriangle(triangle.setRGBA(0, 1, 0, 1));


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

    // Vector der zu Mittelpunkt einer Fläche führt
    private Vector3f calculateMiddlePoint(Vector3f[] vertices) {
        Vector3f v_middle = new Vector3f();

        //calculate v_middle
        for (Vector3f v : vertices) {
            v_middle.add(v.x, v.y, v.z);
        }

        v_middle.mul(1f / vertices.length);
        return v_middle;
    }

    // Eigentlich könnten die restlichen 16 vertices berechnet werden, indem die Bestehenden 8 Vertices schlau gedreht werden.
    // Lösung mit Drehen wäre: Vertice nehmen und x->y je 90° drehen, sowie y->x je 90° drehen.
    // Ich habe mich dafür entschieden ein Loopup table zu machen.
    private Vector3f[] getGoldenRectangleVertices() {
        // Visualisierung auf Geogebra: https://www.geogebra.org/classic/y2k4gwwj
        Vector3f[] goldenRectangle_vertices = {
                // Rechteck das in Z-Richtung schaut.
                new Vector3f(_s / 2, s_phi / 2, (((_phi * _phi) * _s)) / 2), //G
                new Vector3f(-_s / 2, s_phi / 2, (((_phi * _phi) * _s)) / 2), //I
                new Vector3f(-_s / 2, -s_phi / 2, (((_phi * _phi) * _s)) / 2), //J
                new Vector3f(_s / 2, -s_phi / 2, (((_phi * _phi) * _s)) / 2),  //H
                new Vector3f(-_s / 2, s_phi / 2, -(((_phi * _phi) * _s)) / 2), //M
                new Vector3f(_s / 2, s_phi / 2, -(((_phi * _phi) * _s)) / 2), //K
                new Vector3f(_s / 2, -s_phi / 2, -(((_phi * _phi) * _s)) / 2), //L
                new Vector3f(-_s / 2, -s_phi / 2, -(((_phi * _phi) * _s)) / 2), //N
                // 'Gelegtes' Rechteck
                new Vector3f((((_phi * _phi) * _s)) / 2, _s / 2, s_phi / 2), //O
                new Vector3f(-(((_phi * _phi) * _s)) / 2, _s / 2, s_phi / 2), //P
                new Vector3f(-(((_phi * _phi) * _s)) / 2, -_s / 2, s_phi / 2), //S
                new Vector3f((((_phi * _phi) * _s)) / 2, -_s / 2, s_phi / 2), //Q
                new Vector3f(-(((_phi * _phi) * _s)) / 2, _s / 2, -s_phi / 2), //T
                new Vector3f((((_phi * _phi) * _s)) / 2, _s / 2, -s_phi / 2), //R
                new Vector3f((((_phi * _phi) * _s)) / 2, -_s / 2, -s_phi / 2), //U
                new Vector3f(-(((_phi * _phi) * _s)) / 2, -_s / 2, -s_phi / 2), //V
                // Hochgestelltes Rechteck (phis / 2, phis² / 2, s / 2)
                new Vector3f(s_phi / 2, (((_phi * _phi) * _s)) / 2, _s / 2), //W
                new Vector3f(-s_phi / 2, (((_phi * _phi) * _s)) / 2, _s / 2), //Z
                new Vector3f(-s_phi / 2, -(((_phi * _phi) * _s)) / 2, _s / 2), //C_1
                new Vector3f(s_phi / 2, -(((_phi * _phi) * _s)) / 2, _s / 2), //A_1
                new Vector3f(-s_phi / 2, (((_phi * _phi) * _s)) / 2, -_s / 2), //D_1
                new Vector3f(s_phi / 2, (((_phi * _phi) * _s)) / 2, -_s / 2), //B_1
                new Vector3f(s_phi / 2, -(((_phi * _phi) * _s)) / 2, -_s / 2), //E_1
                new Vector3f(-s_phi / 2, -(((_phi * _phi) * _s)) / 2, -_s / 2), //F_1
        };

        return goldenRectangle_vertices;
    }

    // Permutationen von (+- _s * phi, 0, 0)
    private Vector3f[] getOctahedron_vertices() {
        return new Vector3f[]{
                new Vector3f(s_phi, 0, 0), //A
                new Vector3f(-s_phi, 0, 0), //B
                new Vector3f(0, s_phi, 0), //C
                new Vector3f(0, -s_phi, 0), //D
                new Vector3f(0, 0, s_phi), //E
                new Vector3f(0, 0, -s_phi), //F
        };
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

    private void drawTriangle(BaseSide side) {
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

    private void drawPentagon(BaseSide side) {
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

    public void scaleUp(float increment) {
        _s += increment;
        s_phi = _s * _phi;
        triangle = new Triangle(new Color4D(0, 0, 0, 1));
        pentagon = new Pentagon(new Color4D(0, 0, 0, 1));
    }

    public void scaleDown(float increment) {
        _s -= increment;
        s_phi = _s * _phi;
        triangle = new Triangle(new Color4D(0, 0, 0, 1));
        pentagon = new Pentagon(new Color4D(0, 0, 0, 1));
    }

    private static class BaseSide extends OGLObject {
        final static int coordinatesPerVertex = 3;

        protected BaseSide(Color4D color) {
            super(color);
        }

        protected void addVertex(float x, float y, float z) {
            m_positions.put(m_vertexCount * coordinatesPerVertex + 0, x);
            m_positions.put(m_vertexCount * coordinatesPerVertex + 1, y);
            m_positions.put(m_vertexCount * coordinatesPerVertex + 2, z);

            m_normals.put(m_vertexCount * coordinatesPerVertex + 0, 0);
            m_normals.put(m_vertexCount * coordinatesPerVertex + 1, 0);
            m_normals.put(m_vertexCount * coordinatesPerVertex + 2, 1);

            m_vertexCount++;
        }

        public BaseSide setRGBA(float r, float g, float b, float a) {
            m_color.put(0, r);
            m_color.put(1, g);
            m_color.put(2, b);
            m_color.put(3, a);
            return this;
        }
    }

    // the second form that is a side of a Icosidodecahedron is a triangle (each side same length)
    private static class Triangle extends BaseSide {
        protected Triangle(Color4D color) {
            super(color);

            final int nVertices = 3; // three points for a triangle
            final int nCoordinates = nVertices * coordinatesPerVertex; // each coordinate triple used for one vertex

            // Allocate vertex positions and normals
            allocatePositionBuffer(nCoordinates);
            allocateNormalBuffer(nCoordinates);


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
    }

    // the second form that is a side of a Icosidodecahedron is a triangle (each side same length)
    private static class TriangleSide extends BaseSide {
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
    }

    private class Pentagon extends BaseSide {
        // 72° = 360/5 in radians
        final double angle_increment = 2 * Math.PI / 5;

        protected Pentagon(Color4D color) {
            super(color);

            final int nVertices = 5; // three points for a triangle
            final int nCoordinates = nVertices * coordinatesPerVertex; // each coordinate triple used for one vertex

            // Allocate vertex positions and normals
            allocatePositionBuffer(nCoordinates);
            allocateNormalBuffer(nCoordinates);

            // Calculate all vertices
            //angepasster umkreisradius (da via Seitenlänge = _s gesteuertt wird wie gross die form ist)
            float r_u = (float) (_s / (2 * Math.sin(Math.toRadians(36))));

            Vector3f vertices[] = new Vector3f[5];
            for (int i = 0; i < nVertices; i++) {
                float x = (float) (r_u * Math.cos((Math.PI / 2) + i * angle_increment));
                float y = (float) (r_u * Math.sin((Math.PI / 2) + i * angle_increment));
                float z = 0;

                addVertex(x, y, z);
            }


            // Bind vertex positions and normals
            bindPositionBuffer();
            bindNormalBuffer();
        }
    }
}

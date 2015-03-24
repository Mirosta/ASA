package com.tw10g12.Draw.Engine;

import com.jogamp.graph.curve.opengl.TextRenderer;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.font.FontSet;
import com.jogamp.opengl.math.geom.AABBox;
import com.tw10g12.Draw.Engine.Exception.DrawToolsStateException;
import com.tw10g12.Maths.Matrix4;

import java.io.IOException;
import java.util.List;

import javax.media.opengl.GL3;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.tw10g12.Maths.Vector2;
import com.tw10g12.Maths.Vector3;
import com.tw10g12.Maths.Vector4;

@SuppressWarnings(value = { "unused" })
public class DrawTools
{
    public static final int NORMAL = 0;
    public static final int NO_LIGHTING = 1;
    public static final int INSTANCED = 2;

    private GL3 gl3;
    private GLU glu;
    GLUT glut;
    private Tessellator tessellator;

    private float fieldOfView = 60;
    private float nearZ = 0.1f;
    private float farZ = 600000;

    private Matrix4 modelView = Matrix4.getIdentityMatrix();
    private boolean hasStarted = false;
    private int shaderMode;
    private Vector3[] lightPos = new Vector3[]{new Vector3(1,-1,0)};
    private int[] viewport = new int[4];
    private Matrix4 projection = Matrix4.getIdentityMatrix();
    private boolean instanced = false;
    private TextRenderer textRenderer = null;
    private Font font = null;

    public DrawTools(GL3 glInstance, GLU glu, GLUT glut, List<ShaderLoader> shaders)
    {
        this.gl3 = glInstance;
        this.glu = glu;
        this.glut = glut;

        tessellator = new Tessellator(glInstance, shaders);
        tessellator.addNewInstanceVBO();
    }

    public void clear(Colour clearColour)
    {
        clear(clearColour, true, true);
    }

    public void clear(Colour clearColour, boolean colorBuffer, boolean depthBuffer)
    {
        int clearBits = (colorBuffer ? GL3.GL_COLOR_BUFFER_BIT : 0) | (depthBuffer ? GL3.GL_DEPTH_BUFFER_BIT : 0);
        gl3.glClearColor(clearColour.r, clearColour.g, clearColour.b, clearColour.a);
        gl3.glClear(clearBits);
    }

    public void setupOrthographicProjection(double left, double right, double bottom, double top, double near, double far)
    {
        //gl3.glMatrixMode(GL3.GL_PROJECTION);
        //gl3.glLoadIdentity();
        //gl3.glOrtho(left, right, bottom, top, near, far);
    }

    public void setupPerspectiveProjection(float screenWidth, float screenHeight)
    {
        viewport = new int[]{0,0,(int)screenWidth, (int)screenHeight};
        /*gl3.getGL2().glMatrixMode(GL2.GL_PROJECTION);
        gl3.getGL2().glLoadIdentity();
        glu.gluPerspective(fieldOfView, screenWidth/screenHeight, nearZ, farZ);
        double[] outMatrix = new double[16];
        gl3.getGL2().glGetDoublev(GL2.GL_PROJECTION_MATRIX, outMatrix, 0);
        Matrix4 testMatrix = Matrix4.getFromOpenGl(outMatrix);*/
        projection = Matrix4.getPerspectiveMatrix(fieldOfView, screenWidth/screenHeight, nearZ, farZ);
    }

    double tanFOV = 1 / Math.sqrt(3.0);

    public double getNearClipHeight()
    {
        return tanFOV * nearZ;
    }

    public double getNearZ()
    {
        return nearZ;
    }

    public void setupModelView(Matrix4 matrix)
    {
        ShaderLoader currentShader = tessellator.getCurrentShader();
        currentShader.setUniformVariable(gl3, matrix, currentShader.getModelViewName());
        /*gl3.glMatrixMode(GL3.GL_MODELVIEW);
        gl3.glLoadIdentity();

        gl3.glMultMatrixd(matrix.flattenMatrixValues(), 0);
        double[] outMatrix = new double[16];
        gl3.glGetDoublev(GL3.GL_MODELVIEW_MATRIX, outMatrix, 0);*/
    }

    private void setupProjection()
    {
        ShaderLoader currentShader = tessellator.getCurrentShader();
        currentShader.setUniformVariable(gl3, getProjectionMatrix(), currentShader.getProjectionName());
    }

    public void setupLighting()
    {
        Vector4 lightPos = new Vector4(this.lightPos[0],0);
        /*gl3.glLightfv(GL3.GL_LIGHT0, GL3.GL_DIFFUSE, new float[]{1, 0.6f, 0.6f}, 0);
        gl3.glLightfv(GL3.GL_LIGHT0, GL3.GL_AMBIENT, new float[]{0.5f, 0.5f, 0.5f}, 0);
        gl3.glLightfv(GL3.GL_LIGHT0, GL3.GL_POSITION, lightPos.getArray(), 0);*/
    }

    public void drawCuboid(Vector3 position, Vector3 size, Vector3 up, Vector3 down, Vector3 left, Vector3 right, Vector3 forward, Vector3 backward, Colour[] fill)
    {
        Vector3 topLeft = position;
        Vector3 topRight = position.add(right.multiply(size.getX()));
        Vector3 bottomLeft = position.add(down.multiply(size.getY()));
        Vector3 bottomRight = position.add(right.multiply(size.getX()).add(down.multiply(size.getY())));
        Vector3 backTopLeft = position.add(forward.multiply(size.getZ()));
        Vector3 backTopRight = backTopLeft.add(right.multiply(size.getX()));
        Vector3 backBottomLeft = backTopLeft.add(down.multiply(size.getY()));
        Vector3 backBottomRight = backTopLeft.add(right.multiply(size.getX()).add(down.multiply(size.getY())));

        //Right
        drawPlane(fill[1 % fill.length], topRight, backTopRight, bottomRight, backBottomRight);//position.add(right.multiply(size)), down, forward, size);
        //Front
        drawPlane(fill[0 % fill.length], topLeft, topRight, bottomLeft, bottomRight);//position, down, right, size);
        //Back
        drawPlane(fill[3 % fill.length], backTopRight, backTopLeft, backBottomRight, backBottomLeft);//position.add(forward.multiply(size)).add(right.multiply(size)), down, left, size);
        //Left
        drawPlane(fill[4 % fill.length], backTopLeft, topLeft, backBottomLeft, bottomLeft);//position.add(forward.multiply(size)), down, backward, size);
        //Bottom
        drawPlane(fill[5 % fill.length], backBottomRight, backBottomLeft, bottomRight, bottomLeft);//position.add(down.multiply(size)), forward, right, size);
        //Top
        drawPlane(fill[2 % fill.length], topRight, topLeft, backTopRight, backTopLeft);//position.add(forward.multiply(size)), backward, right, size);
    }

    public void drawCuboid(Vector3 position, Vector3 size, Colour[] fill)
    {
        Vector3 up = new Vector3(0,1,0);
        Vector3 down = new Vector3(0,-1,0);
        Vector3 left = new Vector3(-1,0,0);
        Vector3 right = new Vector3(1,0,0);
        Vector3 forward = new Vector3(0,0,-1);
        Vector3 backward = new Vector3(0,0,1);
        drawCuboid(position, size, up, down, left, right, forward, backward, fill);
    }

    public void drawPlane(Colour fill, Vector3 topLeft, Vector3 topRight, Vector3 bottomLeft, Vector3 bottomRight)
    {
        int[] vertexIndices = new int[4];

        vertexIndices[2] = tessellator.addVertex(topLeft, fill);
        vertexIndices[3] = tessellator.addVertex(topRight, fill);
        vertexIndices[0] = tessellator.addVertex(bottomLeft, fill);
        vertexIndices[1] = tessellator.addVertex(bottomRight, fill);
        tessellator.setNormals(4, 0);

        tessellator.addIndex(vertexIndices[0]);
        tessellator.addIndex(vertexIndices[1]);
        tessellator.addIndex(vertexIndices[3]);

        tessellator.addIndex(vertexIndices[0]);
        tessellator.addIndex(vertexIndices[3]);
        tessellator.addIndex(vertexIndices[2]);
    }

    public void drawPlane(Vector3 position, Vector3 down, Vector3 right, Vector3 size, Colour fill)
    {
        Vector3 topLeft = position;
        Vector3 topRight = position.add(right.multiply(size));
        Vector3 bottomLeft = position.add(down.multiply(size));
        Vector3 bottomRight = position.add(size.multiply(down.add(right)));

        int[] vertexIndices = new int[4];

        vertexIndices[2] = tessellator.addVertex(topLeft, fill);
        vertexIndices[3] = tessellator.addVertex(topRight, fill);
        vertexIndices[0] = tessellator.addVertex(bottomLeft, fill);
        vertexIndices[1] = tessellator.addVertex(bottomRight, fill);
        tessellator.setNormals(4, 0);

        tessellator.addIndex((short)vertexIndices[0]);
        tessellator.addIndex((short)vertexIndices[1]);
        tessellator.addIndex((short)vertexIndices[3]);

        tessellator.addIndex((short)vertexIndices[0]);
        tessellator.addIndex((short)vertexIndices[3]);
        tessellator.addIndex((short)vertexIndices[2]);
    }

    public void drawUnsortedTriangle(Vector3 p1, Vector3 p2, Vector3 p3, Colour fill)
    {
        short index = (short) tessellator.addVertex(p1, fill);
        tessellator.addIndex(index);
        index = (short) tessellator.addVertex(p2, fill);
        tessellator.addIndex(index);
        index = (short) tessellator.addVertex(p3, fill);
        tessellator.addIndex(index);

        tessellator.setNormals(3, 0);
    }

    public void drawTriangle(Vector3 p1, Vector3 p2, Vector3 p3, Colour fill)
    {
        Vector3 centre = p1.add(p2).add(p3).divide(3);
        Vector3[] ordered = new Vector3[4];

        order(p1, centre, ordered);
        order(p2, centre, ordered);
        order(p3, centre, ordered);

        for(int i = 0; i < 4; i++)
        {
            if(ordered[i] != null)
            {
                short index = (short) tessellator.addVertex(ordered[i], fill);
                tessellator.addIndex(index);
            }
        }
        tessellator.setNormals(3, 0);

    }

    public void drawTexturedTriangle(Vector3 p1, Vector3 p2, Vector3 p3, Texture tex, Colour fill)
    {
        tessellator.setCurrentShader(shaderMode);
        tessellator.draw();
        tessellator.reset();

        tex.bind(getGL3());
        tex.enable(getGL3());
        drawTexturedTriangle(p1, p2, p3,new Vector2[]{new Vector2(0,1), new Vector2(1,0), new Vector2(0,0)}, fill);
        tex.disable(getGL3());

        tessellator.draw();
        tessellator.reset();
    }

    public void drawTexturedTriangle(Vector3 p1, Vector3 p2, Vector3 p3, Vector2[] uvCoords, Colour fill)
    {
        Vector3 centre = p1.add(p2).add(p3).divide(3);
        Vector3[] ordered = new Vector3[4];
        Colour[] col = new Colour[]{Colour.Red, Colour.Green, Colour.Blue, Colour.White};
        order(p1, centre, ordered);
        order(p2, centre, ordered);
        order(p3, centre, ordered);

        int uvPtr = 0;
        for(int i = 0; i < 4; i++)
        {
            if(ordered[i] != null)
            {
                int index = tessellator.addVertex(ordered[i], fill, uvCoords[uvPtr]);
                tessellator.addIndex(index);
                uvPtr++;
            }
        }
        tessellator.setNormals(3, 0);

    }

    private void order(Vector3 pos, Vector3 centre, Vector3[] ordered)
    {
        int suggestedIndex = -1;
        if(pos.getX() < centre.getX())
        {
            if(pos.getY() < centre.getY())
            {
                suggestedIndex = 0; //Bottom left
            }
            else
            {
                suggestedIndex = 3; //Top left
            }
        }
        else
        {
            if(pos.getY() < centre.getY())
            {
                suggestedIndex = 1; //Bottom Right
            }
            else
            {
                suggestedIndex = 2; //Top Right
            }
        }

        while(ordered[suggestedIndex] != null)
        {
            suggestedIndex++;
            suggestedIndex %= ordered.length;
        }

        ordered[suggestedIndex] = pos;
    }

    public void drawTrapezium(Vector3 p1, Vector3 p2, Vector3 right, double bottomLen, double topLen, Colour fill)
    {
        Vector3 topLeft = p1;
        Vector3 topRight = p1.add(right.multiply(topLen));
        Vector3 bottomLeft = p2;
        Vector3 bottomRight = p2.add(right.multiply(bottomLen));

        drawTriangle(topLeft, topRight, bottomLeft, fill);
        drawTriangle(bottomRight, topRight, bottomLeft, fill);
    }

    public void drawQuad(Vector3 topLeft, Vector3 topRight, Vector3 bottomLeft, Vector3 bottomRight, Colour fill)
    {
        drawTriangle(bottomLeft, bottomRight, topRight, fill);
        drawTriangle(bottomLeft, topRight, topLeft, fill);
    }

    public void drawTexturedQuad(Vector3 topLeft, Vector3 topRight, Vector3 bottomLeft, Vector3 bottomRight, Vector2 topLeftUV, Vector2 topRightUV, Vector2 bottomLeftUV,Vector2 bottomRightUV, Texture tex, Colour fill)
    {
        tex.bind(getGL3());
        tex.enable(getGL3());
        Vector2[] coords = new Vector2[]{bottomLeftUV, bottomRightUV, topRightUV, topLeftUV};
        int[] indices = new int[4];
        indices[0] = tessellator.addVertex(bottomLeft, fill, coords[0]);
        indices[1] = tessellator.addVertex(bottomRight, fill, coords[1]);
        indices[2] = tessellator.addVertex(topRight, fill, coords[2]);
        indices[3] = tessellator.addVertex(topLeft, fill, coords[3]);
        tessellator.setNormals(4, 0);

        tessellator.addIndex(indices[0]);
        tessellator.addIndex(indices[1]);
        tessellator.addIndex(indices[2]);

        tessellator.addIndex(indices[0]);
        tessellator.addIndex(indices[2]);
        tessellator.addIndex(indices[3]);

        tex.disable(getGL3());
    }

    public void drawTexturedQuad(Vector3 topLeft, Vector3 topRight, Vector3 bottomLeft, Vector3 bottomRight, Vector2 topLeftUV, Vector2 bottomRightUV, Texture tex, Colour fill)
    {
        drawTexturedQuad(topLeft, topRight, bottomLeft, bottomRight, topLeftUV, topLeftUV.add(bottomRightUV.subtract(topLeftUV).multiply(new Vector2(1,0))), topLeftUV.add(bottomRightUV.subtract(topLeftUV).multiply(new Vector2(0,1))), bottomRightUV, tex, fill);
    }

    public void drawTexturedQuad(Vector3 topLeft, Vector3 topRight, Vector3 bottomLeft, Vector3 bottomRight, Texture tex, Colour fill)
    {
        drawTexturedQuad(topLeft, topRight, bottomLeft, bottomRight, new Vector2(0,0), new Vector2(1,0),new Vector2(0,1), new Vector2(1,1), tex, fill);
    }

    public void drawText(String text, Vector3 bottomLeft, double size, Colour col, double lineSize, double rotX, double rotY, double rotZ)
    {
        drawText(text, bottomLeft, size, col, lineSize, new Vector3(0,0,0), rotX, rotY, rotZ);
    }

    public void drawText(String text, Vector3 bottomLeft, double size, Colour col, double lineSize, Vector3 origin, double rotX, double rotY, double rotZ)
    {
        //int previousShaderMode = shaderMode;
        //boolean previousInstanced = instanced;
        TextRenderer textRenderer = getTextRenderer();
        if(!textRenderer.isInitialized()) textRenderer.init(getGL3());
        textRenderer.enable(getGL3(), true);
        AABBox stringSize = getFont().getStringBounds(text, 1);
        textRenderer.getMatrix().glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        textRenderer.getMatrix().glLoadMatrixf(getModelView().flattenMatrixValuesAsFloats(), 0);
        textRenderer.translate(getGL3(), (float) bottomLeft.getX(), (float) bottomLeft.getY(), (float) bottomLeft.getZ());
        textRenderer.rotate(getGL3(), (float) rotX, 1, 0, 0);
        textRenderer.rotate(getGL3(), (float) rotY, 0, 0, 1);
        textRenderer.scale(getGL3(), (float)size, (float)size, (float)size);
        textRenderer.translate(getGL3(), (float)-origin.getX() * stringSize.getWidth(), (float)-origin.getY() * stringSize.getHeight() * 0.5f, (float)-origin.getZ());
        textRenderer.drawString3D(getGL3(), getFont(), text, new float[]{1, 1}, 1, new int[]{12, 12});
        textRenderer.enable(getGL3(), false);
        recreateGLSettings();

        //double height = 100;
        //double width = textWidth(text, 100);
        /*gl3.glPushMatrix();
        gl3.glTranslated(bottomLeft.getX(), bottomLeft.getY(), bottomLeft.getZ());
        gl3.glRotated(rotX, 1, 0, 0);
        gl3.glRotated(rotY, 0, 0, 1);
        gl3.glScaled(size / 100.0, size / 100.0, size / 100.0);
        gl3.glTranslated(-origin.getX() * width, -origin.getY() * height, -origin.getZ());
        gl3.glColor4f(col.getR(), col.getG(), col.getB(), col.getA());
        gl3.glLineWidth((float) lineSize);
        for(int i =0; i < text.length(); i++)
        {
            glut.glutStrokeCharacter(GLUT.STROKE_ROMAN, text.charAt(i));
        }
        gl3.glPopMatrix();*/
    }

    public void drawBillboardText(String text, Vector3 bottomLeft, double screenFactor, Colour col, double lineSize)
    {
        setupModelView(Matrix4.getIdentityMatrix());
        Vector4 translatePos = modelView.multiply(new Vector4(bottomLeft.getX(),bottomLeft.getY(),bottomLeft.getZ(),1));
        //System.out.println(translatePos.getX() + "," + translatePos.getY() + "," + translatePos.getZ());
        double scale = (-translatePos.getZ())/(100*screenFactor);
        /*gl3.glTranslated(translatePos.getX(), translatePos.getY(), translatePos.getZ());
        gl3.glScaled(scale, scale, scale);
        gl3.glColor4f(col.getR(), col.getG(), col.getB(), col.getA());*/
        gl3.glLineWidth((float) lineSize);
        //gl3.glTranslated(-100, 0, 0);

        for(int i =0; i < text.length(); i++)
        {
            glut.glutBitmapCharacter(GLUT.BITMAP_TIMES_ROMAN_10, text.charAt(i));
        }
        setupModelView(modelView);
    }

    public Vector3[] startBillboard(Vector3[] positions)
    {
        return startBillboard(positions, NORMAL);
    }

    public Vector3[] startBillboard(Vector3[] positions, int shaderMode)
    {
        Vector3[] returnVectors = new Vector3[0];
        if(positions != null)
        {
            returnVectors = new Vector3[positions.length];
            int n =0;
            for(Vector3 vec : positions)
            {
                returnVectors[n] = modelView.multiply(new Vector4(vec,1)).getXYZ();
                n++;
            }
        }

        end();
        start(shaderMode);
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        //gl3.glMatrixMode(GL3.GL_MODELVIEW);
        //gl3.glLoadIdentity();

        return returnVectors;
    }

    public void endBillboard()
    {
        end();
        gl3.glEnable(GL3.GL_DEPTH_TEST);
        start();
    }

    public void drawArrow(Vector3 start, Vector3 end, Vector3 up, boolean startArrow, boolean endArrow, double lineWidth, double arrowSize, Colour colour)
    {
        drawArrow(start, end, up, startArrow, endArrow, lineWidth, arrowSize,arrowSize, colour);
    }

    public void drawArrow(Vector3 start, Vector3 end, Vector3 up, boolean startArrow, boolean endArrow, double lineWidth, double startArrowSize,double endArrowSize, Colour colour)
    {
        Vector3 normalisedDirection = end.subtract(start).normalise();
        Vector3 lineStart = startArrow ? start.add(normalisedDirection.multiply(startArrowSize)) : start;
        Vector3 lineEnd = endArrow ? end.subtract(normalisedDirection.multiply(endArrowSize)) : end;

        //gl3.glColor4f(colour.getR(), colour.getG(), colour.getB(), colour.getA());
        gl3.glLineWidth((float) lineWidth);
        //gl3.glBegin(GL3.GL_LINES);
        //gl3.glVertex3d(lineStart.getX(), lineStart.getY(), lineStart.getZ());
        //gl3.glVertex3d(lineEnd.getX(), lineEnd.getY(), lineEnd.getZ());
        //gl3.glEnd();

        if(startArrow)
        {
            double arrowWidth = startArrowSize * 0.8;
            drawTriangle(start, lineStart.add(up.multiply(arrowWidth*0.5)), lineStart.add(up.multiply(-arrowWidth*0.5)), colour);
        }
        if(endArrow)
        {
            double arrowWidth = endArrowSize * 0.8;
            drawTriangle(end, lineEnd.add(up.multiply(arrowWidth*0.5)), lineEnd.add(up.multiply(-arrowWidth*0.5)), colour);
        }
    }

    public void start(int shaderMode, boolean instanced)
    {
        this.shaderMode = shaderMode;
        this.instanced = instanced;

        if(hasStarted) throw new DrawToolsStateException("Must call end before calling start again!");

        tessellator.reset();
        tessellator.setCurrentShader(shaderMode);
        tessellator.useProgram();
        tessellator.setCurrentVBO(instanced ? 1 : 0);
        setupProjection();
        setupModelView(modelView);
        setupLighting();
        hasStarted = true;
    }

    public void recreateGLSettings()
    {
        tessellator.useProgram();
        tessellator.setCurrentVBO(instanced ? 1 : 0);
        setupProjection();
        setupModelView(modelView);
    }

    public void start()
    {
        start(this.NORMAL, false);
    }

    public void start(int shaderMode)
    {
        start(shaderMode, false);
    }

    public void start(boolean instanced)
    {
        start(instanced ? this.INSTANCED : this.NORMAL, instanced);
    }

    public void end()
    {
        if(!hasStarted) throw new DrawToolsStateException("Not yet started, nothing to end!");

        if(instanced) tessellator.drawInstanced();
        else tessellator.draw();

        hasStarted = false;
        instanced = false;
    }

    public void setModelView(Matrix4 modelView)
    {
        this.modelView = modelView;
    }

    public Matrix4 getModelView()
    {
        return this.modelView;
    }

    public Tessellator getTessellator()
    {
        return tessellator;
    }

    public TextRenderer getTextRenderer()
    {
        if(textRenderer == null) textRenderer = TextRenderer.create(JOGLUtil.getRenderState(this), 1);
        return textRenderer;
    }

    private Font getFont()
    {
        if(font == null)
        {
            try
            {
                FontSet fontSet = FontFactory.get(FontFactory.JAVA);
                font = fontSet.get(0, 3);
            }
            catch (IOException ex)
            {
                System.err.println("Couldn't load font - " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        }
        return font;
    }

    public GL3 getGL3()
    {
        return gl3;
    }

    public void setGL3(GL3 gl3)
    {
        this.gl3 = gl3;
        this.tessellator.setGL3(gl3);
    }

    public double textWidth(String text, double size)
    {
        double width = 0;
        for(int i =0; i < text.length(); i++)
        {
            width += glut.glutStrokeWidthf(GLUT.STROKE_ROMAN, text.charAt(i));
        }

        return width*(size/100.0);
    }

    public double billboardTextWidth(String text, double screenFactor, Vector3 bottomLeft)
    {
        Vector4 translatePos = modelView.multiply(new Vector4(bottomLeft.getX(),bottomLeft.getY(),bottomLeft.getZ(),1));
        double scale = (-translatePos.getZ())/(100*screenFactor);
        double width = 0;
        for(int i =0; i < text.length(); i++)
        {
            width += glut.glutStrokeWidthf(GLUT.STROKE_ROMAN, text.charAt(i));
        }
        return width*scale*-translatePos.getZ();
    }

    public void billboardTranslate(Vector3 translation)
    {
        Matrix4 mat = Matrix4.getTranslationMatrix(translation).multiply(Matrix4.getScaleMatrix(-translation.getZ()));
        setupModelView(mat);
    }

    public Matrix4 getProjectionMatrix()
    {
        return projection;
    }

    public boolean isPointVisible(Vector3 vec, boolean useModelTransform, double threshold)
    {
        Vector4 point = new Vector4(vec,1);
        if(useModelTransform) point = modelView.multiply(point);
        Matrix4 proj = getProjectionMatrix();
        Vector3 transformed = proj.multiply(point).toVector3();
        if(transformed.getX() < -1 - threshold) return false;
        if(transformed.getX() > 1 + threshold) return false;
        if(transformed.getY() < -1 - threshold) return false;
        if(transformed.getY() > 1 + threshold) return false;
        if(transformed.getZ() < -1 - threshold) return false;
        if(transformed.getZ() > 1 + threshold) return false;

        return true;
    }

    public void drawQuad(Vector3 position, Vector3 origin, Vector3 size, Colour fill)
    {
        Vector3 topLeft = position.add(size.multiply(new Vector3(1,1,1).subtract(origin)));
        Vector3 topRight = position.add(size.multiply(new Vector3(0,1,0).subtract(origin)));
        Vector3 bottomLeft = position.add(size.multiply(new Vector3(1,0,1).subtract(origin)));
        Vector3 bottomRight = position.add(size.multiply(new Vector3(0,0,0).subtract(origin)));
        drawQuad(topLeft, topRight, bottomLeft, bottomRight, fill);
    }

    public void drawInstance(Vector3 position)
    {
        if(instanced)
        {
            tessellator.addInstancePosition(position);
        }
    }

    private int[] getViewport()
    {
        return viewport;
    }

    public Vector3[] rayTracePoint(Vector2 position)
    {
        Vector3[] results = new Vector3[2];
        int[] view = getViewport();
        double[] projection = this.projection.flattenMatrixValues();
        double[] model = Matrix4.getIdentityMatrix().flattenMatrixValues();
        double[] result = new double[3];

        //Point on screen
        boolean success = glu.gluUnProject(position.getX(), view[3] - position.getY(), 0, model, 0, projection, 0, view, 0, result, 0);
        if(!success) return null;
        else results[0] = new Vector3(result[0],result[1],result[2]);
        //Point on far plane
        success = glu.gluUnProject(position.getX(), view[3] - position.getY(), 1, model, 0, projection, 0, view, 0, result, 0);
        if(!success) return null;
        else results[1] = new Vector3(result[0],result[1],result[2]);

        return results;
    }

    public Vector3 getScreenEdge(double d)
    {
        int[] viewport = getViewport();
        double aspect = (double)viewport[2]/(double)viewport[3];
        return new Vector3(aspect,1,1).multiply(d);
    }

    public ShaderLoader getCurrentShader()
    {
        return tessellator.getCurrentShader();
    }
}
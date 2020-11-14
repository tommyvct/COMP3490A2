import java.util.Stack;

import processing.core.*;
// import processing.data.*;
// import processing.event.*;
// import processing.opengl.*;



public class App extends PApplet
{
    /**
     * <h1>Entry point</h1>
     * 
     * The {@code appletArgs} must match this class's name
     * 
     * @param passedArgs
     */
    static public void main(String[] passedArgs)
    {
        String[] appletArgs = new String[] { "App" };

        if (passedArgs != null)
        {
            PApplet.main(concat(appletArgs, passedArgs));
        }
        else
        {
            PApplet.main(appletArgs);
        }
    }


    public void settings()
    {
        size(640, 640);
    }


    /**
     * <h1>viewportTest </h1>
     * 
     * Tests {@code [0, 0, 1]}, {@code [1, 1, 1]} and {@code [-1, -1, 1]}. <p>
     * Results should be {@code [ 320.0, 320.0, 1.0 ]}, {@code [ 640.0, 0.0, 1.0 ]} and {@code [ 0.0, 640.0, 1.0 ]}.
     */
    void viewportTest()
    {
        PVector zerozero = new PVector(0, 0, 1);
        PVector oneone = new PVector(1, 1, 1);
        PVector negonenegone = new PVector(-1, -1, 1);

        var vp = getViwePort();
        var result0 = vp.mult(zerozero, null);
        vp = getViwePort();
        var result1 = vp.mult(oneone, null);
        vp = getViwePort();
        var result2 = vp.mult(negonenegone, null);

        printPMatrix3D(vp);
        print(result0);
        print(result1);
        print(result2);
        print();
    }

    /**
     * Prints out a {@code PMatrix3D} object.
     * 
     * @param m the {@code PMatrix3D} object to print
     */
    void printPMatrix3D(PMatrix3D m)
    {
        println("[ " + m.m00 + " " + m.m01 + " " + m.m02 + " " + m.m03 + " ]");
        println("[ " + m.m10 + " " + m.m11 + " " + m.m12 + " " + m.m13 + " ]");
        println("[ " + m.m20 + " " + m.m21 + " " + m.m22 + " " + m.m23 + " ]");
        println("[ " + m.m30 + " " + m.m31 + " " + m.m32 + " " + m.m33 + " ]");
    }

    // use to implement your model matrix stack
    Stack<PMatrix3D> matrixStack = new Stack<PMatrix3D>();

    PMatrix3D M = new PMatrix3D();   // defaults to identity unless changed by drawing code.
    PMatrix3D V;
    PMatrix3D Pr;
    PMatrix3D Vp;


    /**
     * ViewPort. transform from NDC -> the processing viewport. Setup at the beginning of your program
     * and don’t touch after. <p>
     * 
     * <ul>
     * <li><p>scale by <width/2, -height/2> to go from a 2x2-&gt;widthxheight system centered on 0, and to flip
     * the Y axis to match processing. Translate by width/2, height/2 to move 0,0 to the bottom corner.</p>
     * </li>
     * <li><p>Test points:</p>
     * <ul>
     * <li>(0, 0) NDC -&gt; width/2, height/2 (center of screen)</li>
     * <li>(1, 1) -&gt; width, 0 (top right of screen)</li>
     * <li>(-1, -1) -&gt; 0, height (bottom left of screen)</li>
     * </ul>
     * </li>
     * </ul>
     * Tests covered in {@code viewportTest()} method.
     * @return a {@code PMatrix3D} object describes the viewport matrix
     */
    PMatrix3D getViwePort()
    {
        PMatrix3D scaleAndFlipY = new PMatrix3D(
            width/2 ,     0     , 0, 0, 
               0    , 0-height/2, 0, 0,
               0    ,     0     , 1, 0,
               0    ,     0     , 0, 1
        );

        PMatrix3D translate = new PMatrix3D(
            1, 0, width /2, 0,
            0, 1, height/2, 0,
            0, 0,    1    , 0,
            0, 0,    0    , 1            
        );

        scaleAndFlipY.preApply(translate);

        return scaleAndFlipY;
    }

    /**
     * setup a camera model transformation that converts from world coordinates to camera coordinates.
     * 
     * @param up a vector point up from the centre in world coordinate
     * @param centre center of the camera in world coordinate
     * @param zoom 
     * @return a view (aka camera model) matrix
     */
    PMatrix3D getCameraModelMatrix(PVector up, PVector centre, float zoom)
    {
        PMatrix3D translate2Centre = new PMatrix3D(
            1, 0, 0-centre.x, 0,
            0, 1, 0-centre.y, 0,
            0, 0,     1     , 0,
            0, 0,     0     , 1
        );

        up = up.normalize();

        PMatrix3D basis = new PMatrix3D(
            up.y, 0-up.x, 0, 0,
            up.x,   up.y, 0, 0,
             0  ,   0   , 1, 0,
             0  ,   0   , 0, 1
        );

        PMatrix3D scale = new PMatrix3D(
            1/zoom,    0  , 0, 0,
               0  , 1/zoom, 0, 0,
               0  ,    0  , 1, 0,
               0  ,    0  , 0, 1
        );

        translate2Centre.preApply(scale);
        translate2Centre.preApply(basis);

        return translate2Centre;
    }

    /**
     *  Setup an orthographic projection with the specified left, right, bottom, top points to setup
     *  your world coordinate system. Translates from camera coordinates -> NDC
     * 
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @return a projection matrix
     */
    PMatrix3D getOrtho(float left, float right, float bottom, float top)
    {
        PMatrix3D move2Centre = new PMatrix3D(
            1, 0, 0, 0-((left + right) / 2),
            0, 1, 0, 0-((top + bottom) / 2),
            0, 0, 1,           0           ,
            0, 0, 0,           1
        );

        PMatrix3D scale = new PMatrix3D(
            2/(right - left),        0        , 0, 0,
                   0        , 2/(top - bottom), 0, 0,
                   0        ,        0        , 1, 0,
                   0        ,        0        , 0, 1
        );

        move2Centre.preApply(scale);

        return move2Centre;
    }


    // called once, at the start of our program
    public void setup()
    {
        colorMode(RGB, 1.0f);

        M.reset();
    }

    float zoom = 1f;
    float rotate = 0f;
    float cameraCentreRange = 640f;

    // called roughly 60 times per second
    public void draw()
    {
        clear();

        
        PMatrix3D rotateMatrix = new PMatrix3D(
            cos(rotate), 0-sin(rotate), 0, 0,
            sin(rotate),   cos(rotate), 0, 0,
                  0     ,        0    , 1, 0,
                  0     ,        0    , 0, 1
        );
        
        PVector cameraCentre = (orthoMode == OrthoMode.FLIPX) ?
        new PVector(
            0-(float) (((float) mouseX/(float)  width - 0.5) * (orthoMode == OrthoMode.IDENTITY ? 2f : 640f) * zoom),
            0-(float) (((float) mouseY/(float) height - 0.5) * (orthoMode == OrthoMode.IDENTITY ? 2f : 640f) * zoom),
            1
        )
        :
        new PVector(
              (float) (((float) mouseX/(float)  width - 0.5) * (orthoMode == OrthoMode.IDENTITY ? 2f : 640f) * zoom),
            0-(float) (((float) mouseY/(float) height - 0.5) * (orthoMode == OrthoMode.IDENTITY ? 2f : 640f) * zoom),
            1
        );

        cameraCentre = rotateMatrix.mult(cameraCentre, null);

        PVector upVector = rotateMatrix.mult(new PVector(0, 1, 1), null);    

        V = getCameraModelMatrix(upVector, cameraCentre, zoom);
        
        if (orthoMode == OrthoMode.BOTTOMLEFT640)
        {
            Pr = getOrtho(0, 640, 0, 640);
        }
        else if (orthoMode == OrthoMode.FLIPX)
        {
            Pr = getOrtho(320, -320, -320, 320); 
        }
        else if (orthoMode == OrthoMode.ASPECT)
        {
            Pr = getOrtho(-320, 320, -100, 100); 
        }
        else if (orthoMode == OrthoMode.IDENTITY)
        {
            var temp = new PMatrix3D();
            temp.reset();
            Pr = temp;

        }
        else // orthoMode == OrthoMode.CENTER640
        {
            Pr = getOrtho(-320, 320, -320, 320); 
        }

        Vp = getViwePort();

        // HELLO world, so to speak
        // draw a line from the top left to the center, in Processing coordinate system
        // highlights what coordinate system you are currently in.
        // stroke(1, 1, 1);
        // beginShape(LINES);
        // myVertex(0, 0);
        // myVertex(width / 2, height / 2);
        // endShape();

        if (testMode)
        {
            drawTest(1000);
            drawTest(100);
            drawTest(1);
        }
        else
        {
            drawScene();
        }
    }

    public void keyPressed() 
    {
        if (key == KEY_ZOOM_IN)
        {
            zoom /= ZOOM_CHANGE;
        }
        else if (key == KEY_ZOOM_OUT)
        {
            zoom *= ZOOM_CHANGE;
        }
        else if (key == KEY_RESET)
        {
            zoom = 1f;
            cameraCentreRange = 640f;
            rotate = 0f;
            orthoMode = DEFAULT_ORTHO_MODE;
            println("reset");
        }
        else if (key == KEY_ROTATE_RIGHT)
        {
            rotate += ((orthoMode == OrthoMode.FLIPX) ? -1f : 1f) * ANGLE_CHANGE;
        }
        else if (key == KEY_ROTATE_LEFT)
        {
            rotate -= ((orthoMode == OrthoMode.FLIPX) ? -1f : 1f) * ANGLE_CHANGE;
        }
        else if (key == KEY_TEST_MODE)
        {
            testMode = !testMode;
        }
        else if (key == KEY_ORTHO_CHANGE)
        {
            int next = (orthoMode.ordinal() + 1) % OrthoMode.values().length;
            orthoMode = OrthoMode.values()[next];
            println("ortho mode: " + orthoMode.name());
        }
    }



    Stack<PMatrix3D> modelMatrixStack = null;


    void myPush()
    {
        if (modelMatrixStack == null)
        {
            modelMatrixStack = new Stack<PMatrix3D>();
        }

        modelMatrixStack.push(M);
    }


    void myPop()
    {
        if (modelMatrixStack == null || modelMatrixStack.empty())
        {
            println("the stack is empty!");
            return;
        }

        M = modelMatrixStack.pop();
    }


    void myRotate(float theta)
    {
        M.preApply( new PMatrix3D(
            cos(theta), 0-sin(theta), 0, 0,
            sin(theta),   cos(theta), 0, 0,
                0     ,       0     , 1, 0,
                0     ,       0     , 0, 1
        ));
    }

    void myScale(float s)
    {
        M.preApply(new PMatrix3D(
            s, 0, 0, 0,
            0, s, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        ));
    }


    void myTranslate(float tx, float ty)
    {
        M.preApply(new PMatrix3D(
            1, 0, 0, tx,
            0, 1, 0, ty,
            0, 0, 1,  0,
            0, 0, 0,  1
        ));
    }


    void drawScene()
    {
        myPush();

        // TODO
    }


    // libJim.pde
    final int GRID = 10;


    void drawTest(float scale)
    {
        float left = -scale / 2;
        float right = scale / 2;
        float top = scale / 2;
        float bottom = -scale / 2;

        float r = 1;
        float g = 0;
        float b = 0;
        beginShape(LINES);
        for (int i = 0; i < GRID; i++)
            for (int j = 0; j < GRID; j++)
            {
                float x = left + scale / GRID * i;
                float y = bottom + scale / GRID * j;

                g = (i > GRID / 2) ? 1 : 0;
                b = (j > GRID / 2) ? 1 : 0;
                stroke(r, g, b);
                myVertex(left, y);
                myVertex(right, y);
                myVertex(x, bottom);
                myVertex(x, top);
            }
        endShape(LINES);
    }


    // Modes.pde
    final char KEY_ROTATE_RIGHT = ']';
    final char KEY_ROTATE_LEFT = '[';
    final char KEY_ZOOM_IN = '=';
    final char KEY_ZOOM_OUT = '-';
    final char KEY_RESET = '0';
    final char KEY_ORTHO_CHANGE = 'o';
    final char KEY_TEST_MODE = 't';
    final float ANGLE_CHANGE = PI / 16; // additive
    final float ZOOM_CHANGE = 1.1f; // multiplicative

    // if on, draws test pattern. Otherwise, draws your scene
    boolean testMode = true;


    enum OrthoMode
    {
        IDENTITY, // no change. straight to viewport
        CENTER640, // 0x0 at center, width/height is 640 (+- 320)
        BOTTOMLEFT640, // 0x0 at bottom left, top right is 640x640
        FLIPX, // same as CENTER640 but x is flipped
        ASPECT // uneven aspect ratio: x is < -320 to 320 >, y is <-100 - 100>
    }


    // OrthoMode orthoMode = OrthoMode.IDENTITY;
    OrthoMode orthoMode = OrthoMode.CENTER640;
    final OrthoMode DEFAULT_ORTHO_MODE = OrthoMode.CENTER640; // <>//


    // Transforms.pde
    void myVertex(PVector vert)
    {
        _myVertex(vert.x, vert.y, false);
    }


    void myVertex(float x, float y)
    {
        _myVertex(x, y, false);
    }


    void myVertex(float x, float y, boolean debug)
    {
        _myVertex(x, y, debug);
    }


    /**
     * translate the given point from object space to viewport space, then plot it with vertex.
     * 
     * @param x x-coordinate in object space
     * @param y y-coordinate in object space
     * @param debug if set to {@code true}, print out coordinates from object space and mapped viewport space
     */
    void _myVertex(float x, float y, boolean debug)
    {
        PVector v = new PVector(x, y, 1);
        PMatrix3D combined = new PMatrix3D(M);
        combined.preApply(V);
        combined.preApply(Pr);
        combined.preApply(Vp);

        PVector result = combined.mult(v, null);

        vertex(result.x, result.y);

        if (debug)
        {
            println(x + " " + y + " --> " + result.x + " " + result.y + " " + result.z); // pz is w, should always be 1
        }
    }
}
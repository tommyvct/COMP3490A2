import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

public class App extends PApplet {
    /**
     * <h1>Entry point</h1>
     * 
     * The {@code appletArgs} must match this class's name
     * 
     * @param passedArgs
     */
    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "App" };

        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }

    public void settings() {
        size(640, 640, P3D);
    }

    public void setup() {
    }

    public void draw() {

    }
}
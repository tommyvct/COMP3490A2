## Getting Started

1. Download [Processing](https://processing.org/).
2. Copy everything under `core/library/` from processing into `lib/` folder.
3. Drag this folder into Visual Studio Code or open it using Eclipse.

## Folder Structure

- `src/`: the folder to maintain sources
- `lib/`: the folder to maintain Processing dependencies
- `.classpath`: tells Visual Studio Code or eclipse where the processing dependencies is.

## How processing turn `.pde` files into java code

``` java
import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

public class App extends PApplet 
{
    static public void main(String[] passedArgs) 
    {
        String[] appletArgs = new String[] { "App" };

        if (passedArgs != null) {
          PApplet.main(concat(appletArgs, passedArgs));
        } 
        else 
        {
          PApplet.main(appletArgs);
        }
    }

    // pde file 1, contains settings, setup and draw function
    int globalVar = 0;

    public void settings() 
    {
        size(640, 640, P3D);
    }

    public void setup() 
    {
    }

    public void draw() 
    {
    }

    class inPDEFile1
    {
        int i;
    }

    // all methods are appended with access modifier (public, private, etc...)
    public void methodInPDEFile1()
    {
    }
    // pde file 2
    // pde file 3
    // pde file 4
    // pde file 5
    // more pde file...
}
```
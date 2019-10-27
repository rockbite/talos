# talos vfx

Website: [https://talosvfx.com/](https://talosvfx.com/)

![alt text](https://i.imgur.com/Fxw1Unn.jpg)

Node based, open source VFX Editor with powerfull interface and a ready to use libGDX runtime.

### Screenshot

![screenshot](https://i.imgur.com/KYpynzB.png)

### Key Features

  * Node based particle engine and editor
  * Import (and batch import) from libgdx legacy files
  * Beam Renderer
  * Visual manipulation of shapes, sizes and positions
  * Custom pre-compiled java scriptwriting for value manipulation
  * Custom widget flaours allowing for same widget to look differently if it's just number or if number is angle
  
### List of modules (nodes)

  * Particle module - main output hub for particle related properties (per particle)
  * Emitter module - main output hubg for emitter related properties such as delay, emissions and such
  * System Input - list of inner system values that can be exposed and used by other modules (particle life, emitter life, time)
  * Global Values - dynamic list of values that can be assigned runtime, and used by modules
  * Mixer - Mixes values A & B with provided alpha ratio
  * Shape Range - outputs random position values On or Within provided shape (supports shape customisation, and morphing)
  * Number - Static number output
  * Position - Static position (x and y) output
  * Color - Static color (r,g,b) output
  * Gradient - Interpolates between given list of color values
  * Beam Renderer - renders custom mesh that can be mutated by Bezier tangents and with provided Noise offset. Per inner point.
  * Sprite Renderer - renders simple one texture (region) sprite
  * Flipbook - renders animated output based on provided sprite sheet
  * Random Range - generates random value in given range
  * Dynamic Random Range - interpolates between 2 number ranges using provided curve
  * Noise - outputs simplex noise, with modifiable frequency
  * Multi Input - has dynamic amount of inputs, chooses one randomply and set's it to output
  * Curve - Allows for user made curve with provided points, outputs single value
  * Interpolation - Same as Curve but instead of custom points it is procedural meaning it's a formula
  * Math Operations - Add, substract, multiply, devide, sin, cos functions to be used with A and B values
  * Beam Position - Converts From and To positoin values into Rotation, Position & Size values that are used by Beam Renderer
  * Emitter Config - Wraps emitter configuration checkboxes into one UI. 
  * Script (Java) - Modules with 5 inputs and 5 outputs that can take a Java code as string to act open this values.
  

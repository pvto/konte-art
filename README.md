# Konte
## Generative Art

Konte is a small language for generating images, "drawing by coding".

See [Development ideas](DEVELOPMENT_IDEAS.md) if you are looking for a feature or a bugfix, or you can file a bug report at github.

![Cheatsheet](img/README/cheatsheet.png)

![Gui screenshot](img/README/konte-screenshot-D.png)

## Building and running

Build the project from a command line.

```
$ mvn clean install
```
Run konte GUI from the command line. Within, the *Tutorial* menu helps to a quick start.

Linux:
```
$ java -Xmx2048m -cp target/konte.jar:target/dependency/rsyntaxtextarea-2.5.8.jar org.konte.ui.Ui
```
Windows:
```
> java -Xmx2048m -cp target/konte.jar;target/dependency/rsyntaxtextarea-2.5.8.jar org.konte.ui.Ui
```

Generate a single png image from the *command line*.

Mac:
```
$ java -Xmx2048m -cp target/konte.jar org.konte.misc.CommandLine -fgrammar.c3dg -s1200x600 -dmygram.png
```

## What the app does

 - projects arbitrary z-ordered and linearly transformed 3D bezier paths on a Java2D canvas
 - there is no edge clipping, so 3D is "semi" in this way; you should avoid excessively large shapes that could create strange overlap effects
 - there is a layering property in the language, so you can draw on multiple layers like in Photoshop
 - there is an ad hoc support for meshes
 - RGBA, HSLA and user defined color spaces are supported
 - a simple "independent shapes" lighting model is supported, with lights controlled by arbitrary expressions
 - 3D, ortographic, cabinet (oblique), fisheye perspectives are supported

![meshm2.png](img/README/2015-02-24-02-09-meshm2.png)
[meshm2.c3dg](img/README/meshm2.c3dg)

![Recursions cheatsheet](img/README/recursions-cheatsheet.png)
[recursions cheatsheet](./src/main/resources/org/konte/resources/exmpl/recursions_cheatsheet)

![paint-w-light-e-ADU.png](img/README/2017-01-12-23-58-paint-w-light-e-ADU.png)

![Fisheye](img/README/2017-02-11-22-02-FISHEYE-city-b-ABM.png)
[fisheye-city](https://github.com/pvto/konte-snippets/blob/master/test/FISHEYE-city-b.c3dg)

![Functions cheatsheet](img/README/funcs-scheatsheet.png)
[functions cheatsheet](./src/main/resources/org/konte/resources/exmpl/functions_cheatsheet)

![subdiv-rupt-AFF.png](img/README/2017-01-21-15-02-subdiv-rupt-AFF.png)
[subdiv-rupt.c3dg](img/README/subdiv-rupt.c3dg)

![deviate-b-3-b-ADC.png](img/README/2017-02-19-13-44-deviate-b-3-b-ADC.png)
[deviate-b-3-b.c3dg](img/README/deviate-b-3-b.c3dg)

## Scripting with konte, an introduction

Konte is a mutation of the [contextfreeart.org](http://contextfreeart.org/) language.  In konte, you draw in three dimensions.

There are some predefined shapes like ```SQUARE``` and ```RSQU``` (a rounded square) that you can draw. Here is the list, and you can create your own shapes too.

### Shapes

![shapes.png](img/README/2015-02-23-22-41-shapes.png)
[shapes.c3dg](img/README/shapes.c3dg)

```
SQUARE {...}
RSQU {...}
CIRCLE {...}
TRIANGLE {...}
RTRIANGLE {...}
BOX {...}
PIPE {...}
CONE {...}
SPHERE {...}
```

I find flat shapes like squares and circles most useful in the pack.

### User paths

Also user paths like the following are supported.

![hearts.png](img/README/2015-02-24-00-36-hearts.png)
[hearts.c3dg](img/README/hearts.c3dg)

There is a basic svg path import functionality in the supplied GUI, so you could draw your paths in a vector app or use some clipart paths and import them.  Keep in mind that konte draws in the {0..1,0..1} xy space by default, and it will try to scale an imported svg into that space.

You can also write a path by hand if you like the excercise.

```
path heart {
    moveto(0.000, 0.000, 0.000)
    bend(0.000, 0.300, 0.000)
    bend(0.400, 0.400, 0.000)
    curveto(0.400, 0.100, 0.000)
    bend(0.400, -0.200, 0.000)
    bend(0.000, -0.500, 0.000)
    curveto(0.000, -0.500, 0.000)
    bend(0.000, -0.500, 0.000)
    bend(-0.400, -0.200, 0.000)
    curveto(-0.398, 0.093, 0.000)
    bend(-0.400, 0.400, 0.000)
    bend(0.000, 0.300, 0.000)
    close
}
```

### Determinism and two types of randomness

Konte uses a seeded random feed to decide what it does next.  By multiply overriding a single rule like this,
```
do 1 {...}
do .1 {...}
do .05 {...}
```
you let konte decide which path it will take, relying on the "probabilities" ```1``` and ```.1``` and ```.05```.  I say "probabilities", because the weights do not have to add up to one.

With the same seed, say 'ADD', konte will always generate the same image.

![do-w-meshes-col.png](img/README/2015-03-03-21-00-do-w-meshes-col.png)
[do-w-meshes-col.c3dg](img/README/do-w-meshes-col.c3dg) (variation ADD)

The ```rndf()``` function then provides a random value from a uniform distribution. (For other distributions, see *Other functions* below.)

![rndf.png](img/README/2015-03-03-20-49-rndf.png)
[rndf.c3dg](img/README/rndf.c3dg) (variation AAA)


There is also a non-seeded, non-deterministic random way, by using the ```rnd()``` function:
```
example2 {SQUARE {scale rnd()}}
```
Even if the seed stays the same, the image will look different over different renders.

```rnd()``` and ```rndf()``` would take a single pass over and use a random value within [0..1] within their rule for an entire image.  I use a little trick of backreferencing a model property to enforce dynamic randomness:
```
example3 {SQUARE {scale rnd()+x*0}}
```


### Rules and loops

```
scene {
  cube{roty 40 rotx 20}
}
cube {
  3*{x 1/12}
    3*{y 1/12}
      3*{z 1/12}
        featurez{x -1/12 y -1/12 z -1/12 scale -1/12}
}
featurez {
  50*{z .01} RSQU{}
  RSQU{scale .8 red 1 sat -.7 hue 360*rnd()+x}
}
```
![cubes](img/README/2015-02-23-21-57-cubes.png)
[cubes.c3dg](img/README/cubes.c3dg)

When the above fragment gets parsed into a model, the resulting tree structure looks something like this.
```
{ scene{ cube{ *{*{*{ featurez{ *{RSQU},RSQU } }}} } } }
```

As we read from top downwards, three rules are defined above: ```scene```, ```cube```, and ```featurez```.  'Scene' is the first rule and it will be the starting point: ```cube{roty 40 rotz 20}``` tells the generator first to rotate over current y axis by 40 degrees and over current x axis by 20 degrees and then jump to rule 'cube'.

There are three nested loops in ```cube```, creating 27 branches in total:
```
[A]  3*{x 1/12}
[B]    3*{y 1/12}
[C]      3*{z 1/12}
        . . .
```
By each iteration of [A], current x position is incremented by 1/12, by each iteration of [B], y by 1/12 likewise, and so for z of [C].

When the generator first handles ```featurez``` rule, it is on loop zero and its x, y and z positions are *in pristine state*.  Now the transforms in ```featurez{x -1/12 y -1/12 z -1/12 scale -1/12}``` are applied.  It happens before any loop increments, and the group gets centered around its centermost element.  First, initial transforms, and on top of that, accumulating increments in loops.

Within ```featurez```, we draw on the screen then, creating one of the 27 objects in the picture.
```
featurez {
  50*{z .01} RSQU{}
  RSQU{scale .8 red 1 sat -.7 hue 360*rnd()+x}
}
```
```50*{z .01} RSQU{}``` draws fifty black rounded squares, traveling slightly away from the screen plane.  There are 27 * 50 black shapes in the picture overall.

```RSQU{scale .8 red 1 sat -.7 hue 360*rnd()+x}``` adds a shape of a random hue.  There are 27 colored shapes in the picture.  

![2015-02-25-01-14-cubes-big.png](img/README/2015-02-25-01-14-cubes-big.png)
[cubes-big.c3dg](img/README/cubes-big.c3dg)

### Cameras

There are different types of cameras in konte.
```
camera { SIMPLE }
```
![buildings.png](img/README/2015-02-25-20-45-buildings.png)
[buildings.c3dg](img/README/buildings.c3dg)
```
camera { PANNING 2.0 }  /* with initial distance -2.0 from origo */

camera { ORTOGRAPHIC }


/*
*  a cabinet perspective with a 30 degree tilt and a scale factor of 0.5:
*/
camera { CABINET 30 0.5 }
```
![cabinet-tuto.png](img/README/2017-02-20-21-55-cabinet_camera-AAB.png)

```
camera { FISHEYE .5 0 1 .5 }
```
![Fisheye](img/README/2017-02-20-21-54-fov-tuto-AAB.png)

```
camera { ZPOW 4 }  // proj(x,y,z) = {x/z^4, y/z^4}
```
![ZPOW](img/README/2017-02-22-22-23-zpow_camera-AAA.png)
[ZOPW tutorial](https://github.com/pvto/konte-snippets/blob/master/help/zpow_camera.c3dg)

```
/*
* an experimental projection:
*  [x,y] = [cos(alpha) / dist, sin(alpha) / dist]
*    where alpha = atan( x / y )  (for a point relative to the camera)
*    and dist = ( x^2 + y^2 + z^2 ) ^ (0.5 * pack)  (distance of the point from the camera)
*    where pack is a user given packing exponent, default 1.0
*/
camera { CIRCULAR 2.0 }
```
![buildings-circular.png](img/README/2015-02-25-21-27-buildings-circular.png)
[buildings-circular.c3dg](img/README/buildings-circular.c3dg)


### Colors

Konte handles RGBA and HSLA color spaces.  HSL support is based on RGB, so it is not complete.  Adjusting the hue of a uniform grey will not do anything. There is no return from a uniform grey back to a previously used hue.

![HSL.png](img/README/2015-03-01-23-09-HSL.png)
[HSL.c3dg](img/README/HSL.c3dg)

```
R //  alias red   [0..1]
G //  alias green [0..1]
B //  alias blue  [0..1]
A //  alias alpha [0..1]
H //  alias hue [0..360]
S //  sat(uration) [0..1]
L //  alias lightness [0..1]
```

![RGB.png](img/README/2015-03-01-23-31-RGB.png)
[RGB.c3dg](img/README/RGB.c3dg)

#### User colorspaces

A script can define its own colorspace and use it by setting ```shading``` and ```col0```.  Unlike to R,G,B and other properties, ```shading``` and ```col0``` are set as absolute values, and not incremented.

![draw-shading.png](img/README/2015-02-26-23-06-draw-shading.png)
[draw-shading.c3dg](img/README/draw-shading.c3dg)
[eye.c3dg](img/README/eye.c3dg):
```
shading eyeshades {
    point(-2)    { RGB  1  1  1 A 0  }
    point(0)    { RGB  .1  0  0  }
    point(0.1)  { RGB  .4 .2  0  }
    point(0.2)  { RGB 0.075 0.506 0.875  }
    point(0.4)  { RGB 1 .3 .2 A .4 }
    point(0.5)  { RGB 1  1  1   }
    point(.6)  { RGB 1  1  1  A 0 }
}
```

#### Extending user colorspaces

User colorspaces can be extended to arbitrary dimensions by making them dynamic.  Here's an extension to two dimensions, with the help of a ```lirp``` function (linear interpolation).

![extending-user-1d-colorspace.png](img/README/2015-02-27-17-24-extending-user-1d-colorspace.png)
[extending-user-1d-colorspace.c3dg](img/README/extending-user-1d-colorspace.c3dg)

```
shading extended {
    point(0.0) { A 1
        RGB lirp(0,1,.5,.3,SAT) lirp(0,1,.5,.1,SAT) lirp(0,1,.5,0,SAT)}
    point(0.25) { A 1
        RGB lirp(0,1,.5,.9,SAT) lirp(0,1,.5,0,SAT) lirp(0,1,.5,.6,SAT) }
    point(.5) { A 1
        RGB lirp(0,1,.5,.1,SAT) lirp(0,1,.25,1,SAT) lirp(0,1,.5,.8,SAT) }
    point(.75) { A 1
        RGB lirp(0,1,.5,1,SAT) lirp(0,1,.5,.7,SAT) lirp(0,1,.5,.7,SAT) }
    point(1) { A 1
        RGB lirp(0,1,.9,1,SAT) lirp(0,1,.9,.7,SAT) lirp(0,1,1,1,SAT) }
}
```

This colorspace is composed of five successive points within [0,1] that you reference with setting ```col0 0.25``` etc.  To break a color towards black or towards white, set ```DEF{SAT=X}``` where X=0 would give black and X=1 would give white.

### Lighting

Konte allows placing lights in the space.  Rather than lights though they can be thought of as spatial expressions that modify object color.

A light consists of a spatial expression for a point light, a color expression for the light color, a scale expression giving the spherical radius, and an optional type that states if a light should be of complementary color or if it should create darkness instead of light.

```
light {point(0,0.1,-1){RGB 1 .9 .9} s 1}
```
![icescape.png](img/README/2015-02-25-21-49-icescape.png)
[icescape.c3dg](img/README/icescape.c3dg)

We can mix lights and darkness to create ambient effects.  The spatial expression for a light can backreference shape properties.  In effect this means that, while drawing an object on the screen, all light expressions are evaluated on that object, and object color is modified using this dynamic outcome.

```
light {point(0,0.1,-1){RGB 1 .9 .9} s 1}
light {DARKNESS point(x+rnd(),0.1,-1){RGB 1 1 1} s .5}
```
![icescape.png](img/README/2015-02-25-22-09-icescape.png)


```
light {point(0,0.1,-1){RGB 1 .9 .9} s 1}
light {COMPLEMENTARY point(.5,.1,.1){RGB 1 0 0} s .1}
```
![icescape.png](img/README/2015-02-25-22-15-icescape.png)

### Phong lights

Phong lights are fully controllable by dynamical expressions.

![phong_possible.png](img/README/2016-09-03-14-12-phong_possible-A.png)
[phong_possible-A.c3dg](img/README/phong_possible-A.c3dg)

![phong_impossible-C.png](img/README/2016-09-03-16-50-phong_impossible-D.png)
[phong_impossible-C.c3dg](img/README/phong_impossible-D.c3dg)


### Drawing meshes

Each tree trunk or branch or a branch of branch (exluding its leaves) is a separate mesh in the following picture.

![tree-mesh.png](img/README/2015-02-26-11-44-tree-mesh.png)
[tree-mesh.c3dg](img/README/tree-mesh.c3dg)

A mesh is created by first defining which mesh we are piling to, by doing ```DEF{mesh=1}``` (or the shorthand version used in the example, ```{mesh=1}```), and then adding segments to the current mesh by calling on the predefined shape MESH.

That the mesh be drawn, we need to add elements in rows, creating a tabulation of quadrilaterals.  In the example, each column is a tree branch segment that consists of ten quadrilaterals, taking the form of a pipe together: ```10*{ry 36 {row=row+1}} MESH{z 1}```.


### Macros

Macros in konte are multivalent lambda expressions that can shorten and clean up code when used prudently.  The following is a polar version of the [Devil's staircase](http://en.wikipedia.org/wiki/Cantor_function) fractal, where polar coordinate mappings are defined as macros like this: ```MACRO Xsc cos((X-SX/2)*WD)```.

```X```, ```SX``` and ```WD``` here are lambda expressions that konte will evaluate, ```cos``` is a predefined function, and ```Xsc``` simply is the macro name.

![devils-staircase.png](img/README/2015-02-26-12-13-devils-staircase.png)
[devils-staircase.c3dg](img/README/devils-staircase.c3dg)

### Context lookup (octree model)

An octree model can be used to record and lookup nearby features.
This places some memory burden and generally slows the app down slightly or
more, depending on feature density and lookup context size.

Q: What triggers the octree model?
A: Using any of the neighborhood functions in a script.

![neighborhood cheatsheet](img/README/2017-02-22-21-05-neighborhood_cheatsheet-AAB.png)
[neighborhood cheatsheet.](https://github.com/pvto/konte-snippets/blob/master/help/neighborhood_cheatsheet.c3dg)

### Dynamic paths

If we look at the Devil's staircase example above, it draws a polar cantor segment by using a lambda based path that dynamically adapts to its environment.

```
path P
{
  moveto( Xsc*WDT, Xss*WDT, 0)
  lineto( Xsc*LEV, Xss*LEV, 0)
  lineto( Xac*LEV, Xas*LEV, 0)
  lineto( Xac*WDT, Xas*WDT, 0)
  close
}
```
This technique could aid in problems like fancy charting.

![barchart.png](img/README/2015-02-26-22-41-barchart.png)
[barchart.c3dg](img/README/barchart.c3dg)

```
path P
{
  moveto( 0, 0, 0)
  bend( -W/4, Y/2, 0)
  bend( -W/4, Y/2, 0)
  curveto( 0, Y, 0)
  lineto( W, Y, 0)
  bend( W+W/4, Y/2, 0)
  bend( W+W, Y/4, 0)
  curveto( W, 0, 0)
  close
}
```

## Predefined functions

Here is a list of functions that can be called in konte.  Additional user defined functions must be installed via a script, through the scripting interface.

### Algebraic and trigonometric functions
```
abs  // absolute value.      Example:  abs(-2.1)
sqrt // square root.         Example:  sqrt(2)
log  // 10-based logarithm.  Example:  log(100)
pow  // power.               Example:  pow(2, 4) -> 16
round // Rounds towards the nearest integral value.
      //                     Example:  round(0.5) -> 1.0
floor // Round downwards to nearest int.
      //                     Example:  floor(0.9) -> 0
max  // maximum.             Example:  max(2, 1) -> 2
min  // minimum.             Example:  min(3, 1) -> 1
mean // the mean of the given arguments.
     //                      Example: mean(0.1, 2, x)

sin  // sine function.       Example:  sin(PI/2)
cos  // cosine function.     Example:  cos(0)
tan  // tangent function.    Example:  tan(2/3)
asin // arcus sine.          Example:  asin(sin(PI/2))
acos // arcus cosine.
atan // arcus tangent.

```
### Other functions
```
rndf // random number [0,1). Example:  rndf()
     //  Uses a seeded random feed.
rnd  // random number [0,1). Example:  rnd()

     // rnd() does not draw from the variation random feed,
     // but from system random number generator.
     // This may change in the future.

irndf// random int [0,n).    Example:  irnd(10) -> one of 0..9
     //  Uses a seeded random feed.
irnd // random int [0,n).    Example:  irnd(10) -> one of 0..9

binm  // binomial distribution, p.d.f.: binm(n, p, x)
                             Example:  binm(10,0.5,1) -> 0.009765625
binmc // cumulative binomial dist.
                             Example:  binmc(10,0.5,9) -> 0.9990234
brndf // seeded random integer [0,n] from a binomial distribution
                             Example:  brndf(10, 0.5) -> 3
                             (the mean of these would --> 5 as n --> Inf.)
brnd  // random integer [0,n] from a binomial distribution
                             Example:  brnd(10, 0.1) -> 1

hypg  // hypergeometric distribution, p.d.f.: hypg(N1, N2, n, x)
hypgc // cumulative hypergeometric dist.
hypgrndf // seeded random number from a hypergeometric distribution
hypgrnd  // random number from a hypergeometric distribution


saw  // Saw wave function -> [0,1], period 1.

     // Examples:
     //    saw(0) -> 0
     //    saw(0.25) -> 0.5
     //    saw(0.5) -> 1
     //    saw(0.75) -> 0.5
     //    saw(1) -> 0,
     //   . . .
square // Square wave function -> {0,1}, period 1.

       // Examples:
       //  square(0) -> 0
       //  square(0.25) -> 0
       //  square(0.5) -> 1
       //  square(0.75) -> 1
       //  square(1) -> 0
       // . . .
hipas  // "high-pass" function.

       // Examples:
       //  hipas(0.25, 0.5) -> 0
       //  hipas(0.61, 0.5) -> 0.61

lopas  // "low-pass" function.

       // Examples:
       //  lopas(0.25, 0.5) -> 0.25
       //  lopas(0.61, 0.5) -> 0

lirp   // Linear interpolation function with an adjustable middle

       // Examples:
       //  lirp(0, 1, 0.5, 0.3, 0.5) -> 0.3
       //    (from range [0,1] with a middle point at 0.5 receiving value 0.3,
       //     interpolate at x = 0.5)
       //  lirp(0, 1, 0.5, 0.3, 0.25) -> 0.15
       //    (from range [0,1] with a middle point at 0.5 receiving value 0.3,
       //     interpolate at x = 0.25)

mandelbrot  // fractal function [0,255].
       // Examples:
       //  mandelbrot(0.5, 1)

julia       // fractal function [0,255].
            // 3rd and 4th arguments are z0 on the complex plane
       // Examples:
       //  julia(0.5, 1,  0.25, 0.25)

```

## Working with SVG

You can use konte to create svg graphics.  This requires checking a box in the *Generate* submenu, due to svg export currently requiring objects stay longer in the memory than bitmap export.

The workflow is like this: first choose canvas dimensions (Ctrl+Shift+R) and generate to desired size, then export to svg (Ctrl+G).  You should get a new svg file with your objects on an empty background.

Here's an example with bitmap and svg versions.  If you zoom in, there are clear-cut figures standing on svg roofs.

![export-svg-example.png](img/README/2015-02-28-22-22-export-svg-example.png) ![export-svg-example.svg](img/README/export-svg-example.svg)
[export-svg-example.c3dg](img/README/export-svg-example.c3dg)

The png is 113 times smaller (!) than the svg that soars up to 3.6M with 14.000 paths.  (I find big svg images with more than 20K paths intolerable, but your situation may be different.)

## More examples

<!--![logo_new-c-rec.png](img/README/AAS-logo_new_c_rec.png)
![binbu.png](img/README/binbu.png)-->


![monet-grey](img/README/monet-grey.png)

I created a hairy version of Claude Monet's [Three Trees in Grey Weather](http://www.wikiart.org/en/claude-monet#supersized-featured-212779).

Some constants are defined here using ```DEF```, and also some variables are used within rules (those ```DEF```'s within loops).  Finally, pixel values are retrieved from a bitmap on the local disk.
```
bg {RGB 0.980 0.969 0.914}
include "~/Pictures/monet-grey.png" img0
DEF iw imgwidth(img0)
DEF ih imgheight(img0)
DEF pixsize (1/imgwidth(img0))
camera {z -1.3}

SS {
    draw_img{ y -.1 rx 15 ry 3}
}
rule draw_img {
    (iw) * { DEF {u=u+1} }
        (ih) * { DEF {v=v+1} }
            i_pxl
            {
                x -.5 y (.5*ih/iw)
                s pixsize
                x u y -v
                rz (((u*u+v*v)+sin(u*7+v*7)))
                RGB imgred(img0,u%iw,v%ih)
                    imggreen(img0,u%iw,v)
                    imgblue(img0,u%iw,v)
            }
}
rule i_pxl { SQUARE { s 40 .05 1 }}
```

![logo_new.png](img/README/ABM-logo_new.png)

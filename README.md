# konte-art
*This is an art project for generative graphics. Its code is old and crappy, but there are one or two nice things here. I'll be cleaning up the code, but you are warned.*

Konte is a small formal language and an execution environment for generating png images. It projects arbitrary z-ordered and linearly transformed 3D bezier paths on a Java2D canvas.

 - Bezier paths have the convenient property of translating to higher dimensions
 - a Java2D bezier routine is used here to draw 3D shapes
 - there is no edge clipping, so 3D is "semi" in this way
 - you should avoid excessively large shapes that could create strange overlap effects
 - there is a layering property in the language, so you can draw on multiple layers like in Photoshop

##Building and running

Build the project from command line.

```
$ mvn clean install
```
Run the buggy UI from command line.

```
$ java -Xmx2048m -cp target/konte.jar org.konte.ui.Ui
```

There are online examples in the *Tutorials* menu, so you should be good from that on.  I'll give an overview of the language in the following though.

##The konte generative language
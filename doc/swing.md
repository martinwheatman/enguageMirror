## Java Swing Example

I've created and uploaded a Java
Swing example of Enguage library to opt/swing.  To build this,
type:
```
    git clone https://github.com/martinwheatman/enguage.git
```
or do a 'git pull' on your existing repo.

Then, inside the repo, type:
```
    make swing
```
This will make a simple command in bin/swing which runs the example
code, the source code is in opt/swing . To clear up the repo you can
type:
```
    make clean
```
If you have problems with this, you can manually create and run the
example with (assuming you have created the enguage.jar file):
```
    make jar
    javac opt/swing/EnguagePanel.java
    java -cp .:lib/enguage.jar opt.swing.EnguagePanel
```
I've also uploaded an example video to https://www.academia.edu/video/k77Qmk

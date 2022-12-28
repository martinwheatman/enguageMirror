# Android
Enguage has been an Android app since 2013, 
and while it is still (only just!) on the Play store, 
it is probably a better bet to built from the source: the Enguage jarfile and the Android specific Assets.java file to access the Android specific assets/ directory.
Enguage is developed in Eclipse, as a platform independent IDE,
these instructions are for migrating the resultant jarfile into Android Studio 4.1.3,
build March 10, 2021.
## How to build a simple Enguage Android app
Firstly, it seems that AndroidStudio SDK only use Java 1.8,
so you will need to install this, otherwise you will get complaints about your jarfile needing to be version 52. To check which version you have, type in a shell:
<ul>
<li>javac -version
</ul>
Assuming you need to up/downgrade, in a shell:
<UL>
<li>sudo apt update
<li>sudo apt install openjdk-8-jdk-headless
<li>sudo update-alternatives --config javac
</UL>
You might also want to clear out the (old) class files from the Enguage repo:
<ul>
<li>find . -name "*.class" -exec rm {} \;
</ul>
Then, create the Enguage jarfile for android (this is without the Assets class):
<ul>
<li> make android </li>
</ul>
Now you're ready to move across to AndroidStudio.
Resources for the app are [in this repo](../opt/android.app).
<ul>
<li>Create a new Android project, selecting "Empty Activity".
In this example I've called the project Enguage.
Build and run it on your device to make sure its ready.
</ul>
Now you're ready to copy across the various assets from the Engauge repo into the Android Studio project:
<ul>
<li> Copy the content of MainActivity.jv 
from opt/android.app/src.main/java.com.yagadi/ into MainActivity.java;
<li> Copy the Assets.jv file into $HOME/AndroidStudioProjects/Enguage/app/src/main/java/com/yagadi, as Assets.java;
<li> Ceate a new directory $HOME/AndroidStudioProjects/Enguage/app/libs
<li> Copy into this the jafile, lib/anduage.jar
<li> Locate this in the Android Studio (try the 'breadcrumbs' at the top, if it is not in the Project Explorer.) Right click on this in Android Studio and select "Add as library"
<li> Create an assets folder for the Enguage repertores. Right-click in Android Studio -> New -> Folder -> Assets Folder
This will be, by default created in AndroidStudioProjecct/Engauge/app/src/main
<li> Copy into this the content of etc/, i.e. config.xml and rpt/
</ul>
Following this there may be XML resources to copy across, such as:
<ul>
<li>Colors,
<li>Strings, and
<li>Layouts
</ul>
If I've missed anything, please feel free to 'remind' me. Thanks! :)
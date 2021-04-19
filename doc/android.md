# Android
Enguage has been an Android app since 2013, 
and while it is still (only just!) on the Play store, 
it an be built easily from the source.
These instructions relate to Android Studio 4.1.3, build March 10, 2021.
You can probably do a better job than me!
## How to build a simple Enguage Android app
Resources for the app can be [in this repo](../opt/android.app).
The steps are as follows:
<ul>
<li> Create a new Android project, selecting "Empty Activity".
In this example I've called the project Enguage.
<li> Copy the content of the resources file MainActivity.jv 
from opt/android.app/src.main/java.com.yagadi/ into MainActivity.java .
For this , you may need to adjust the package name at the top of the file.
<li> Copy into the same directory, the Assets.jv file to Assets.java,
again adjusting the package name.
</ul>
In the enguage repo, create the Enguage Jarfile for android (without the Assets class):
<ul>
<li> make android </li>
<li> cp lib/anduage.jar $HOME/AndroidStudioProjects/Enguage/app/libs
<li> Right click on this in Android Studio and select "Add as library"
</ul>
You will also need to add in the opt/android.app/assets directory (that is ./etc) into you android project
<ul>
<li> Right-click in Android Studio -> New -> Folder -> Assets Folder
This will be, by default created in AndroidStudioProjecct/Engauge/app/src/main
<li> Copy into this the content of &lt;your repo>/etc/, e.g. config.xml and rpt/
</ul>
Following this there may be XML resources to tidy up, such as:
<ul>
<li>Colors,
<li>Strings, and
<li>Layouts
</ul>
If I've missed anything, please feel free to 'remind' me. Thanks! :)
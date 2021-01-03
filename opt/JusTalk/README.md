# JusTalk - A Vocal Web Driver

JusTalk is an *experimental* keyword interpreter, directed at Web interaction.
These keywords drive the actions of a browser.
In keeping with Speech Act Theory, there are felicitous, informative replies to 
reassure the user that the appropriate action has been performed.
It is like a Blocks World program,
[SHRDLU](https://en.wikipedia.org/wiki/SHRDLU), 
but interacting with a webpage-of-widgets, rather than a tray-of-blocks.
Specifically, interaction is with the Document Object Model,
you are not interacting with the webpage HTML, 
but with the live document.
Much of the code, therefore, is aimed at working around the document as injected 
by the web page, and by the visual elements of layout:
e.g. ignoring the header and footer code!

## Latest!
Now includes phonetic spelling, e.g.: click on the mail spelt mike alpha lima echo radio button

Because we have no power over what the speech-to-text software is going to hear,
we need to be able to specify what spelling we want. The phonetic alphabet is:

                        "alpha",  "bravo",    "charlie", "delta",  "echo",   "foxtrot",
                        "hotel",  "golf",     "india",   "juliet", "kilo",    "lima",
                        "mike",   "november", "oscar",   "papa",   "quebec",  "romeo", 
                        "sierra", "tango",    "uniform", "victor", "whiskey", "x-ray",
                        "yankie", "zulu"

## To install JusTalk:

Go to the [JusTalk Chrome Extension webpage](https://chrome.google.com/webstore/search/enguage?h1=en), and add it to your browser.

Because this may be out of date--it takes a few days for the upload to become visible--you may want [follow these instructions](https://youtu.be/6yZKteo1a2I):

- download these files in this repo into a directory/folder;
- go to chrome://extensions
- switch on "Developer mode"
- Select "Load Unpacked", and load the downloads from the new directory/folder created above.

If Chrome does not responding to the Ctrl-Space sequence, it
may be that you need [to unblock your microphone](https://www.youtube.com/watch?v=TiZcsd_BahU).

## Use cases:

- Ctrl-Space and say "Hello"; hit Crtl-Space again, for it to start listening!
- [Navigating to a story on the BBC News website](https://www.youtube.com/watch?v=Q9PAZGEJe0E&t=2s);
- [Using Google to find an article on Wikipedia](https://www.youtube.com/watch?v=yWuij7lBooQ);
- Tweeting by voice, still trying!
- Facebook/Instagram: I'm not on these platforms, anyone else?

## Command details

Commands are given as verb-phrase string-literals, interspersed with noun-phrase variables.
So the comand to click on a button, radio button or checkbox is currently described as:
- click on X

Where X is the 'name' of the button, or label of the radio button or checkbox. Following the specification for clickable items, this is either the title, value, innerText, radio buttons, checkboxes or the 'submit' button.

Similarly, the page reader included the commands:
- read .. from X
- read .. from the main heading

Text and text areas are managed with the commands:
- set the value of X to Y
- get the value of X

In describing the page, there are several commands:
- describe the page
- how many \[buttons|values|links|figures|[level n]headings|paragraphs] are there
- how do I \[navigate|interact|query]
- what \[buttons|values|links|figures|title|[level n]headings|paragraphs] are there

## Notes

- You'll need Chrome for the moment, I'm afraid.
If there's anyone out there in webland who knows a bit about manifests and can make 
this work on Firefox (etc.), please feel free to get in touch/fix. I *will* address this, but its not high priority.
- you'll need to manually allow the microphone access to each page you visit. This pop-up seems to be inaccessible at the moment.
- this is experimental in that the source code may change widely as the extension develops.
- N.B. Radio buttons and checkboxes are now in this version, and should be available on the Chrome webstore when approved.

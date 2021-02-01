# JusTalk - A Vocal Web Driver
JusTalk is an webpage interpreter, directed at vocal interaction.
Keywords within HTML, like button and input, provide the actions you can perform.
However, interaction is with the Document Object Model,
so often you are not interacting with a static HTML file, 
but with a generated, live document.
Much of the code, therefore, is aimed at working with the web page as loaded, 
and working around the visual elements, such as layout!
It is rather like the [Blocks World program](https://en.wikipedia.org/wiki/SHRDLU), 
but you are interacting with a webpage-of-widgets, rather than a tray-of-blocks.

Informative replies reassure the user that the appropriate action has been performed.

+ [How to install JusTalk.](docs/install.md)
+ [Commands and use cases](docs/commands.md)
+ [How to define what is said](docs/spelling.md)

## Notes
- You'll need Chrome for the moment, I'm afraid.
If there's anyone out there in webland who knows a bit about manifests and can make 
this work on Firefox (etc.), please feel free to get in touch/fix.
- you may need to manually allow the microphone access to each page you visit.

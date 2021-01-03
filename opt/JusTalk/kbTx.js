chrome.commands.onCommand.addListener(function(command) {
  if (command === 'enguage') {
    // find the active tab...
    chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
      //send it a message...
      chrome.tabs.sendMessage(
        tabs[0].id,          // index not always 0?
        null, // message sent - none required?
        null                 // response callback - none expected!
      //function(response) {console.log("done" /*response.farewell*/);}
      );
    });
  }
}); 
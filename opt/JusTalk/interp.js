var felicity = [ "sorry", "ok", "yes", "no" ];
var reply = [ "i don't understand", "i don't know" ];
var verbose = false;

window.SpeechRecognition = window.webkitSpeechRecognition || window.SpeechRecognition;

/*
function pageLoad() {
    if (verbose) {
        var title = document.getElementsByTagName( "title" );
        window.speechSynthesis.speak(
            new SpeechSynthesisUtterance(
                felicity[ 1 ]+ ", "+ (title.length == 0 ? "an untitled page" : title[ 0 ].innerText) +" has been loaded"
        )   );
}   }
window.addEventListener( "load", pageLoad, false );
*/

chrome.runtime.onMessage.addListener(
    function(request, sender, sendResponse) {

        window.speechSynthesis.pause(); 

        var recognition = new SpeechRecognition();
        recognition.start();
        recognition.continuous = false;
        recognition.onresult = function(event) {
            if (event.results[0].isFinal) {

                var utterance = event.results[0][0].transcript;

                if (utterance == "pause" ||
                    utterance == "pores" ||
                    utterance == "pours" ||
                    utterance == "pools" ||
                    utterance == "paws")
                    ; //window.speechSynthesis.pause();

                else if (utterance == "continue")
                    window.speechSynthesis.resume();

                else {
                    window.speechSynthesis.cancel();
                    window.speechSynthesis.speak(
                        new SpeechSynthesisUtterance(
                            interp( utterance )
                    )   );
                }
    }	}   }
);
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
function includesWord( src, trg ) { // to stop 'male' matching 'female'
    if (typeof src != "string" || typeof trg != "string") return false;
    src = " "+ src.toLowerCase();
    trg = " "+ trg.toLowerCase();
    return src.includes( trg );
}
function shift( cmd, n ) {
    for (i=0; i<n; i++) cmd.shift();
    return cmd;
}
var clickMe = null;
function clickClickMe() {
	if (clickMe != null) {
		clickMe.click()
		clickMe = null;
}	}
function clickOn( utt ) { // [click on] [the] X [button|link|checkbox|radio button]
    if (utt.length == 0)
		return felicity[0] + ", "+ reply[ 0 ] +": click on ";
	else {
        var orig = utt.join( " " );
	    var  the = "";
	    if (utt[ 0 ] == "the") {
	        the = "the ";
	        utt.shift();
	    }
	    if (utt.length == 0)
	    	return felicity[0] + ", "+ reply[ 0 ] +": click on the";
	    else {
		    var buttons = null, links = null, inputBt = null, labels = null;
		    var elemType = utt.length < 1 ? "" : utt[ utt.length -1 ],
                typeAdj  = utt.length < 2 ? "" : utt[ utt.length -2 ]; // "radio" ?
		    if (elemType == "link") {
		        links = document.getElementsByTagName( "a" );
		        utt.pop();
		    } else if (elemType == "button" ) {
                if (typeAdj != "radio") 
		            buttons = document.getElementsByTagName( "button" );
                else
                    utt.pop(); // remove radio
		        inputBt = document.getElementsByTagName( "input" );
		        utt.pop(); // remove button
		    } else if (elemType == "checkbox" ) {
		        inputBt = document.getElementsByTagName( "input" );
		        labels  = document.getElementsByTagName( "label" );
		        utt.pop();
		    } else {
		        elemType = "";
		        buttons = document.getElementsByTagName( "button" );
		        inputBt = document.getElementsByTagName( "input" );
		        links   = document.getElementsByTagName( "a" );
		    }
		    if (utt.length == 0) {
                alert( "failing here?" );
                return felicity[0] + ", "+ reply[ 0 ] +": click on "+ orig;
		    } else {
                utt = utt.join( " " ); // is now a string
			    var clickable;
			    var candidates = [];
                if (buttons != null) for (i=0; i<buttons.length; i++) candidates.push( buttons[ i ]);
			    if (links   != null) for (i=0; i < links.length; i++) candidates.push(   links[ i ]);
			    if (labels  != null) for (i=0; i <labels.length; i++) candidates.push(  labels[ i ]);
			    if (inputBt != null) for (i=0; i<inputBt.length; i++)
			        if ((inputBt[ i ].type == "button"   && (elemType=="" || elemType=="button")) ||
                        (inputBt[ i ].type == "checkbox" && (elemType=="" || elemType=="checkbox"))||
                        (inputBt[ i ].type == "radio"    && (elemType=="" ||  typeAdj=="radio"))||
                         inputBt[ i ].type == "submit" )
			            candidates.push( inputBt[ i ]);
			    var clickables = [];
			    for (clickable of candidates)
			        if ((clickable.title != undefined     &&
                         includesWord( clickable.title, utt ))
                     || (clickable.value != undefined     &&
                         includesWord( clickable.value, utt ))
                     || (clickable.innerText != undefined &&
                         includesWord( clickable.innerText, utt ))
                     || (clickable.hasAttribute( "aria-label" ) &&
                         includesWord( clickable.getAttribute( "aria-label" ), utt ))
                     || (clickable.tagName == "INPUT"     && // need to check this too(!)
                         clickable.type    != undefined   &&
                         clickable.type    == "submit"    &&
                         utt               == "submit"     )
                     || (clickable.tagName == "INPUT"     &&
                         clickable.type    != undefined   &&
                         clickable.type    == "radio"    &&
                         (clickable.parentNode.nodeName == "LABEL" &&
                          includesWord( clickable.parentNode.innerText, utt ) ))) // label?
                        //if (!clickables.includes( clickable ))
                            clickables.push( clickable );

                // just click on first found for the moment.        
                switch (clickables.length) {
                    case 0:
                        return felicity[0] + ", no clickable items match: "+ orig;
                    default: //case 1:
                        clickMe = clickables[ 0 ];
                        setTimeout( clickClickMe, 1800 );
                        return felicity[1] +", "+ the + utt +" "+ elemType +" is clicked";
                    //default:
                    //    return felicity[0] +", several clickable items match "+ the + name +" "+ elemType;
}   }	}	}	}
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
function setValueTo( cmd ) { // set the value of X to Y
    if (cmd.length >= 3) {
    	var name = cmd[ 0 ]; cmd.shift();
	    while (cmd.length >= 2 && cmd[ 0 ] != "to") {
	        name += " "+ cmd[ 0 ];
	        cmd.shift(); // to?
	    }
	    if (cmd.length > 1) {
		    cmd.shift(); // to
            var value = cmd.join( " " );
            var elements = document.getElementsByTagName( "*" );
            for (el of elements) {
                if (el.tagName == "INPUT" &&
                    (el.type == "text" || el.type == "textarea") &&
                    (includesWord( el.placeholder, name ) ||
                     includesWord( el.title,       name )   ))
                {
                    el.value = value;
                    return felicity[ 1 ] +", "+ name +" set to "+ value;
            }   }
            return felicity[ 0 ] +", "+ name +" is not a value";
    }	}
    return felicity[0] +", "+ reply[ 0 ] +" "+ cmd.join(" ");
}
function getValueOf( cmd ) { // get the value of ...
    var rc = felicity[0] +", "+ reply[ 0 ];
    var name = cmd.join( " " );
    if (name.length > 0) {
    	var elem = document.getElementById( name );
	    rc = elem == null ?
	        felicity[ 0 ] + ", "+ name +" is not a value"
	        : felicity[ 1 ] + ", "+ name +" is "+
	            elem.value == undefined ? "unset" : "set to " + elem.value;
    }
    return rc;
}
function typeIn( utt ) {
    var msg = "nothing";
    var value = utt.join( " " );
    var elements = document.getElementsByTagName( "*" );
    for (el of elements){
        if (el.hasAttribute( "focus" )) {
            msg = "something else";
            if ( el.tagName == "INPUT" &&
                (el.type == "text" ||
                 el.type == "textarea"))
            {
                el.value = value;
                return felicity[ 1 ] +", "+ el.placeholder +" set to "+ value;
            }
    }   }
    return felicity[ 0 ]+ ", "+ msg +" has focus";
}
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
function go( cmd ) { // go ...
	var response = felicity[0] +", "+ reply[ 0 ] +": "+ cmd;
	if (cmd.length > 0) {
		if (cmd[ 0 ] == "to") {
			cmd.shift();
			if (cmd.length > 0) {
				address = cmd.join("");
				window.location.href="http://" + address.toLowerCase();
				response = felicity[ 1 ];
			}
		} else if (cmd[ 0 ] == "back") {
			window.history.back();
			response = felicity[ 1 ] +", gone back";
		} else if (cmd[ 0 ] == "forward") {
			window.history.forward();
			response = felicity[ 1 ] +", gone forward";
	}	}
	return response;
}
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
function hidden( elem ) {
    return elem.style.display    ==     "none"             ||
           elem.style.visibility ==   "hidden"             ||
           elem.style.visibility == "collapse"             ||
           (elem.style.visibility == "inherit"         &&
            hidden( elem.parentNode )                    ) ||
           (elem.hasAttribute( "aria-hidden" ) == true &&
            elem.getAttribute( "aria-hidden" ) == "true" );
}
function toNumerics( str ) {
    switch (str) {
        case "won"   : return "1";
        case "too"   :
        case "two"   :
        case "to"    : return "2";
        case "three" : return "3";
        case "for"   :
        case "fore"  : return "4";
        case "ate"   : return "8";
        default: return str;
}   }
function read( cmd ) { // read .../read from/read from main heading
    var response = "";
    var skip = 0;
    // do we have to read from something
    var readPHn = true; // unless we say otherwise, we're reading all paras and headers
    var findMe = "";
    var fromIndex = cmd.indexOf( "from" );
    if (fromIndex > -1) { // we've to find something
        for (i=0; i <= fromIndex; i++) cmd.shift();
        findMe = cmd.join( " " ).toLowerCase();
        if (findMe == "the main heading") {
            findMe = "";
            readPHn = false;
        } else if (cmd.length == 2 && cmd[ 0 ] == "paragraph") {
            skip = toNumerics(cmd[ 1 ]);
            readPHn = true;
            findMe = "";
    }   }

    // read text - headings and paragraphs
    for (elem of document.getElementsByTagName( "*" )) {

        if (!hidden( elem ) &&
            ( elem.tagName == "P" ||
             (elem.tagName.startsWith( "H" ) && elem.tagName.length == 2))) {

            // were at a 'paragraph'
            if (skip > 0) {
                skip--;
                continue;
            }
            // jump to a point in the text if required
            if (findMe != "") {
                if (response != "")
                    response += elem.innerText +" ";
                else {
                    var offset = elem.innerText.toLowerCase().indexOf( findMe );
                    if (offset != -1)
                        response += elem.innerText.toLowerCase().substr( offset ) +" ";
                }
            
            // are we reading from (first) main header H1
            } else if (elem.tagName == "H1" || readPHn) {
                response += elem.innerText +" ";
                readPHn = true;
    }   }   }

    return (response != "" ? response :
                felicity[ 0 ] +", "+
                    (findMe != "" ?
                        "i can't find the passage beginning with "+ findMe
                        : "i can't find anything to read. "))  +". ";
}
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
function articled( str ) {
	lett = str.toLowerCase().split(" ");
	if (lett[0].length == 1) {
		return (lett[0] == "a" || lett[0] == "e" || lett[0] == "f" || lett[0] == "h"
			 || lett[0] == "i" || lett[0] == "l" || lett[0] == "m" || lett[0] == "n"
			 || lett[0] == "o" || lett[0] == "r" || lett[0] == "s" || lett[0] == "x"
			 ? " an " : " a ")+ str;
	}
	switch (str[ 0 ]) {
		case 'a': case 'e': case 'i': case 'o': case 'u':
		case 'A': case 'E': case 'I': case 'O': case 'U':
			return " an "+ str;
		default: return " a "+ str;
}	}
function labelledNodeDesc( el ) {
    return el.parentNode.nodeName == "LABEL" ? articled( el.parentNode.innerText ):
           el.value.trim()        !=      "" ? articled( el.value ) :
                                               articled( "unknown" );
}
function innerTextOrValueDesc( el ) {
    return el.innerText.trim() != "" ? articled( el.innerText ):
           el.value.trim()     != "" ? articled( el.value    ):
                                       articled( "unknown"  );
}
function titleOrInnerTextDesc( el ) {
    return el.title.trim()     != "" ? articled( el.title )    :
           el.innerText.trim() != "" ? articled( el.innerText ):
                                       articled( "unknown" )   ;
}
function placeholderOrId( el ) {
    return el.placeholder.trim() != "" ? articled( el.placeholder ) :
           el.id.trim()          != "" ? articled( el. id         ) :
                                         articled( "unnamed" );
}
function describeThePage() {
	var response = "";
	var elements = document.getElementsByTagName( "*" );
	for (el of elements)
		if (el.tagName ==      "A")
			response += titleOrInnerTextDesc( el ) +" link, ";

		else if (el.tagName == "BUTTON")
			response += articled( el.innerText ) +" button, ";

		else if (el.tagName == "INPUT" && (el.type == "text" || el.type == "textarea"))
			response += placeholderOrId( el ) +" value, ";

		else if (el.tagName == "INPUT" && (el.type == "button"   ))
			response += innerTextOrValueDesc( el ) + " button, ";
                
		else if (el.tagName == "INPUT" && (el.type == "submit"   ))
			response += innerTextOrValueDesc( el ) + " submit button, ";
                
		else if (el.tagName == "INPUT" && (el.type == "radio"   ))
			response += labelledNodeDesc( el )+ " radio button, ";

		else if (el.tagName == "INPUT" && (el.type == "checkbox"   ))
			response += labelledNodeDesc( el )+ " check box, ";

    return felicity[ 1 ] +", on this page there is "+
                    (response == "" ? "nothing" : response);
}
function attrValue( element, attr ) {
    return element.hasAttribute( attr ) ? element.getAttribute( attr ).trim().toLowerCase() : "";
}
function labelValue( el ) {
    return el.parentNode.nodeName=="LABEL" ? el.parentNode.innerText : el.value;
}
function query ( cmd, imp ) { //is there [a|an].../do you have [a|an]...
	var response = [];
	var article = cmd[ 0 ]; cmd.shift();
	var type = cmd[ cmd.length -1 ];
	if (type == "checkbox" || type == "link" || type == "button" || type == "value") {
        if (type == "button" && cmd[ cmd.length -2 ] == "radio") {
            type = "radio";
            cmd.pop();
        }
        cmd.pop();
    } else
		type = "value";
	str = cmd.join(" ").toLowerCase();
	var elements = document.getElementsByTagName( "*" );
	for (el of elements) if (!hidden( el )) {
		if ((el.tagName == "A" && type == "link" &&
				(includesWord( el.title,     str ) ||
				 includesWord( el.innerText, str )   ))
         || ((el.tagName == "BUTTON" ||
             (el.tagName == "INPUT" && el.type == "button"))
             && type == "button" &&
				 (includesWord( el.innerText, str )))
         || (el.tagName == "INPUT" && (el.type == "text" || el.type == "textarea")
             && type == "value" &&
                 (includesWord(el.innerText, str )))
         || (el.tagName == "INPUT" && el.type == type    && includesWord( labelValue(el), str))
		)   
            return felicity[ 2 ] + " , "
					+ (imp ? "there is " : "I have ")
					+ article +" "+ str +" "+ (type == "radio"?"radio button":type);
        else if (includesWord( el.title,       str ) ||
				 includesWord( el.innerText,   str ) ||
                 includesWord( labelValue(el), str))
        {
            if ((el.tagName == "BUTTON" ||
                  (el.tagName == "INPUT" && el.type == "button")))
                response.push( article +" "+ str +" button" );
            else if (el.tagName == "A")
                response.push( article +" "+ str +" link" );
            else if (el.tagName == "INPUT" && el.type == "radio")
                response.push( article +" "+ str +" radio button" );
            else if (el.tagName == "INPUT" && el.type == "checkbox")
                response.push( article +" "+ str +" checkbox" );
    }   }
	return felicity[ 3 ] + " , " + (response.length > 0 ?
                (imp ? "but there is " : "but I have ") + response.join( " and " )
				:  (imp ? "there is not " : "I don't have ")
					+ article +" "+ str +" "+ type );
}
function howMany( cmd ) { // how many [level n headings|X] [are there [on this page]]
    if (cmd.length == 0) return felicity[ 0 ]+ ", "+ reply[ 0 ]+ ": how many";
    var name = cmd[ 0 ];
    var e, elems = null;
    var number = 0;
    var level = "*";
    if (name == "level") {
        if (cmd.length == 1) {
            return felicity[ 0 ]+ ", "+ reply[ 0 ]+ ": how many level";
        } else if (cmd.length == 2) {
            return felicity[ 0 ]+ ", "+ reply[ 0 ]+ ": how many level "+ cmd[ 1 ];
        } else { // ok, "level n headings"...
            level = toNumerics( cmd[ 1 ]);
            name = cmd[ 2 ];
    }   }
    switch (name) {
        case "headers"    :
        case "headings"   : elems = document.getElementsByTagName( "h" + level ); break;
        case "paragraphs" : elems = document.getElementsByTagName( "p" );      break;
        case "values"     : elems = document.getElementsByTagName( "input" );  break;
        case "checkboxes" : elems = document.getElementsByTagName( "input" );  break;
        case "radio"      : elems = document.getElementsByTagName( "input" );  break;
        case "buttons"    : elems = document.getElementsByTagName( "*" );      break;
        case "links"      : elems = document.getElementsByTagName( "a" );      break;
        case "figures"    : elems = document.getElementsByTagName( "figure" ); break;
        default           : return felicity[ 0 ]+ ", "+ reply[ 0 ]+ ": how many "+ cmd.join( " " );
    }
    if (elems != null) 
        switch (name) {
            case "values"  :
                for (e of elems)
                    if (e.type == "text" || e.type == "textarea")
                        if (!hidden( e ))
                            number++;
                break;
            case "buttons" :
                for (e of elems)
                    if ( e.tagName == "BUTTON" ||
                        (e.tagName == "INPUT" && e.type == "button"))
                        if (!hidden( e ))
                            number++;
                break;
            case "checkboxes" :
                for (e of elems)
                    if (e.tagName == "INPUT" && e.type == "checkbox")
                        if (!hidden( e ))
                            number++;
                break;
            case "radio" :
                for (e of elems)
                    if (e.tagName == "INPUT" && e.type == "radio")
                        if (!hidden( e ))
                            number++;
                break;
            default        :
                if ((name == "headings" || name == "headers") && level != "*")
                    name = "level "+ level+ " " + name;
                for (e of elems)
                    if (!hidden( e ) &&
                        ( e.innerText.trim() != "" ||
                         (e.title != null && e.title != "")))
                        number++;
                break;
        }
    return number == 0 ? 
        felicity[ 0 ]+ ", there are no "+ name
        : felicity[ 1 ]+ ", there are "+ number +" "+ name;
}
function upto7words( str ) {
    var sa = str.split( " " );
    var out = [];
    for (i=0; i<7; i++) {
        out.push( sa[ i ]);
        if (sa[ i ] == ".") break;
    }
    return out.join( " " );
}
function what( cmd ) { // [what|list] .. [buttons|links|values|figures|paragraphs] ..
    var widgets = [];
    var response = reply[ 0 ];
    var type = "", name;
            if (-1 != cmd.indexOf(    "radio" )) type =   "radio";
    else if (-1 != cmd.indexOf("checkboxes")) type ="checkbox";
    else if (-1 != cmd.indexOf(  "buttons" )) type =  "button";
    else if (-1 != cmd.indexOf(   "values" )) type =   "value";
    else if (-1 != cmd.indexOf(    "links" )) type =    "link";
    else if (-1 != cmd.indexOf(  "figures" )) type =  "figure";
    else if (-1 != cmd.indexOf("paragraphs")) type =   "paras";
    else if (-1 != cmd.indexOf( "headings" )) type =  "header";
    else if (-1 != cmd.indexOf(  "headers" )) type = "heading";
    else if (-1 != cmd.indexOf(    "title" )) type =   "title";
    else return felicity[ 0 ] +", "+ " i didn't hear a word like: buttons, values, links figures, paragraphs or headers.";
        
    var elems;
    switch (type) {
        case "title":
            var elem = document.getElementsByTagName( "title" );
            response = elem.length == 0 ?
                    reply[ 1 ]
                    : "the title of this page is: "+ elem[ 0 ].innerText;
            break;
        case "button":
            elems = document.getElementsByTagName( "*" );
            for (el of elems)
                if (el.tagName == "BUTTON") {
                    if (!hidden( el ))
                        widgets.push( articled( el.innerText.toLowerCase().trim()) +" "+type );
                } else if (el.tagName == "INPUT" && el.type == "button") {
                    if (!hidden( el ))
                        widgets.push( articled( el.title.toLowerCase().trim()) +" "+type );
                }
            response = widgets.length == 0 ?
                        "there are no buttons."
                        : "there is: " + widgets.join( " ; and, there is " );
            break;
        case "header":
        case "heading":
            // find level n
            var levelIndex = cmd.indexOf( "level" );
            var level = "1";
            if (levelIndex != -1 && levelIndex+1 < cmd.length-1)
                level = toNumerics( cmd[ levelIndex + 1 ]);
            elems = document.getElementsByTagName( "h"+ level );
            for (el of elems)
                if(!hidden( el ))
                    widgets.push( el.innerText );
            response = widgets.length == 0 ?
                    "there don't appear to be any level "+ level +" "+ type +"s"
                    : "this page includes the level "+level+" "+ type +": "
                                                + widgets.join( " , and the level "+level+" "+ type +" " );
            break;
        case "paras":
            elems = document.getElementsByTagName( "p" );
            for (el of elems)
                if (!hidden( el ))
                    widgets.push( upto7words( el.innerText ));
            response = widgets.length == 0 ?
                    "there don't appear to be any paragraphs"
                    : widgets.join( " , " );
            break;
        case "value":
            elems = document.getElementsByTagName( "input" );
            for (el of elems)
                if (el.type == "text" || el.type == "textarea")
                    if (!hidden( el ) && "" != (name = attrValue( el, "placeholder" )))
                        widgets.push( articled( name ) +" "+type );
            response = widgets.length == 0 ?
                        "there are no values."
                        : "there is: " + widgets.join( " ; and, there is " );
            break;
        case "checkboxes":
            elems = document.getElementsByTagName( "input" );
            for (el of elems)
                if (el.type == "checkboxes")
                    if (!hidden( el ) && "" != (name = labelValue( el )))
                        widgets.push( articled( name ) +" "+type );
            response = widgets.length == 0 ?
                        "there are no checkboxes."
                        : "there is a: " + widgets.join( " ; and, there is a " );
            break;
        case "radio":
            elems = document.getElementsByTagName( "input" );
            for (el of elems)
                if (el.type == "radio")
                    if (!hidden( el ) && "" != (name = labelValue( el )))
                        widgets.push( articled( name ) +" "+type+" button " );
            response = widgets.length == 0 ?
                        "there are no radio buttons."
                        : "there is a: " + widgets.join( " ; and, there is a " );
            break;
        case "figure":
            elems = document.getElementsByTagName( "figcaption" );
            for (el of elems)
                if (!hidden( el ))
                    widgets.push( articled( el.innerText.toLowerCase().trim()) );
            response = widgets.length == 0 ?
                        "there are no figures."
                        : "there is: " + widgets.join( " ; and, there is " );
            break;
        case "link":
            elems = document.getElementsByTagName( "a" );
            for (el of elems)
                if (!hidden( el )) {
                    name = el.hasAttribute( "title" ) ?
                            el.getAttribute( "title" ) :
                            el.innerText.toLowerCase().trim();
                    if (name != "")
                        widgets.push( articled( name ) +" "+type );
                }
            response = widgets.length == 0 ?
                            "there are no links."
                            : "there is: " + widgets.join( " ; and, there is " );
            break;
        default : response = "programming error: "+ type +" type is not supported.";
    }
	return felicity[ widgets.length==0 ? 0:1 ] +", "+ response;
}
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
function toNavigate( felicious ) {
    return (felicious ? felicity[ 1 ] +", ":"")+
            " To navigate, you can say: "+
            " go to website dot com; or, you can say, "+
            " click on x, or click on the x link."+
            " You can also say, scroll up or down.\n";
}
function toQuery( felicious ) {
    return  (felicious ? felicity[ 1 ] +", ":"")+
            "To query this page, you can say: "+
            "describe the page ; or, you can say, "+
            "what, or how many, buttons, links ,values, headings, or paragraphs are there; or, you can say, "+
            "is there an x button, or is there a y link.\n";
}
function toInteract( felicious ) {
    return (felicious ? felicity[ 1 ] +", ":"")+
            "To interact with this page, you can say: "+
            "click on x, or click on the x button; or, you can say, "+
            "set the value of x to y; or, you can say, "+
            "get the value of x.\n";
}
function help() {
    return toQuery( true ) + toNavigate( false ) + toInteract( false );
}
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
var halfpage = Math.floor( screen.availHeight / 2 );
function scroll( down ) {
    window.scrollBy( 0, down ? halfpage : -halfpage );
    return felicity[ 1 ] +", "+ "scrolled "+(down?"down":"up")+".";
}
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
const phonetics = [ "alpha",   "bravo",    "charlie", "delta",  "echo",   "foxtrot",
                    "hotel",   "golf",     "india",   "juliet", "kilo",   "lima",
                    "mike",    "november", "oscar",   "papa",   "quebec", "romeo", 
                    "sierra", "tango",     "uniform", "victor", "whiskey", "x-ray",
                    "yankie", "zulu" ];
function unspell( utt ) { // click on mail spelt mike alpha lima echo
    var out = [];
    for(i=0; i<utt.length; i++)
        if (i<utt.length-1 && utt[ i ] == "spelt") {
            if (utt[ 1+i ] == "spelt") // we've 'spelt spelt', treat as 'spelt'
                out.push( utt[ ++i ]);
            else {
                var chars = [];
                while (phonetics.includes( utt[ ++i ]))
                    chars.push( utt[ i ].charAt( 0 )); // push the first character of e.g. india.
                var word = chars.join("");
                if (word != "") {
                    out.pop(); // remove mis-spelt word...
                    out.push( word ); // ...forgetting 'spelt', push spelt word
                } else
                    out.push( "spelt" ); // spelt isn't pushed
                out.push( utt[ i ]); // both cases push the current (nonphonetic) word
            }
        } else
            out.push( utt[ i ]);
    return out;
}
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
// ****************************************************************************
function interp( utterance ) {
        
    var response = felicity[0] +", "+ reply[ 0 ] +": "+ utterance;
    if (utterance == null) return felicity[0] +", "+ "i didn't catch that";
    var cmds = utterance.split( "and then" );

    for (i=0; i<cmds.length; i++) {
        
        cmds[i]=cmds[i].trim().toLowerCase();
        cmd=cmds[i].split( " " );
        while (cmd.length > 0 && felicity.includes( cmd[ 0 ] )) 
            cmd.shift();

        cmd = unspell( cmd );
        
        // Basic Interpretation...
        if (cmd.length == 0)
            response = "ok";

        // Page interaction/navigation
        else if (cmd[ 0 ] == "click"
              && cmd[ 1 ] == "on")
            response = clickOn( shift( cmd, 2 ));
        else if (cmd[ 0 ] ==   "set"
              && cmd[ 1 ] ==   "the"
              && cmd[ 2 ] == "value"
              && cmd[ 3 ] ==    "of")
            response = setValueTo( shift( cmd, 4 ));
        else if (cmd[ 0 ] ==   "get" 
              && cmd[ 1 ] ==   "the"
              && cmd[ 2 ] == "value"
              && cmd[ 3 ] ==    "of")
            response = getValueOf( shift( cmd, 4 ));
        else if (cmd[ 0 ] ==   "type" 
              && cmd[ 1 ] ==   "in")
            response = typeIn( shift( cmd, 2 ));
        else if (cmd[ 0 ] == "go")
            response = go( shift( cmd, 1 ));
		else if (cmd[ 0 ] == "read")
        	response = read( shift( cmd, 1 ));

        // Interaction Help...
        else if (cmds[i] == "hello" ||
                 cmds[i] == "help"  ||
                 (cmd[1] == "what" && cmd.indexOf( "say" ) != -1))
            response = help();
        // ... how [should/do I/you] ...
        else if (cmd[ 0 ] ==  "how" &&
                 cmd.indexOf( "navigate" ) != -1)
            response = toNavigate( true );
        else if (cmd[ 0 ] ==  "how" &&
                 cmd.indexOf( "query"    ) != -1)
            response = toQuery( true );
        else if (cmd[ 0 ] ==  "how" &&
                 cmd.indexOf( "interact" ) != -1)
            response = toInteract( true );

        // Page Description
        else if (cmd[ 0 ] == "describe" &&
                 cmd.indexOf(    "page" ) != -1)
			response = describeThePage();
		else if (cmd[ 0 ] == "is" &&
                 cmd[ 1 ] == "there" &&
                (cmd[ 2 ] == "a" || cmd[ 2 ] == "an"))
			response = query( shift( cmd, 2 ), true );
		else if (cmd[ 0 ] == "do" &&
                 cmd[ 1 ] == "you" &&
                 cmd[ 2 ] == "have" &&
                (cmd[ 3 ] == "a" || cmd[ 3 ] == "an"))
            response = query( shift( cmd, 3 ), false );
		else if (cmd[ 0 ] == "does" &&
                 cmd[ 1 ] == "this" &&
                 cmd[ 2 ] == "page" &&
                 cmd[ 3 ] == "have" &&
                (cmd[ 4 ] == "a" || cmd[ 4 ] == "an"))
            response = query( shift( cmd, 4 ), false );
        else if (cmd[ 0 ] == "what" ||
                 cmd[ 0 ] == "list") // catch-all: values, links and buttons
            response = what( cmd );
        else if (cmd[ 0 ] == "how" &&
                 cmd[ 1 ] == "many" )
            response = howMany( shift( cmd, 2 ));

        // window interaction...
        else if (cmd[ 0 ] == "scroll" &&
                 cmd[ 1 ] == "up")
            response = scroll( false );
        else if (cmd[ 0 ] == "scroll" &&
                 cmd[ 1 ] == "down")
            response = scroll( true );

        // Configuring Interaction: nothing supported a yet!
		else if (cmds[i] == "keep listening")
			response = felicity[ 0 ] + ", " +"listening mode not supported yet";
		else if (cmds[i] == "stop listening")
			response = felicity[ 0 ] + ", " +"listening mode not supported yet";
		else if (cmds[i] == "verbose") {
            verbose = !verbose;
			response = felicity[ 1 ] + ", " +"verbosity is "+ (verbose ?"on":"off");

        // default error response:
        } else
            response = felicity[0] + ", "+ reply[ 0 ] +", "+ cmd.join( " " );;

        if (response.startsWith( felicity[ 0 ] + "," )) break;
    }
    return response;
}

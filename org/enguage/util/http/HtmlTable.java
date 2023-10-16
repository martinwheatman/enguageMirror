package org.enguage.util.http;

import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class HtmlTable {
	
	public  static final int      ID = 0; // "html"
	private static final String NAME = "HtmlTable";
	private static final Audit audit = new Audit( NAME );

	public HtmlTable( String fname) {
		HtmlStream ts = new HtmlStream( fname );
		attributes = doTBody( ts );
	}
	private Attributes attributes = new Attributes();
	public  Attributes attributes() {return attributes;}
	
	private static String doCell( HtmlStream hs, String type ) {
		// "<td>Princess Elizabeth<br/>21 April, 1926<br/>Mayfair, London</td>
		//		=> ["Princess Elizabeth", "", ""]
		Strings s = hs.getText();
		//audit.in( "doCell", "Cell content starts: "+ s );
		Html html = hs.getHtml();
		//audit.debug( "html="+ html.toString() );
		while ( !html.isEmpty() &&
				!(html.name().equalsIgnoreCase( type ) &&
				html.type()==Html.Type.end))
		{
			// don't put the html tag into the text
			// s.append( html.toString());
			// however, ...
			
			// deal with tables within <td/> - collapsible boxes
			if (html.name().equals( "table" ))
				doTBody( hs );
			
			if (html.name().equals( "br" ))
				s.append( ";" );

			// Consume non-displayable text
			if (html.attributes().contains( "style", "display:none" )) {
				int n = 1;
				while (n>0) {
					html = hs.getHtml(); // this consumes text :)
					if (html.type() == Html.Type.end)
						n--;
					else if (html.type() == Html.Type.begin)
						n++;
			}	}
			
			
//			if (html.name().equals( "sup" )) {
//				html = hs.getHtml();
//				while (!html.name().equals("sup") && html.type() != Html.Type.end)
//					html = hs.getHtml();
//			}
			
			// skip script and style tags
			if (!html.name().equals(   "script" ) &&
				!html.name().equals(    "style" )    )
			{
				Strings text = hs.getText();
				// need top get rid of [1] but nothing after "]"
				if (!text.toString().startsWith( "& # 91" )) { // '['
					//audit.debug( "Cell content continues: "+ text );
					s.appendAll( text );
			}	}
			
			html = hs.getHtml();
			//audit.debug( "html="+ html.toString());
		}
		//audit.out( s );
		return s.toString();
	}
	private static Attribute doTr( HtmlStream hs, boolean mergedTopRow ) {
		String name = "header"; // default name, if colspan=2
		String value = "";      // default value if no data
		
		Html html = hs.getHtml();
		//audit.in( "doTr", "html="+ html );
		while (!html.isEmpty()) {
			if (html.name().equalsIgnoreCase( "th" ))
				if (html.attributes().contains("colspan","2") || mergedTopRow)
					value = doCell( hs, "th" );
				else
					name = doCell( hs, "th" );
			
			else if (html.name().equalsIgnoreCase( "td" ))
				value = doCell( hs, "td" );
			
			else if (html.name().equalsIgnoreCase( "tr" ) 
					&& html.type() == Html.Type.end)
				break;
			
			mergedTopRow = false;
			html = hs.getHtml();
		}
		
		//audit.out( name +"=\""+ value +"\"" );
		return new Attribute( name, value );
	}
	private static Attributes doTBody( HtmlStream hs ) {
		Attributes attrs = new Attributes();
		Html html = hs.getHtml();
		//audit.in( "doTBody", "html="+ html );
		while (!html.isEmpty()) {
			
			if (html.name().equals("tr"))
				if (html.type() == Html.Type.end)
					break;
				else {
					Attribute attr = doTr( hs, html.attributes().contains( "class",  "mergedtoprow") );
					if (!attr.name().equals(""))
						attrs.add( attr );
				}
			
			html = hs.getHtml();
			//audit.debug( "/tbody? = "+ html.toString());
		}
		//audit.out( attrs.toString());
		return attrs;
	}
	public static Attributes doHtml( HtmlStream hs ) {
		Attributes attrs = new Attributes();
		Html html = hs.getHtml();
		audit.in( "doHtml", "html="+ html );
		while (!html.isEmpty()) {
			if (html.name().equals("table") &&
				html.attributes().contains("class", "infobox"))
				attrs = doTBody( hs );
			html = hs.getHtml();
			//audit.debug( "html="+ html.toString());
		}
		//Audit.log( tableCount +" tables found." );
		audit.out( attrs.toString());
		return attrs;
	}
	public static void main( String [] args) {
		//Audit.on();
		
		// create some test data
		String fname = "table.html";
				//"selftest/wiki/Queen_Elizabeth_The_Second.wikipedia";
		String table = // the following text is from, and probably copyright of, Wikipedia.
				//"junk in here<table class='infobox'><tbody>"
//				+ "<tr><th colspan='2'>Elizabeth II</th></tr>"
//				+ "<tr><th>Born</th><td>Princess <a ref='#'>Elizabeth</a> of York<br/><span display='none'>rubbish here</span>21 April, 1926<br/>Mayfair, London</td>"
//				+ "</tr></tbody></table>";
				"<style data-mw-deduplicate='TemplateStyles:r1066479718'>.mw-parser-output .infobox-subbox{padding:0;border:none;margin:-3px;width:auto;min-width:100%;font-size:100%;clear:none;float:none;background-color:transparent}.mw-parser-output .infobox-3cols-child{margin:auto}.mw-parser-output .infobox .navbar{font-size:100%}body.skin-minerva .mw-parser-output .infobox-header,body.skin-minerva .mw-parser-output .infobox-subheader,body.skin-minerva .mw-parser-output .infobox-above,body.skin-minerva .mw-parser-output .infobox-title,body.skin-minerva .mw-parser-output .infobox-image,body.skin-minerva .mw-parser-output .infobox-full-data,body.skin-minerva .mw-parser-output .infobox-below{text-align:center}</style>"
				+ "<table class='infobox vcard'>"
				+ "<tbody>"
				+ "<tr><th colspan='2' class='infobox-above fn' style='background-color: #cbe; font-size: 125%'>Elizabeth II</th></tr>"
				+ "<tr><td colspan='2' class='infobox-subheader'><i><a href='/wiki/Head_of_the_Commonwealth' title='Head of the Commonwealth'>Head of the Commonwealth</a></i></td></tr>"
				+ "<tr><td colspan='2' class='infobox-image photo'><span class='mw-default-size' typeof='mw:File/Frameless'><a href='/wiki/File:Queen_Elizabeth_II_official_portrait_for_1959_tour_(retouched)_(cropped)_(3-to-4_aspect_ratio).jpg' class='mw-file-description'><img alt='Elizabeth facing right in a half-length portrait photograph' src='//upload.wikimedia.org/wikipedia/commons/thumb/1/11/Queen_Elizabeth_II_official_portrait_for_1959_tour_%28retouched%29_%28cropped%29_%283-to-4_aspect_ratio%29.jpg/220px-Queen_Elizabeth_II_official_portrait_for_1959_tour_%28retouched%29_%28cropped%29_%283-to-4_aspect_ratio%29.jpg' decoding='async' width='220' height='293' class='mw-file-element' srcset='//upload.wikimedia.org/wikipedia/commons/thumb/1/11/Queen_Elizabeth_II_official_portrait_for_1959_tour_%28retouched%29_%28cropped%29_%283-to-4_aspect_ratio%29.jpg/330px-Queen_Elizabeth_II_official_portrait_for_1959_tour_%28retouched%29_%28cropped%29_%283-to-4_aspect_ratio%29.jpg 1.5x, //upload.wikimedia.org/wikipedia/commons/thumb/1/11/Queen_Elizabeth_II_official_portrait_for_1959_tour_%28retouched%29_%28cropped%29_%283-to-4_aspect_ratio%29.jpg/440px-Queen_Elizabeth_II_official_portrait_for_1959_tour_%28retouched%29_%28cropped%29_%283-to-4_aspect_ratio%29.jpg 2x' data-file-width='739' data-file-height='985' /></a></span><div class='infobox-caption' style='line-height:normal;padding-bottom:0.2em;padding-top:0.2em;'>Formal portrait, 1959</div></td></tr>"
				+ "<tr><th colspan='2' class='infobox-header' style='background-color: #e4dcf6;line-height:normal;padding:0.2em 0.2em'><a href='/wiki/Queen_of_the_United_Kingdom' class='mw-redirect' title='Queen of the United Kingdom'>Queen of the United Kingdom</a><br />and other <a href='/wiki/Commonwealth_realm' title='Commonwealth realm'>Commonwealth realms</a> <div style='display:inline;font-weight:normal' class='noprint'>(<a href='/wiki/List_of_sovereign_states_headed_by_Elizabeth_II' title='List of sovereign states headed by Elizabeth II'>list</a>) </div></th></tr>"
				+ "<tr><th scope='row' class='infobox-label'>Reign</th><td class='infobox-data'>6&#160;February 1952&#160;&#8211;&#32;<span class='avoidwrap' style='display:inline-block;'>8&#160;September 2022</span></td></tr>"
				+ "<tr><th scope='row' class='infobox-label'><a href='/wiki/Coronation_of_Elizabeth_II' title='Coronation of Elizabeth II'>Coronation</a></th><td class='infobox-data'>2&#160;June 1953</td></tr>"
				+ "<tr><th scope='row' class='infobox-label'>Predecessor</th><td class='infobox-data'><a href='/wiki/George_VI' title='George VI'>George&#160;VI</a></td></tr>"
				+ "<tr><th scope='row' class='infobox-label'>Successor</th><td class='infobox-data'><a href='/wiki/Charles_III' title='Charles III'>Charles&#160;III</a></td></tr>"
				+ "<tr><th colspan='2' class='infobox-header' style='background-color: #e4dcf6;line-height:normal;padding:0.2em 0.2em'><div style='height: 4px; width:100%;'></div></th></tr>"
				+ "<tr><th scope='row' class='infobox-label'>Born</th><td class='infobox-data'>Princess Elizabeth of York<br /><span style='display:none'>(<span class='bday'>1926-04-21</span>)</span>21 April 1926<br /><a href='/wiki/Mayfair' title='Mayfair'>Mayfair</a>, London, England</td></tr>"
				+ "<tr><th scope='row' class='infobox-label'>Died</th><td class='infobox-data'>8 September 2022<span style='display:none'>(2022-09-08)</span> (aged&#160;96)<br /><a href='/wiki/Balmoral_Castle' title='Balmoral Castle'>Balmoral Castle</a>, Aberdeenshire, Scotland</td></tr>"
				+ "<tr><th scope='row' class='infobox-label'>Burial</th><td class='infobox-data'>19&#160;September 2022<br /><div style='display:inline' class='label'><a href='/wiki/King_George_VI_Memorial_Chapel' title='King George VI Memorial Chapel'>King George VI Memorial Chapel</a>, St&#160;George's Chapel, Windsor&#160;Castle</div></td></tr>"
				+ "<tr><th scope='row' class='infobox-label'>Spouse</th><td class='infobox-data'><style data-mw-deduplicate='TemplateStyles:r1151524712'>.mw-parser-output .marriage-line-margin2px{line-height:0;margin-bottom:-2px}.mw-parser-output .marriage-line-margin3px{line-height:0;margin-bottom:-3px}.mw-parser-output .marriage-display-ws{display:inline;white-space:nowrap}</style>"
				+ "<div class='marriage-display-ws'>"
				+ "<div style='display:inline-block;line-height:normal;margin-top:1px;white-space:normal;'>"
				+ "<a href='/wiki/Prince_Philip,_Duke_of_Edinburgh' title='Prince Philip, Duke of Edinburgh'>Prince Philip, Duke of Edinburgh</a></div>"
				+ "   <div class='marriage-line-margin2px'>&#8203;</div>&#32;<div style='display:inline-block;margin-bottom:1px;'>&#8203;</div>&#40;<abbr title='married'>m.</abbr>&#160;<style data-mw-deduplicate='TemplateStyles:r1038841319'>.mw-parser-output .tooltip-dotted{border-bottom:1px dotted;cursor:help}</style><span class='rt-commentedText tooltip' title='20 November 1947'>1947</span>&#59;&#32;died&#160;<link rel='mw-deduplicated-inline-style' href='mw-data:TemplateStyles:r1038841319'><span class='rt-commentedText tooltip' title='9 April 2021'>2021</span>&#41;<wbr />&#8203;</div></td></tr>"
				+ "<tr><th scope='row' class='infobox-label'><a href='/wiki/Issue_(genealogy)' title='Issue (genealogy)'>Issue</a><br /><span style='font-weight:normal'><i><a href='#Issue'>Detail</a></i></span></th><td class='infobox-data'><style data-mw-deduplicate='TemplateStyles:r1126788409'>.mw-parser-output .plainlist ol,.mw-parser-output .plainlist ul{line-height:inherit;list-style:none;margin:0;padding:0}.mw-parser-output .plainlist ol li,.mw-parser-output .plainlist ul li{margin-bottom:0}</style><div class='plainlist'>"
				+ "<ul><li><a href='/wiki/Charles_III' title='Charles III'>Charles&#160;III</a></li>";
		Fs.stringToFile( fname, table );
		

		// print out that data
		HtmlStream hs = new HtmlStream( fname );
		//Audit.title( fname +":" );
		for (Attribute a : doHtml( hs ))
			for (String value : a.value().split( ";" ))
				Audit.log( a.name() +"="+ value );
		
		//tidy up
		Fs.destroy( fname );
}	}

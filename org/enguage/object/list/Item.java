package org.enguage.object.list;

import java.util.ListIterator;

import org.enguage.util.Attribute;
import org.enguage.util.Attributes;
import org.enguage.util.Audit;
import org.enguage.util.Number;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.vehicle.Plural;
import org.enguage.vehicle.when.Moment;
import org.enguage.vehicle.when.When;
import org.enguage.vehicle.where.Where;

public class Item {

	static private      Audit audit = new Audit("Item" );
	static public final String NAME = "item";
	
	static private Strings format = new Strings(); // e.g. "cake slice", "2 cake slices" or "2 slices of cake"
	static public  void    format( String csv ) { format = new Strings( csv, ',' ); }
	static public  Strings format() { return format; }

	public Item() { name( "item" ); }
	public Item( Strings ss ) { // [ "black", "coffee", "quantity='1'", "unit='cup'" ]
		this();
		Attributes  a = new Attributes();
		Strings descr = new Strings();
		
		for (String s : ss)
			if (Attribute.isAttribute( s ))
				a.add( new Attribute( s ));
			else if (!s.equals("-"))
				descr.add( s );

		description(  descr );
		attributes( a );
	}
	public Item( Strings ss, Attributes as ) {
		// [ "black", "coffee", "quantity='1'"], [unit='cup']
		this( ss );
		attributes().addAll( as );
	}
	public Item( String s ) {
		// "black coffee quantity='1' unit='cup'
		this( new Strings( s ).contract( "=" ));
	}
	public Item( Item item ) { // copy c'tor
		this();
		description( new Strings( item.description() ));
		attributes( new Attributes( item.attributes() ));
	}
	
	// members to implement tag member: name, desc, attr
	private String  name = new String();
	public  String  name() {return name; }
	public  Item    name( String s ) { name=s; return this; }
	
	private Strings descr = new Strings();
	public  Strings description() { return descr;}
	public  Item    description( Strings s ) { descr=s; return this;}
	
	private Attributes attrs = new Attributes();
	public  Attributes attributes() { return attrs; }
	public  Item       attributes( Attributes a ) { attrs=a; return this; }
	public  String     attribute( String name ) { return attrs.get( name ); }
	public  void       replace( String name, String val ) { attrs.replace( name, val );}
	
	// -- list helpers
	public long when() {
		long it = -1;
		try {
			it = Long.valueOf( attribute( "WHEN" ));
		} catch (Exception e){}
		return it;
	}
	public int quantity() {
		int quant = 1;
		try {
			quant = Integer.parseInt( attribute( "quantity" ));
		} catch(Exception e) {} // fail silently
		return quant;
	}
	public boolean equals( Item patt ) { // like Tag.equals()
		return Plural.singular( descr ).equals( Plural.singular( patt.description() ))
				&& attrs.matches( patt.attributes());
	}
	public boolean equalsDescription( Item patt ) { // like Tag.equalsContent()
		return Plural.singular( descr ).equals( Plural.singular( patt.description() ));
	}
	public boolean matches( Item patt ) {
		return Plural.singular( descr ).contains( Plural.singular( patt.description() )) &&
				attributes().matches( patt.attributes());
	}
	public boolean matchesDescription( Item patt ){
		return Plural.singular( descr ).contains( Plural.singular( patt.description() ));
	}
	// -----------------------------------------
	public void updateItemAttributes( Item it ) {
		// update quantity, then replace/add others/all?
		for (Attribute a : attrs) {
			String value = a.value(),
					name = a.name();
			if (name.equals( "quantity" )) {
				/*
				 * should getNumber() and combine number from value.
				 */
				Strings vs = new Strings( value );
				if (vs.size() == 2) {
					/*
					 * combine magnitude - replace if both absolute, add if relative etc.
					 * Should be in Number? Should deal with "1 more" + "2 more" = "3 more"
					 */
					String firstVal = vs.get( 0 ), secondVal = vs.get( 1 );
					if (secondVal.equals( Number.MORE ) || secondVal.equals( Number.FEWER )) {
						int oldInt = 0, newInt = 0;
						try {
							oldInt = Integer.valueOf( it.attribute( name ));
						} catch (Exception e) {} // fail silently, oldInt = 0
						try {
							newInt = Integer.valueOf( firstVal );
							value = Integer.toString( oldInt + (secondVal.equals( Number.MORE )
									? newInt : -newInt));
							
						} catch (Exception e) {}
			}	}	}
			it.replace( name, value );
	}	}
	public String counted( Float num, String val ) {
		// N.B. val may be "wrong", e.g. num=1 and val="coffees"
		if (val.equals("1")) return "a";
		return Plural.ise( num, val );
	}
	public String toXml() { return "<"+name +attrs+">"+descr+"</"+name+">";}
	public String toString() {
		Strings rc = new Strings();
		Strings formatting = format();
		if (formatting == null || formatting.size() == 0)
			rc.append( descr );
		else {
			Float prevNum = Float.NaN;    // pluralise to the last number... NaN means no number found yet
			/* Read through the format string:
			 *    add attributes (uppercase loaded),
			 * OR plain text (if lower case),
			 * OR the content if blank string.
			 */
			for (String format : formatting)
				if (format.equals("")) { // main item: "black coffee"
					if (description().size()>0)
						rc.add( Plural.ise( prevNum, description().toString() ));
				} else { // formatted attributes: "UNIT of" + unit='cup' => "cups of"
					Strings subrc = new Strings();
					boolean found = true;
					for (String component : new Strings( format ))
						if ( Strings.isUpperCase( component )) { // UNIT
							if (attributes().hasIgnoreCase( component )) {
								String val = attributes().getIgnoreCase( component );
								if (component.equals("WHEN"))
									subrc.add( new When( new Moment( Long.valueOf( val ))).toString() );
								else if (component.equals("LOCATION") || component.equals("LOCATOR"))
									subrc.add( val ); // don't count these!
								else { // 3 cupS -- pertains to unit/quantity only?
									subrc.add( counted( prevNum, val ) );  // UNIT='cup(S)'
									ListIterator<String> si = new Strings( val ).listIterator();
									prevNum = Number.getNumber( si ).magnitude(); //Integer.valueOf( val );
									if (prevNum.isNaN()) prevNum = 1.0f; 
								}
							} else
								found = false;
						} else
							subrc.add( component ); // ...of...
					
					if (found) rc.addAll( subrc );
				}
		}
		return rc.toString( Strings.SPACED );
	}
	static public String interpret( Strings cmd ) {
		String rc = Shell.FAIL;
		if (cmd.size() > 2
				&& cmd.get( 0 ).equals( "set" )
				&& cmd.get( 1 ).equals( "format" ))
		{
			Item.format( Strings.stripQuotes( cmd.get( 2 )));
			rc = Shell.SUCCESS;
		}
		return rc;
	}
	//
	// --- test code ---
	//
	private static void test( String s ) {
		audit.debug( ">>>>>>>"+ s +"<<<<" );
		Item t1 = new Item( new Strings( s ).contract( "=" ));
		//audit.debug( ">>t1 is "+ t1.toXml() );
		audit.debug( "is ===> "+ t1.toString() );
	}
	public static void main( String args[] ) {
		Audit.allOn();
		Audit.traceAll( true );
		Item.format( "QUANTITY,UNIT of,,from FROM,WHEN,"+ Where.LOCATOR +" "+ Where.LOCATION );
		audit.debug("Item.toString(): using format:"+ format() );
		test( "black coffees quantity=1 unit='cup' from='Tesco' locator='in' location='London'" );
		test( "black coffees quantity='2' unit='cup'" );
		test( "black coffees quantity='1'" );
		test( "black coffee quantity='2'" );
		test( "black coffees quantity='5 more' when='20151125074225'" );
}	}

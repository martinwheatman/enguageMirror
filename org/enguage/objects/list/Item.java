package org.enguage.objects.list;

import java.util.ArrayList;
import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.Plural;
import org.enguage.vehicle.number.Number;
import org.enguage.vehicle.when.Moment;
import org.enguage.vehicle.when.When;
import org.enguage.vehicle.where.Where;

public class Item {

	static private      Audit audit = new Audit("Item" );
	static public final String NAME = "item";
	static public final int      id = 171847; //Strings.hash( NAME );
	
	static private Strings format = new Strings(); // e.g. "QUANTITY,UNIT of,,LOCATOR LOCATION"
	static public  void    format( String csv ) { format = new Strings( csv, ',' ); }
	static public  Strings format() { return format; }

	static private Strings groupOn = new Strings();
	static public  void    groupOn( String groups ) { groupOn = new Strings( groups );}
	static public  Strings groupOn() { return groupOn; }
	
	// members: name, desc, attr
	private String  name = new String( NAME );
	public  String  name() { return name; }
	public  Item    name( String s ) { name=s; return this; }
	
	private Strings descr = new Strings();
	public  Strings description() { return descr;}
	public  Item    description( Strings s ) { descr=s; return this;}
	
	static private ArrayList<Strings> isStuff = new ArrayList<Strings>();
	static private boolean isStuff( Strings s ) {return isStuff.contains( Plural.singular( s ));}
	static private void    stuffIs( Strings s ) {
		areThings.remove( Plural.plural( s ));
		isStuff.add( Plural.singular( s ));
	}
	
	static private ArrayList<Strings> areThings = new ArrayList<Strings>();
	static private boolean areThings( Strings s ) {return areThings.contains( Plural.plural( s ));}
	static private void    thingsAre( Strings s ) {
		isStuff.remove( Plural.singular( s ));
		areThings.add( Plural.plural( s ));
	}
	
	private Attributes attrs = new Attributes();
	public  Attributes attributes() { return attrs; }
	public  Item       attributes( Attributes a ) { attrs=a; return this; }
	public  String     attribute( String name ) { return attrs.get( name ); }
	public  void       replace( String name, String val ) { attrs.replace( name, val );}
	
	public Item() {};
	public Item( Item item ) { // copy c'tor
		name( item.name() );
		description( new Strings( item.description() ));
		attributes( new Attributes( item.attributes() ));
	}
	public Item( Strings ss ) { // [ "black", "coffee", "quantity='1'", "unit='cup'" ]
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
	static public Item next( ListIterator<String> si ) {
		Item it = null;
		if (si.hasNext() && si.next().equals( "<" ) &&
			si.hasNext() && si.next().equals( "item" )) // will be "/" on end list
		{
			it = new Item();
			it.attributes( Attributes.next( si ));
			si.next(); // consume ">"
			it.description( Strings.copyUntil( si, "<" ));
			if (!si.hasNext() || !si.next().equals(    "/" ) &&
				!si.hasNext() || !si.next().equals( "item" ) &&
				!si.hasNext() || !si.next().equals(    ">" )   )
			{
				audit.ERROR( "Missing end-item tag");
		}	}
		return it;
	}
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
	public boolean equalsDescription( Item patt ) { // like Tag.equalsContent()
		return Plural.singular( descr ).equals( Plural.singular( patt.description() ));
	}
	public boolean matchesDescription( Item patt ){
		return Plural.singular( descr ).contains( Plural.singular( patt.description() ));
	}
	public boolean matchesAttributes( Item patt ){
		return attributes().matches( patt.attributes());
	}
	public boolean equals( Item patt ) {
		return equalsDescription( patt )
				&& matchesAttributes( patt );
	}
	public boolean matches( Item patt ) {
		return matchesDescription( patt )
				&& matchesAttributes( patt );
	}
	// -----------------------------------------
	
	public void updateAttributes( Attributes newValues ) {
		// replace values with newValues, combining quantity
		audit.in( "updateAttributes", newValues.toString() );
		for (Attribute nval : newValues) {
			String value = nval.value(),
					name = nval.name();
			if (name.equals( "quantity" )) {
				Number n = new Number( value ),
				       m = new Number( attribute( "quantity" ));
				value = m.combine( n ).toString();
			}
			replace( name, value );
		}
		audit.out();
	}
	public Item removeQuantity( Number removed ) {
		audit.in( "removeQuantity", removed.toString());
		Number quantity = new Number( attribute( "quantity" ));
		removed.magnitude( -removed.magnitude()).isRelative( true );
		replace( "quantity", quantity.combine( removed ).toString() );
		return (Item) audit.out( this );
	}
	
	// pluralise to the last number... e.g. n cups(s); NaN means no number found yet
	private Float  prevNum = Float.NaN;
	private boolean united = false;
	private String counted( Float num, String val ) {
		// N.B. val may be "wrong", e.g. num=1 and val="coffees"
		if (val.equals("1")) return "a"; // English-ism!!!
		return Plural.ise( num, val );
	}
	private final Strings unitary = new Strings( "1" );
	private String counted( Float num, Strings val ) {
		// N.B. val may be "wrong", e.g. num=1 and val="coffees"
		if (val.equals( unitary )) return "a"; // English-ism!!!
		return Plural.ise( num, val );
	}
	private Float getPrevNum( String val ) {
		ListIterator<String> si = new Strings( val ).listIterator();
		Float prevNum = new Number( si ).magnitude(); //Integer.valueOf( val );
		return prevNum.isNaN() ? 1.0f : prevNum;
	}
	private Strings getFormatComponentValue( String composite ) { // e.g. "from LOCATION"
		Strings value = new Strings();
		for (String cmp : new Strings( composite ))
			if ( Strings.isUpperCase( cmp )) { // variable e.g. "UNIT"
				if (groupOn().contains( cmp )) { // ["LOC"].contains( "LOC" )
					value=null; // IGNORE this component, and finish
					audit.debug( "toString(): ignoring:"+ cmp );
					break;
				} else { // cmp = "UNIT"
					String val = attributes().get( cmp );
					if (val.equals( "" )) {
						value=null; // this component is undefined, IGNORE
						break;
					} else if (cmp.equals(When.ID)) {
						When w = Moment.valid( val ) ?
								new When( new Moment( Long.valueOf( val ))) : // e.g. 2020012888888
								When.getWhen( new When(), new Strings( val )); //e.g. 'yesterday'
						value.add( w.toString() );
					} else if (cmp.equals(Where.LOCTN)
							|| cmp.equals(Where.LOCTR)) {
						value.add( val ); // don't count these!
					} else if (cmp.equals("QUANTITY")) {
						value.add( counted( prevNum, val ));
						prevNum = getPrevNum( val );
					} else if (cmp.equals("UNIT")) { // UNIT='cup(S)'
						value.add( counted( prevNum, val ));
						prevNum = Float.NaN;
						united = true;
					} else { // something else?
						value.add( val );  
				}	}
			} else // lower case -- constant
				value.add( cmp ); // e.g. "of"
		return value;
	}
	public String toXml() { return "<"+name+attrs+">"+descr+"</"+name+">";}
	public String toString() {
		Strings rc = new Strings();
		if (format.size() == 0)
			rc.appendAll( descr );
		else {
			united = false;
			/* Read through the format string: ",from LOCATION"
			 * ADDING attributes: u.c. VARIABLES OR l.c. CONSTANTS),
			 * OR the description if blank string.
			 */
			for (String f : format) // e.g. f="from LOCATION"
				if (f.equals("")) { // main item: "black coffee", "crisps"
					if (united)
						if (isStuff( descr )) // coffee
							rc.appendAll( Plural.singular( descr ));
						else if ( areThings( descr )) // biscuits
							rc.appendAll( Plural.plural( descr ));
						else
							rc.appendAll( descr ); // get it wrong and hope user says no, X is stuff
						
					else
						rc.append( counted( prevNum, descr )); // 2 beers
					prevNum = Float.NaN;
				} else { // component, f, e.g. "UNIT of"
					Strings subrc = getFormatComponentValue( f );
					if (null != subrc) // ignore group name, and undefs
						rc.addAll( subrc );
				}
		}
		return rc.toString();
	}
	private Strings getFormatGroupValue( String f ) {
		boolean found = false;
		Strings value = new Strings();
		for (String cmp : new Strings( f ))
			if ( Strings.isUpperCase( cmp )) { // variable e.g. UNIT
				String val = attributes().get( cmp );
				if (val.equals( "" )) {
					found = false;
					break;
				}
				value.add( val );
				if (groupOn().contains( cmp ))  // ["LOC"].contains( "LOC" )
					found = true;
			} else // lower case -- constant
				value.add( cmp ); // ...of...
		return found ? value : null;
	}
	public String group() { // like toString() but returning group value
		Strings rc = new Strings();
		if (format.size() == 0)
			rc.append( "" );
		else
			/* Read through the format string: ",from LOCATION"
			 * ADDING attributes: u.c. VARIABLES OR l.c. CONSTANTS),
			 * OR the description if blank string.
			 */
			for (String f : format) // e.g. f="from LOCATION"
				// main item: "black coffee" IGNORE
				if (!f.equals("")) { // attributes: "UNIT of" + unit='cup' => "cups of"
					Strings value = getFormatGroupValue( f );
					if (null != value) // ignore group name, and undefs
						rc.addAll( value );
				}
		return rc.toString( Strings.SPACED );
	}
	// ------------------------------------------------------------------------
	static public Strings interpret( Strings cmd ) {
		String rc = Shell.FAIL;
		if (cmd.size() > 2) {
			rc = Shell.SUCCESS;
			
			if (cmd.get( 0 ).equals( "set" )
			 && cmd.get( 1 ).equals( "format" ))
			
				format( Strings.stripQuotes( cmd.get( 2 )));
				
			else if (cmd.get( 0 ).equals( "things" )
			      && cmd.get( 1 ).equals( "include" ))
				
				thingsAre( new Strings( Strings.stripQuotes( cmd.get( 2 ))));
				
			else if (cmd.get( 0 ).equals( "stuff" )
			      && cmd.get( 1 ).equals( "includes" ))
			
				stuffIs( new Strings( Strings.stripQuotes( cmd.get( 2 ))));
				
			else
				rc = Shell.FAIL;
		}
		return new Strings( rc );
	}
	//
	// --- test code ---
	//
	private static Groups groups = new Groups();
	private static void testAdd( String descr ) {
		Item item = new Item( new Strings( descr ).contract( "=" ));
		groups.add( item.group(), item.toString());
	}
	private static void test( String s ) { test( s, null );}
	private static void test( String descr, String expected ) {
		Audit.log( ">>>>>>>"+ descr +"<<<<" );
		Item   item  = new Item( new Strings( descr ).contract( "=" ));
		String group = item.group(),
		       ans   = item.toString() + (group.equals("") ? "" : " "+ group);
		if (expected != null && !expected.equals( ans ))
			audit.FATAL( "the item: '" + ans +"'\n  is not the expected: '"+ expected +"'");
		else if (expected == null)
			audit.passed( " is ===> "+ ans );
		else
			audit.passed( " PASSED: "+ ans );
	}
	public static void main( String args[] ) {
		//Audit.allOn();
		//Audit.traceAll( true );
		Item.format( "QUANTITY,UNIT of,,from FROM,WHEN,"+ Where.LOCTR +" "+ Where.LOCTN );
		test( "black coffees quantity=1 unit='cup' from='Tesco' locator='in' location='London'",
				"a cup of black coffees from Tesco in London" );
		Audit.log( "adding b/c: "+ interpret( new Strings( "stuff includes 'black coffee'" )));
		test( "black coffees quantity=1 unit='cup' from='Tesco' locator='in' location='London'",
				"a cup of black coffee from Tesco in London" );
		
		test( "black coffee quantity='2' unit='cup'", "2 cups of black coffee" );
		test( "black coffee quantity='1'", "a black coffee" );
		test( "black coffee quantity='2'", "2 black coffees" );
		test( "black coffee quantity='5 more' when='20151125074225'" );

		Item.format( "QUANTITY,UNIT of,,from FROM,WHEN,"+ Where.LOCTR +" "+ Where.LOCTN );
		test( "crisp quantity=2 unit='packet' from='Tesco' locator='in' location='London'",
				"2 packets of crisp from Tesco in London" );
		Audit.log( "adding crisps: "+ interpret( new Strings( "things include crisps" )));
		test( "crisp quantity=2 unit='packet' from='Tesco' locator='in' location='London'",
				"2 packets of crisps from Tesco in London" );
		

		audit.title( "grouping" );
		Item.groupOn( Where.LOCTN );
		audit.debug("Using format:"+ Item.format.toString( Strings.CSV )
		          + (Item.groupOn().size() == 0
		        		  ? ", not grouped."
		                  : ", grouping on:"+ Item.groupOn() +"." ));

		test( "black coffee quantity=1 unit='cup' from='Tesco' locator='in' location='London'",
				"a cup of black coffee from Tesco in London" );
		test( "milk unit='pint' quantity=2 locator='from' location='the dairy aisle'",
				"2 pints of milk from the dairy aisle" );
		test( "crisps unit='packets' quantity=2 locator='from' location='the dairy aisle'",
				"2 packets of crisps from the dairy aisle" );
		//
		testAdd( "unit='cup' quantity='1' locator='from' location='Sainsburys' coffee" ); // subrc?
		testAdd( "quantity='1' locator='from' location='Sainsburys' biscuit" );
		testAdd( "locator='from' unit='pint' quantity='1' location='the dairy aisle' milk" );
		testAdd( "locator='from' location='the dairy aisle' cheese" );
		testAdd( "locator='from' location='the dairy aisle' eggs  quantity='6'" );
		testAdd( "toothpaste" );
		
		audit.title( "Groups" );
		Audit.log( groups.toString() +"." );
		
		audit.PASSED();
}	}

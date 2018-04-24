package org.enguage.object.list;

import java.util.Locale;

import org.enguage.object.Value;
import org.enguage.util.Attribute;
import org.enguage.util.Attributes;
import org.enguage.util.Audit;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.util.Tag;
import org.enguage.util.Tags;
import org.enguage.vehicle.Reply;
import org.enguage.vehicle.where.Where;

public class List {
	private static       Audit        audit = new Audit( "List" );
	public  static final String        NAME = "list";
	
	Value value; // instead of extending this class...
	public void ignore() {value.ignore();}
	public void restore() {value.restore();}
	
	// constructors
	public List( String e, String a ) {
		value = new Value( e, a ); //super( e, a );
		list = new Tag( value.getAsString() ).name( "list" );
	}
	
	// member - List manages a tag which represents 
	private Tag  list = new Tag();
	public  Tags listTags() { return list.content(); }
	public  Attributes attributes() { return list.attributes(); }
	
	private int position( Item item, boolean exact ) { // e.g. ["cake slices","2"]
		audit.in( "find", "lookingFor="+ item.toXml() +" f/p="+ (exact ? "FULL":"partial"));
		
		String ilocr = item.attribute( Where.LOCATOR );
		String ilocn = item.attribute( Where.LOCATION );
		
		long it = -1; // item time
		try {
			it = Long.valueOf( item.attribute( "WHEN" ));
		} catch (Exception e){}
		
		int pos = -1;
		for (Tag t : listTags()) {
			pos++;
			String tlocr = t.attribute( Where.LOCATOR );
			String tlocn = t.attribute( Where.LOCATION );
			long tt = -1; //tag time
			try {
				tt = Long.valueOf( t.attribute( "WHEN" ));
			} catch (Exception e) {}
			if ( (it == -1 || it == tt) // if tt == -1 && it != -i fail!
				&& (!exact || (
					   (ilocr.equals( "" ) || ilocr.equals( tlocr ))
					&& (ilocn.equals( "" ) || ilocn.equals( tlocn )))
				)
				&&     (( exact && t.equals(  item.tag() ))
			         || (!exact && t.matchesContent( item.tag() )))
			 )
				return audit.out( pos );
		}
		return audit.out( -1 );
	}
	private String quantity( Item item, boolean exact ) { // e.g. ["cake slices","2"]
		//audit.in( "quantity", "Item="+item.toString() + ", exact="+ (exact?"T":"F"));
		int count = 0;
		for (Tag t : listTags() ) // go though the file
			if (  ( exact && t.equals( item.tag() ))
				||(!exact && t.matchesContent( item.tag() )))
			{	int quant = 1;
				try {
					quant = Integer.parseInt( t.attribute( "quantity" ));
				} catch(Exception e) {} // fail silently
				count += quant;
			}
		//audit.out( count );
		return Integer.valueOf( count ).toString();
	}
	public  Strings get() { return get( null ); }
	private Strings get( Item item ) { // to Items class!
		audit.in( "get", "item="+ (item==null?"ALL":item.toString()));
		Strings rc = new Strings();
		for (Tag t : listTags())
			if (item == null || t.matches( item.tag()))
				rc.add( new Item( t ).toString());
		return audit.out( rc );
	}
	private String append( Item item ) { // adjusts attributes, e.g. quantity
		String rc = item.toString(); // return what we've just said
		audit.in( "add", "item created is:"+ item.toXml() +", but rc="+ rc);
		int n = position( item, false ); // exact match? No!
		if (-1 == n) { 
			if (!item.attribute( "quantity" ).equals( "0" )) {
				// in case: quantity='+= 37' => set it to '37'
				Strings quantity = new Strings( item.attribute( "quantity" ));
				if (quantity.size()==2) { // += n / -= n => n
					item.replace( "quantity", quantity.get( 1 ));
				}
				list.content( item.tag() );
			}
		} else { // found so update item...
			Tag removedItemTag = list.removeContent( n );
			item.updateTagAttributes( removedItemTag );
			String quantity = removedItemTag.attribute( "quantity" );
			if (quantity.equals( "" ) || Integer.valueOf( quantity ) != 0)
				list.content( n, removedItemTag );
		}
		value.set( list.toXml() );
		return audit.out( rc );
	}
	private String update( Item item ) { // adjusts attributes, e.g. quantity
		String rc = item.toString(); // return what we've just said
		int n = position( item, false ); // exact match? No!
		if (-1 != n) { 
			Tag removedItemTag = list.removeContent( n );
			item.updateTagAttributes( removedItemTag );
			String quantity = removedItemTag.attribute( "quantity" );
			if (quantity.equals( "" ) || Integer.valueOf( quantity ) != 0)
				list.content( n, removedItemTag );
			value.set( list.toXml() );
		}
		return audit.out( rc );
	}
	private String removeAttribute( Item item, String name ) { // adjusts attributes, e.g. quantity
		String rc = item.toString(); // return what we've just said
		audit.in( "removeAttribute", "item:"+ item.toXml() +", name="+ name );
		int n = position( item, false ); // exact match? No!
		if (-1 != n) {
			Tag tmp = list.removeContent( n );
			tmp.attributes().remove( name );
			list.content( n, tmp );
			item.tag( tmp );
			value.set( list.toXml() ); // was set( lines );
		} else
			audit.ERROR("not found "+ item.toXml());
		return audit.out( rc );
	}
	private Strings attributeValue( Item item, String name ) {
		Strings   rc = new Strings();
		String upper = name.toUpperCase( Locale.getDefault() );
		audit.in( "attributeValue", "item='"+ item.toXml() +"', name="+ upper );
		for (Tag t : list.content())  {
			if (item == null || t.matchesContent( item.tag())) {
				if (t.attributes().has( upper )) {
					rc.add( t.attributes().get( upper ));
		}	}	}
		return audit.out( rc );
	}
	private Strings namedValues( String name, String value ) {
		Strings rc = new Strings();
		audit.in( "namedValues", "attribute="+ name +", name="+ value );
		for (Tag t : list.content()) {
			if (t.attributes().has( name ) && t.attributes().get( name ).equals( value )) {
				rc.add( t.content().toString());
		}	}
		if (rc.size()==0) rc.add( Shell.FAIL);
		return audit.out( rc );
	}
	/* this needs to include adjusting quantity downwards, as above in add()
	 * i need coffee + I have 3 coffees = ???
	 * Returns number removed: existing - to be removed.
	 */
	private String remove( Item item, boolean exact ) {
		/* removes an item or quantity of item
		 * returns fail, or a narrative on whatever is remaining.
		 */
		String rc = Shell.FAIL;
		audit.in("remove", "item="+ item.toXml() +", exact="+ (exact?"T":"F"));
		
		/* Here if we have quantity, subtract quantity...
		 * NB this code does not cover the instances where:
		 * I need coffee + i have 37 coffees - ok
		 * i need 37 coffees + i have coffee
		 */
		int removed = 0, n;
		if (-1 != (n = position( item, exact ))) {
			/*
			 * we have a listed item, remove the item in question...
			 */
			Tag tmp = list.removeContent( n );
			
			if (item.attributes().has( "quantity" )
			  && tmp.attributes().has( "quantity" ))
			{ // we have two quantity
				int existing = Integer.valueOf(  tmp.attribute( "quantity" ));
				     removed = Integer.valueOf( item.attribute( "quantity" ));
				if (removed >= existing) {
					removed = existing; // back to zero
					tmp = null;
				} else { //if (existing > removed) {
					// still some left over
					int remaining = existing-removed;
					tmp.replace( "quantity", Integer.valueOf( remaining ).toString() );
					tmp.content( item.content() ); // will replace crisp with crisps...
					list.content( n, tmp );              // ...and some coffee with coffees! hmm???
				}
				// return what is left over...
				item.replace( "quantity", Integer.valueOf( removed ).toString() );
			} else {
				/*
				 * ...or, as before, remove whole item or all items...
					// i need milk/i have 37 milks
				 */
				if (( exact && tmp.equals( item.tag() ))
				 || (!exact && tmp.matches( item.tag() ))) {
					removed = 1;
			}	}
			if (removed > 0) {
				value.set( list.toString() ); // put list back...
				rc = item.toString(); // prepare return value
		}	}
		audit.out( rc );
		return rc;
	}
	public boolean move(List l) {
		/*
		 * moves the content of one list to another.
		 */
		while (l.listTags().size() > 0)
			append( new Item( l.listTags().remove( 0 )));

		return true;
	}
	private boolean exists(Item item, Strings params) {
		/* 
		 * TODO: to "list exists _user needs coffee"
		 * return "FALSE" or "5 cups of coffee" 
		 */
		/* TODO
		 * exists a & b & c + a & b => false (only if all present!)
		 */
		/* Applying an addition of exists:
		 * i.e. a+b+c? with a+b =>false
		 */
		String lastParam = params.get( params.size() - 1 );
		// also need when='any' !
		return position( item,
				!(lastParam.equals( "quantity='some'" )
				||lastParam.equals( "quantity='any'" ))) != -1;
	}
	static public String interpret( Strings sa ) {
		/* What we're doing here is to process the parameters provided in 
		 * the repertoire as processed by Intention.class (attributes are 
		 * expanded) i.e. X ==> x='xvalue'
		 * At this point the "list" is stripped from that conceptualisation.
		 * We need to ensure that the first 5 parameters are re-expanded 
		 * (so black coffee is one value/parameter):
		 * (list) get martin needs black coffee quantity='1', or perhaps...
		 * (list) get martin needs  quantity='1' black coffee
		 * This is complicated by the fact that a phrase may have an "and"
		 * in it which means the first (or last) param of each component 
		 * needs to be converted, and the operation called for each.
		 */
		String rc = Shell.FAIL;
		audit.in( "interpret", sa.toString());
		
		String	cmd = sa.remove( 0 ),
				ent = sa.remove( 0 ), 
				atr = sa.remove( 0 );
		
		List list = new List( ent, atr );
		
		if (cmd.equals( "delete" )) {
			list.ignore();
			rc = Shell.SUCCESS;
			
		} else if (cmd.equals( "undelete" )) {
			list.restore();
			rc = Shell.SUCCESS;
				
		} else if (sa.size() == 0) {
			if (cmd.equals("get"))
				rc = list.get().toString( Reply.andListFormat());
			
		} else {
			Strings paramsList = new Strings( sa );
			/* We could remove the attributes at the beginning of
			 * the params list, and add them to all items.
			 */
			Attributes as = new Attributes();
			Strings rca = new Strings();
			
			for (Strings params : paramsList.divide( "and" )) {
				Item item = new Item( params, as );
				
				if (cmd.equals( "exists" )) {
					if (list.exists( item, params )) {
						if (rca.size() == 0) rca.add( Shell.SUCCESS );
					} else {
						rca = new Strings();
						rca.add( Shell.FAIL );
						break;
					}
					
				} else if (cmd.equals( "removeAttribute" )) {
					// Typically: removeAttribute SUBJECT LIST OBJECT NAME
					list.removeAttribute(
							item,
							new Attribute( sa.get( 1 )).value()
					);
					rca.add( Shell.SUCCESS );
					
				} else if (cmd.equals( "attributeValue" )) {
					// Typically: attributeValue SUBJECT LIST OBJECT NAME
					item.attributes().remove( "name" ); // item has name embedded :(
					rca.add( list.attributeValue(
							item,
							new Attribute(
									sa.get( 1 )).value()
							).toString( Reply.andListFormat())
					);
						
				} else if (cmd.equals( "getWhere" )) {
					// Typically: getWhere _user meeting where value='the pub'
					item = null; // item built with name embedded :(
					rca.add(
							list.namedValues( sa.get( 0 ),
									new Attribute( sa.get( 1 )).value()
							).toString( Reply.andListFormat())
					);
						
				} else if (cmd.equals( "quantity" )) {
					rca.add( list.quantity( item, false ));
					
				} else if (cmd.equals( "remove" )) {
					// must return those left... so need 10, have 6, return 4.
					rca.add( list.remove( item, false ));
					
				} else if (cmd.equals( "removeAny" )) {
					while (-1 != list.position( item, false ))
						list.remove( item, false );
					rca.add( Shell.SUCCESS );
					
				} else if (cmd.equals( "add" )) {
					rca.add( list.append( item ));
					
				} else if (cmd.equals( "update" )) {
					rca.add( list.update( item ));
					
				} else if (cmd.equals("get"))
					rca.add(
							list.get( item ).toString(
									Reply.andListFormat()
					)		);
			}
			// some (e.g. get) may have m-values, some (e.g. exists) only one
			rc = rca.size() == 0 ?
					Shell.FAIL : rca.toString( Reply.andListFormat());
		}
		return audit.out( rc );
	}
	// ----------------------------------------------------------------------------------
	// params( "one two = three" ]) => [ "one", "two=three" ]
	static public Strings params( String s ) {
		return new Strings( s ).contract( "+=" ).contract( "-=" ).contract( "=" );
	}
	static public void test( int id, String cmd, String result ) {
		String s = List.interpret( params( cmd ));
		if (!result.equals( "" ) && !new Strings( s ).equals( new Strings( result )))
			audit.FATAL(
					(id != -1 ? id +": " : "")+
					cmd +", returns:\n"
					+ "\t'"+ s +"'\n"
					+ "but should return:\n"
					+ "\t'"+ result +"'"
			);
	}
	static public void test( int id, String cmd ) { test( id, cmd, "" );}
	static public void test( String cmd ) { test( -1, cmd, "" );}
	static public void test( String cmd, String result ) {test( -1, cmd, result );}
	
	public static void main( String[] argv ) {
		// Audit.turnOn();
		// Audit.runtimeDebug = true;
		// Audit.tracing = true;
		// localDebug = true;
		
		// BEGIN SHOPPING LIST TESTS...
		Item.format( "QUANTITY,UNIT of,,from FROM" );
		test( 101, "delete martin needs", "TRUE" );
		test( 102, "add martin needs coffee quantity='1'", "a coffee" );
		test( 103, "add martin needs coffee quantity='8 more'", "8 more coffees" );
		test( 104, "add martin needs milk quantity='6' unit='pint'",   "6 pints of milk" );
		test( 105, "get martin needs", "9 coffees, and 6 pints of milk" );

		// remove more milk than we've got, and not all coffees
		test( 106, "remove martin needs milk quantity='10' unit='pint'", "6 pints of milk" ); 
		test( 107, "remove martin needs coffees quantity='6'", "6 coffees" );
		test( 108, "get martin needs", "3 coffees");

		test( 109, "exists martin needs coffees quantity='3'",   "TRUE" );
		test( 110, "exists martin needs coffees quantity='any'", "TRUE" );
		test( 111, "exists martin needs coffees quantity='1'",  "FALSE" );
		
		test( 112, "removeAny martin needs coffee", "TRUE" );
		test( 113, "get martin needs", "");
		// END SHOPPING LIST TEST.
		
		// BEGIN Calendar list tests...
		Item.format( ","+Where.LOCATOR +" "+ Where.LOCATION+",WHEN" );
		test( 201, "add _user meeting fred locator='at' location='the pub' when='20151225190000'",
				  "fred at the pub at 7 pm on the 25th of December , 2015" );
		test( 202, "add _user meeting fred locator='at' location='the pub' when='20151225193000'",
				  "fred at the pub at 7 30 pm on the 25th of December , 2015" );
		// END Calendar list tests.

		audit.log( "All tests pass!" );
}	}

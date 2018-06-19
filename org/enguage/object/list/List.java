package org.enguage.object.list;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.object.Value;
import org.enguage.util.Attribute;
import org.enguage.util.Audit;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.vehicle.Reply;
import org.enguage.vehicle.Number;
import org.enguage.vehicle.where.Where;

public class List extends ArrayList<Item> {
	static final long serialVersionUID = 0L;
	static       private Audit   audit = new Audit( "List" );
	static final public    String NAME = "list";
	
	Value value; // instead of extending this class...
	public void ignore() {value.ignore();}
	public void restore() {value.restore();}
	
	// constructors
	public List( String e, String a ) {
		loadTagData( value = new Value( e, a ));
	}
	
	//----------------------- List of Items Code --------
	private void loadTagData( Value v ) {
		Strings ss = new Strings( v.getAsString());
		ListIterator<String> si = ss.listIterator();
		if (doName( si )) {
			Item it;
			while (null != (it = getItem( si )))
				add( it );
	}	}
	private boolean doName( ListIterator<String> si ) {
		return si.hasNext()
				 && si.next().equals("<")
				 && si.hasNext()
				 && si.next().equals( NAME );
	}
	private Item getItem( ListIterator<String> si ) {
		Item it = null;
		if (si.hasNext() && si.next().equals( ">" ) &&
			si.hasNext() && si.next().equals( "<" ) &&
			si.hasNext() && si.next().equals( "item" ))
		{
			it = new Item();
			Attribute a;
			while (null != (a = getAttr( si )))
				it.attributes().add( a );
			it.description( getDescr( si ));
			if (!si.hasNext() || !si.next().equals( "/" ) &&
				!si.hasNext() || !si.next().equals( "item" ))
				audit.ERROR( "Missing end-item tag");
		}
		return it;
	}
	private Strings getDescr( ListIterator<String> si ) {
		Strings sa = new Strings();
		String s;
		while (si.hasNext() &&
				!(s = si.next()).equals( "<" ))
			sa.append( s );
		return sa;
	}
	private Attribute getAttr( ListIterator<String> si ) {
		// going to read "name", "=", "val" or ">" descr1 descr2
		if (si.hasNext()) {
			String name = si.next();
			if (si.hasNext()) {
				if (si.next().equals( "=" ) && si.hasNext())
					return new Attribute( name, Strings.trim( si.next(), '\'' ) );
				else
					si.previous();   // readahead=2, but...
					//si.previous(); // don't put ">" back
			} else
				si.previous();
		}
		return null;
	}
	private int index( Item item, boolean exact ) {
		//audit.in( "find", "lookingFor="+ item.toXml() +" f/p="+ (exact ? "FULL":"partial"));
		
		String ilocr = item.attribute( Where.LOCATOR );
		String ilocn = item.attribute( Where.LOCATION );
		long   iwhen = item.when();
		
		int pos = -1;
		for (Item li : this) {
			pos++;
			String tlocr = li.attribute( Where.LOCATOR );
			String tlocn = li.attribute( Where.LOCATION );
			long   tt    = li.when();
			if ( (iwhen == -1 || iwhen == tt) // if tt == -1 && it != -i fail!
				&& (!exact || (
					   (ilocr.equals( "" ) || ilocr.equals( tlocr ))
					&& (ilocn.equals( "" ) || ilocn.equals( tlocn )))
				)
				&&     (( exact && li.equals( item ))
			         || (!exact && li.matchesDescription( item )))
			 )
				return pos; //audit.out( pos );
		}
		return -1; //audit.out( -1 );
	}
	private String quantity( Item item, boolean exact ) { // e.g. ["cake slices","2"]
		audit.in( "quantity", "Item="+item.toString() + ", exact="+ (exact?"T":"F"));
		Integer count = 0;
		for (Item t : this ) // go though the file
			if (   ( exact && t.equals( item ))
			    || (!exact && t.matchesDescription( item )))
				count += t.quantity();
		audit.out( count );
		return count.toString();
	}
	public  String toString() { return toString( null ); }
	private String toString( Item pattern ) { // to Items class!
		audit.in( "get", "item="+ (pattern==null?"ALL":pattern.toString()));
		Groups g=new Groups();
		for (Item item : this)
			if (pattern == null || item.matches( pattern ))
				g.add( item.group(), item.toString());
		return audit.out( g.toString());
	}
	private String append( Item item ) { // adjusts attributes, e.g. quantity
		String rc = item.toString()+ " "+ item.group(); // return what we've just said
		audit.in( "append", item.toXml() +" ("+ rc +")" );
		int n = index( item, false ); // exact match? No!
		if (-1 == n) { 
			// combining nothing with a relative number
			// in case: quantity='+= 37' => set it to '37'
			String quantity = item.attribute( "quantity" );
			audit.debug( "quantity="+ quantity );
			Number number = Number.getNumber( quantity );
			if (number.magnitude() != 0.0f) {
				// replace whether relative or not...
				audit.log( "replacing q with "+ number.toString());
				item.replace( "quantity", number.toString());
				add( item );
			}
		} else { // found so update item...
			Item removedItemTag = remove( n );
			item.updateItemAttributes( removedItemTag );
			String quantity = removedItemTag.attribute( "quantity" );
			Number number = new Number( quantity );
			if (quantity.equals( "" ) || number.magnitude() != 0.0f) {
				audit.debug( "re-adding "+ removedItemTag.toXml());
				add( n, removedItemTag );
			}
		}
		value.set( toXml() );
		return audit.out( rc );
	}
	//----------------------- end of List of Items Code --------

	private String update( Item item ) { // adjusts attributes, e.g. quantity
		String rc = item.toString(); // return what we've just said
		int n = index( item, false ); // exact match? No!
		if (-1 != n) { 
			Item removedItemTag = remove( n );
			item.updateItemAttributes( removedItemTag );
			String quantity = removedItemTag.attribute( "quantity" );
			if (quantity.equals( "" ) || Integer.valueOf( quantity ) != 0)
				add( n, removedItemTag );
			value.set( toXml() );
		}
		return audit.out( rc );
	}
	public String toXml() {
		String list = "";
		for (Item item : this)
			list += "   "+item.toXml()+"\n";
		return "<list>"+ list +"</list>";
	}
	private String removeAttribute( Item item, String name ) { // adjusts attributes, e.g. quantity
		String rc = item.toString(); // return what we've just said
		audit.in( "removeAttribute", "item:"+ item.toXml() +", name="+ name );
		int n = index( item, false ); // exact match? No!
		if (-1 != n) {
			Item tmp = remove( n );
			tmp.attributes().remove( name );
			add( n, tmp );
			value.set( toXml() ); // was set( lines );
		} else
			audit.ERROR("not found "+ item.toXml());
		return audit.out( rc );
	}
	private Strings attributeValue( Item item, String name ) {
		Strings   rc = new Strings();
		String upper = name.toUpperCase( Locale.getDefault() );
		audit.in( "attributeValue", "item='"+ item.toXml() +"', name="+ upper );
		for (Item t : this)  {
			if (item == null || t.matchesDescription( item )) {
				if (t.attributes().has( upper )) {
					rc.add( t.attributes().get( upper ));
		}	}	}
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
		if (-1 != (n = index( item, exact ))) {
			/*
			 * we have a listed item, remove the item in question...
			 */
			Item tmp = remove( n );
			
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
					tmp.description( item.description() ); // will replace crisp with crisps...
					add( n, tmp );              // ...and some coffee with coffees! hmm???
				}
				// return what is left over...
				item.replace( "quantity", Integer.valueOf( removed ).toString() );
			} else {
				/*
				 * ...or, as before, remove whole item or all items...
					// i need milk/i have 37 milks
				 */
				if (( exact && tmp.equals( item ))
				 || (!exact && tmp.matches( item ))) {
					removed = 1;
			}	}
			if (removed > 0) {
				value.set( toXml() ); // put list back...
				rc = item.toString(); // prepare return value
		}	}
		audit.out( rc );
		return rc;
	}
	public boolean move(List l) {
		/*
		 * moves the content of one list to another.
		 */
		while (l.size() > 0)
			append( new Item( l.remove( 0 )));

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
		return index( item,
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
				rc = list.toString();
			
		} else {
			Strings paramsList = new Strings( sa ),
			        rca        = new Strings();
			
			for (Strings params : paramsList.divide( "and" )) {

				Item item = new Item( params );
				
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
						
						
				} else if (cmd.equals( "quantity" )) {
					rca.add( list.quantity( item, false ));
					
				} else if (cmd.equals( "remove" )) {
					// must return those left... so need 10, have 6, return 4.
					rca.add( list.remove( item, false ));
					
				} else if (cmd.equals( "removeAny" )) {
					while (-1 != list.index( item, false ))
						list.remove( item, false );
					rca.add( Shell.SUCCESS );
					
				} else if (cmd.equals( "add" )) {
					rca.add( list.append( item ));
					
				} else if (cmd.equals( "update" )) {
					rca.add( list.update( item ));
					
				} else if (cmd.equals( "get" ))
					rca.add( list.toString( item ));
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
		
		//Audit.allOn();
		
		// Audit.runtimeDebug = true;
		// Audit.tracing = true;
		// localDebug = true;

		List l = new List( "martin", "needs" );
		l.append( new Item( "coffee   unit='cup' quantity='1' locator='from' location='Sainsburys'"   ));
		l.append( new Item( "biscuits            quantity='1' locator='from' location='Sainsburys'"   ));
		l.append( new Item( "locator='from' unit='pint' quantity='1' location='the dairy aisle' milk" ));
		audit.log( "martin needs: "+ l.toString());
		
		Item.format( "QUANTITY,UNIT of,,LOCATOR LOCATION" );
		Item.groupOn( "LOCATION" );
		audit.log( "get martin needs: "+ interpret( new Strings( "get martin needs" )));
		//System.exit( 0 );
		
		// BEGIN SHOPPING LIST TESTS...
		Item.format( "QUANTITY,UNIT of,,from FROM" );
		test( 101, "delete martin needs", "TRUE" );
		Audit.allOn();
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
		Item.groupOn( "" ); // reset
		Item.format( ","+Where.LOCATOR +" "+ Where.LOCATION+",WHEN" );
		test( 201, "add _user meeting fred locator='at' location='the pub' when='20151225190000'",
				  "fred at the pub at 7 pm on the 25th of December , 2015" );
		test( 202, "add _user meeting fred locator='at' location='the pub' when='20151225193000'",
				  "fred at the pub at 7 30 pm on the 25th of December , 2015" );
		// END Calendar list tests.

		audit.log( "All tests pass!" );
}	}

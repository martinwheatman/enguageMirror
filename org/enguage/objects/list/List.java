package org.enguage.objects.list;

import java.util.ArrayList;
import java.util.ListIterator;

import org.enguage.objects.space.Value;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.number.Number;
import org.enguage.vehicle.reply.Reply;
import org.enguage.vehicle.where.Where;

public class List extends ArrayList<Item> {
	static final long serialVersionUID = 0L;
	static       private Audit   audit = new Audit( "List" );
	static final public  String   NAME = "list";
	
	Value value; // instead of extending this class...
	public void ignore() {value.ignore();}
	public void restore() {value.restore();}
	
	// constructors
	public List( String e, String a ) {loadTagData( value = new Value( e, a ));}
	
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
		return si.hasNext() && si.next().equals("<")
			&& si.hasNext() && si.next().equals( NAME );
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
					return new Attribute( name, Strings.trim( Strings.trim( si.next(), Attribute.DEF_QUOTE_CH ), Attribute.ALT_QUOTE_CH) );
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
		
		String ilocr = item.attribute( Where.LOCTR );
		String ilocn = item.attribute( Where.LOCTN );
		long   iwhen = item.when();
		
		int pos = -1;
		for (Item li : this) {
			pos++;
			String tlocr = li.attribute( Where.LOCTR );
			String tlocn = li.attribute( Where.LOCTN );
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
	private int matches( Item item ) {
		audit.in( "matches", "item="+ item.toXml());
		int pos = -1;
		for (Item li : this) {
			pos++;
			audit.debug( "matching: "+ li.toXml() );
			if (li.matches( item ))
				return audit.out( pos ); // pos;
		}
		return audit.out( -1 ); // -1;
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
		audit.in( "get", "item="+ (pattern==null?"ALL":pattern.toXml()));
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
			String quantity = item.attribute( "quantity" );
			Number number = new Number( quantity );
			if (quantity.equals( "" ) || number.magnitude() != 0.0f) {
				// combining nothing with a relative number
				// in case: quantity='+= 37' => set it to '37'
				if (number.relative()) {
					number.relative( false );
					item.replace( "quantity", number.toString() );
				}
				add( item );
			}
		} else { // found so update item...
			Item removedItemTag = remove( n );
			removedItemTag.updateAttributes( item.attributes() );
			String quantity = removedItemTag.attribute( "quantity" );
			if (quantity.equals( "" ) || new Number( quantity ).magnitude() != 0.0f)
				add( n, removedItemTag );
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
			removedItemTag.updateAttributes( item.attributes() );
			String quantity = removedItemTag.attribute( "quantity" );
			if (quantity.equals( "" ) || new Number( quantity ).magnitude() != 0.0F)
				add( n, removedItemTag );
			value.set( toXml() );
		}
		return audit.out( rc );
	}
	public String toXml() {
		String list = "";
		for (Item item : this)
			list += item.toXml()+"\n      ";
		return "<list>"+ list +"</list>";
	}
	private String delAttr( Item item, String name ) { // adjusts attributes, e.g. quantity
		String rc = item.toString(); // return what we've just said
		audit.in( "delAttr", "item:"+ item.toXml() +", name="+ name );
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
	private Strings getAttrVal( Item item, String name ) {
		Strings   rc = new Strings();
		audit.in( "getAttrVal", "item='"+ item.toXml() +"', name="+ name );
		for (Item t : this) 
			if ((item == null || t.matchesDescription( item ))
				&& (t.attributes().has( name )))
					rc.add( t.attributes().get( name ));
		return audit.out( rc );
	}
	private boolean isAttrVal( Strings descr, String name, String value ) {
		/* Example:??? item = name = value =???
		 * item  => <item cause="i am baking a cake">i need 3 eggs</item>
		 * name  => 'cause'
		 * value => i need 3 eggs.
		 */
		boolean rc = false,
		   isTrans = Trans.isConcept( name );
		audit.in( "isAttrVal", "descr='"+ descr +"', name='"+ name +"', value="+ value );
		for (Item li : this) {
			if (descr.equalsIgnoreCase( li.description())) {
				String cause = li.attributes().get( name );
				if (!cause.equals(""))
					if (           (rc = cause.equals( value )) ||
					    isTrans && (rc = isAttrVal( descr, name, cause )))
						break;
		}	}
		return audit.out( rc );
	}
	/* this needs to include adjusting quantity downwards, as above in add()
	 * i need coffee + I have 3 coffees = ???
	 * Returns number removed: existing - to be removed.
	 */
	private String removeQuantity( Item tbr, boolean exact ) {
		String rc = Shell.FAIL;
		audit.in("removeQuantity", "item="+ tbr.toXml() +", exact="+ (exact?"T":"F"));
		
		int n;
		if (-1 != (n = index( tbr, exact ))) {
			Item tmp = remove( n );
			
			if (   tbr.attributes().has( "quantity" )
			    && tmp.attributes().has( "quantity" ))
			{
				String maxRemoval = new Number( tmp.attribute( "quantity" )).toString();
				
				tmp.removeQuantity( new Number( tbr.attribute( "quantity" )));
				tmp.description( tbr.description() ); // will replace crisp with crisps...
				
				if (new Number( tmp.attribute( "quantity" )).magnitude() <= 0) {
					tbr.replace( "quantity", maxRemoval );
					tmp.replace( "quantity", "0" );
				} else
					add( n, tmp );
				rc = tbr.toString(); // prepare return value
				
			} else if (   ( exact && tmp.equals( tbr ))
			           || (!exact && tmp.matches( tbr )))
				/* ...or, as before, remove whole item or all items...
				 *	// i need milk/i have milk
				 *	// i need milk/i have 37 milks
				 *	// i need 37 milks/i have milk
				 */
				rc = tbr.toString(); // prepare return value
				
		}
		if (!rc.equals( Shell.FAIL ))
			value.set( toXml() ); // put list back...
		return audit.out( rc );
	}
	public String removeAll( Item tbr ) {
		audit.in( "removeAll", tbr==null?"<ALL>":tbr.toXml());
		ArrayList<Item> reprieve = new ArrayList<Item>();
		while (size() > 0) {
			Item itm = remove( 0 );
			if (tbr != null && !itm.attributes().matches( tbr.attributes() ))
				reprieve.add( itm );
		}
		for (Item itm : reprieve) add( itm );
		value.set( toXml() );
		return audit.out( Shell.SUCCESS );
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
	static public Strings interpret( Strings sa ) {
		/* An item may be <item>black coffee</item>, or
		 * <item unit="cup" quantity="1">black coffee</item>
		 */
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
		Strings rc = Shell.Fail;
		audit.in( "interpret", sa.toString());
		
		String	cmd = sa.remove( 0 ),
				ent = sa.remove( 0 ), 
				atr = sa.remove( 0 ),
				attrName = cmd.equals( "getAttrVal" ) ? sa.remove( 0 ) : "";
		
		List list = new List( ent, atr );
		
		if (cmd.equals( "delete" )) {
			list.ignore();
			rc = Shell.Success;
			
		} else if (cmd.equals( "undelete" )) {
			list.restore();
			rc = Shell.Success;
				
		} else if (sa.size() == 0) {
			if (cmd.equals("get"))
				rc = new Strings( list.toString());
			
			else if (cmd.equals( "removeAll" )) {
				list.removeAll( (Item)null );
				rc = Shell.Success;
			}
			
		} else {
			Strings paramsList = new Strings( sa ),
			        rca        = new Strings();
			
			for (Strings params : paramsList.divide( "and" )) {
				
				// Expand params, e.g. if param="OBJECT='black coffee'"
				if (params.size() == 1)
					params = Attribute.getValues( params.get( 0 ));
				
				Item item = new Item( params );
				
				if (cmd.equals( "exists" )) {
					if (list.exists( item, params )) {
						if (rca.size() == 0) rca.add( Shell.SUCCESS );
					} else {
						rca = new Strings();
						rca.add( Shell.FAIL );
						break;
					}
					
				} else if (cmd.equals( "matches" )) {
						if (list.matches( item ) != -1) {
							if (rca.size() == 0) rca.add( Shell.SUCCESS );
						} else {
							rca = new Strings();
							rca.add( Shell.FAIL );
							break;
						}
						
				} else if (cmd.equals( "delAttr" )) {
					// Typically: delAttr SUBJECT LIST OBJECT NAME
					list.delAttr(
							item,
							new Attribute( sa.get( 1 )).value()
					);
					rca.add( Shell.SUCCESS );
					
				} else if (cmd.equals( "getAttrVal" )) {
					
					// Typically: getAttrVal SUBJECT LIST NAME OBJECT
					rca.add( list.getAttrVal(
								item,
								Attribute.getValue( attrName ) // Expand: n='v' => v
							).toString( Reply.andListFormat())
					);
						
				} else if (cmd.equals( "isAttrVal" )) {
					// called in why.txt: [list isAttrVal SUBJECT LIST cause] WHAT CAUSE
					// audit.log( "Item is: "+ item.toXml());
					rca.add( list.isAttrVal(
								new Strings( item.attribute( "what" )), // i need 3 eggs
								item.description().toString(), // cause - not description!
								item.attribute( "cause" )      // i am baking a cake
							) ? Shell.SUCCESS : Shell.FAIL
					);
						
				} else if (cmd.equals( "quantity" )) {
					rca.add( list.quantity( item, false ));
					
				} else if (cmd.equals( "remove" )) {
					// must return those left... so need 10, have 6, return 4.
					rca.add( list.removeQuantity( item, false ));
					
				} else if (cmd.equals( "removeAny" )) {
					while (-1 != list.index( item, false ))
						rca.add( list.removeQuantity( item, false ));
					
				} else if (cmd.equals( "removeAll" )) {
					rca.add( list.removeAll( item ));
					
				} else if (cmd.equals( "add" )) {
					rca.add( list.append( item ));
					
				} else if (cmd.equals( "update" )) {
					rca.add( list.update( item ));
					
				} else if (cmd.equals( "get" ))
					rca.add( list.toString( item ));
			}
			// some (e.g. get) may have m-values, some (e.g. exists) only one
			rc = rca.size() == 0 ?
					Shell.Fail : new Strings( rca.toString( Reply.andListFormat()));
		}
		return audit.out( rc );
	}
	// ----------------------------------------------------------------------------------
	// params( "one two = three" ]) => [ "one", "two=three" ]
	static public Strings params( String s ) {
		return new Strings( s ).contract( "+=" ).contract( "-=" ).contract( "=" );
	}
	static public void test( int id, String cmd, String result ) {
		Strings s = List.interpret( params( cmd ));
		if (!result.equals( "" ) && !s.equals( new Strings( result )))
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
		
		Item.format( "QUANTITY,UNIT of,,"+ Where.LOCTR +" "+ Where.LOCTN );
		Item.groupOn( Where.LOCTN );
		audit.log( "get martin needs: "+ interpret( new Strings( "get martin needs" )));
		
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
		
		test( 112, "removeAny martin needs coffee", "coffee" );
		test( 113, "get martin needs", "");
		// END SHOPPING LIST TEST.
		
		// BEGIN Calendar list tests...
		Item.groupOn( "" ); // reset
		Item.format( ","+Where.LOCTR +" "+ Where.LOCTN+",WHEN" );
		test( 201, "add _user meeting fred locator='at' location='the pub' when='20151225190000'",
				  "fred at the pub at 7 pm on the 25th of December , 2015" );
		test( 202, "add _user meeting fred locator='at' location='the pub' when='20151225193000'",
				  "fred at the pub at 7 30 pm on the 25th of December , 2015" );
		// END Calendar list tests.

		// BEGIN test why
		l = new List( "martin", "why" );
		
		l.append( new Item( "i need 3 eggs               cause='i am baking a cake'"   ));
		l.append( new Item( "i need to go to the garage  cause='i need 3 eggs'"   ));
		l.value.set( l.toXml() );
		
		test( 300, "getAttrVal martin why name='cause' i need 3 eggs",              "i am baking a cake" );
		test( 301, "getAttrVal martin why name='cause' i need to go to the garage", "i need 3 eggs" );
		// END test why
		
		audit.log( "All tests pass!" );
}	}

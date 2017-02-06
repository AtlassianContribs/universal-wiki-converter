package com.atlassian.uwc.converters.moinmoin;

import java.util.Enumeration;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class MoinListConverter extends BaseConverter {

	private Logger log = Logger.getLogger(this.getClass());
	
	protected static final String LSEP = System.getProperty("line.separator", "\n");
	

	
	public void convert(Page page) {
		log.debug("Converting list in Page: " + page.getName());
		
		String cont = page.getOriginalText();
		
		page.setConvertedText(this.convertList(cont));

	}
	

	
	private class Indent {
		
		int their;
		int mine;
		String symbol;
		
//		Indent(int their, int mine){
//			this.their = their;
//			this.mine = mine;
//		}
		
		Indent(int their, int mine, String symbol){
			this.their = their;
			this.mine = mine;
			this.symbol = symbol;
		}
		
		Indent(){
		}
		
		Indent getNew(int theirnew, String symbol){
			return new Indent(theirnew, mine + 1, symbol);
		}
		
		boolean hasNew(int theirnew){
			return theirnew > this.their;
		}
		
		boolean isSame(int theirnew){
			return theirnew == their;
		}
	}
	
	private final Indent first = new Indent(-1,0," ");
	
//	void appendX(StringBuilder sb, indent){
//		for (int i = 0; i < count; i++){
//			sb.append(s);
//		}
//	}
//	
	
	void appendX(StringBuilder sb, Stack<Indent> s){
		for (Enumeration<Indent> e = s.elements(); e.hasMoreElements(); ){
			sb.append( e.nextElement().symbol );
		}
	}
	
	static Pattern startp = Pattern.compile("(\\s*)(([\\*\\.])|([AaIi1]\\.)) +.*", Pattern.MULTILINE);
	
	String convertList(String input){
		StringBuilder output = new StringBuilder();

		final Stack<Indent> intend = new Stack<Indent>();
		intend.push(first);
		
				
		for( String line : input.split("\\r?\\n") ){
			
			
			
			Matcher mat = startp.matcher(line);
			
			if( mat.matches() ){
				
				//log.debug("LINE 1 " + line);
				
				int intendation =  mat.group(1) != null ? mat.group(1).length() : 0;
				
				
				// find the different type
				final int symbolLength;
				final String symbol;
				
				if( mat.group(4) != null ){
					symbol = "#";
					symbolLength = 2;
				} else if( mat.group(3) != null) {
					symbol = "*";
					symbolLength = 1;
				} else { // somehow the line matches but the enum items are not there
					output.append(line);
					output.append(LSEP);

					// reset the intendation
					intend.clear();
					intend.push(first);
					continue;
				}
				
				Indent i = intend.peek();
				
				
				if( i.hasNew(intendation) ) {
					i = i.getNew(intendation, symbol);
					intend.push(i);

				} else if ( i.isSame(intendation) ){
					
				} else {
					intend.pop();
					i = intend.peek();
					if( i.equals(first) ){
						// Moin Moin has a silly behavior of something like this
						//    * first (level one)
						//  * second level one (this has to be level one but only if they dont have a previous thing 
						output.append(LSEP);
						i = i.getNew(1, symbol);
						intend.push(i);
					}
				}
				
				i.symbol = symbol; //actual current symbol 
				
				// output.append(" ");
				appendX(output, intend);
				output.append(line.substring(intendation + symbolLength));
				output.append(LSEP);
				
				
			} else {
				
				//log.debug("LINE 0 " + line);
				
				output.append(line);
				output.append(LSEP);

				// reset the intendation
				intend.clear();
				intend.push(first);
			}
			
			
		}
		
		return output.toString();
	}

}

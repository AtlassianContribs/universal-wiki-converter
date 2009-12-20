package com.atlassian.uwc.converters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author Laura Kolker
 * This class represents an illegal character (for the purposes of page naming).
 * It has a value -> the original illegal char, a replacement -> what to replace the 
 * illegal char with, and a type -> ANYWHERE or START_ONLY.
 */
public class IllegalChar {
	/**
	 * log4j obj
	 */
	Logger log = Logger.getLogger(this.getClass());
	/**
	 * the illegal character this obj represents
	 */
	private String value;
	/**
	 * the replacement character for the illegal char this object represents
	 */
	private String replacement;
	/**
	 * if START_ONLY, then the illegal char is only illegal if the first char of a string
	 */
	private Type type;
	
	/**
	 * represents when this object is illegal: at the start of a string or anywhere
	 */
	public enum Type {
		/**
		 * this obj will only be considered illegal if it is the first character 
		 * of the pagename
		 */
		START_ONLY,
		/**
		 * this obj will be considered illegal if it occurs anywhere in a string
		 */
		ANYWHERE;
		
		/**
		 * @param obj
		 * @return true if given obj is equal to this type
		 */
		public boolean equals (Type obj){ return this == obj; } 
		/**
		 * @param m
		 * @param replacement
		 * @return a replacement string, for a given instantiated matcher
		 * with a given replacement string
		 */
		public String replace (Matcher m, String replacement) { 
			switch(this) {
			case ANYWHERE:
				return m.replaceAll(replacement);
			case START_ONLY:
				return m.replaceFirst(replacement);
			default:
				return null;
			}
		} 
	}
	
	/**
	 * creates an IllegalChar object 
	 * @param value original character that is illegal
	 * @param replacement character to be used as a replacement for the illegal char
	 * @param type either Type.ANYWHERE, or TYPE.START_ONLY, the latter represents chars 
	 * that are illegal only at the start of a pagename
	 */
	public IllegalChar(String value, String replacement, Type type) {
		this.value = value;
		this.replacement = replacement;
		this.type = type;
	}

	/**
	 * @return current replacement char for this object
	 */
	public String getReplacement() {
		return replacement;
	}

	/**
	 * @return Type.ANYWHERE or Type.START_ONLY
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return illegal char this object represents
	 */
	public String getValue() {
		return value;
	}
	
	public String toString() {
		String string = "\n" +
				"value = '" + this.value + "'\n" +
				"replacement = '" + this.replacement + "'\n" +
				"type = " + this.type + "\n";
		
		return string;
	}
	
	/**
	 * @param obj
	 * @return true, if this object is equivalent to the given object
	 */
	public boolean equals(IllegalChar obj) {
		if (obj == null) 
			return false;
		
		boolean eqVal = same(this.getValue(), obj.getValue());
		boolean eqRep = same(this.getReplacement(), obj.getReplacement());
		boolean eqTyp = same(this.getType(), obj.getType());
		
		return eqVal && eqRep && eqTyp;
	}
	
	/**
	 * compares two objects.
	 * @param a 
	 * @param b
	 * @return true, if objects are the same
	 */
	private boolean same(Object a, Object b) {
		if (a == null && b == null) return true;
		if (a == null) return false;
		return (a.equals(b));
	}

	
	/**
	 * gets a replacement string for the given input, that 
	 * replaces any illegal chars represented by this object
	 * with this object's replacement char
	 * @param input
	 * @return input with replaced illegal characters
	 */
	public String getReplacement(String input) {
		//getValue could easily contain a regex character. 
		//Use Pattern.quote to escape regex chars
		String pattern = Pattern.quote(getValue()); 
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(input);
		if (m.find()) {
			Type type = this.getType();
			return type.replace(m, this.getReplacement());
		}
		return input;
	}

}

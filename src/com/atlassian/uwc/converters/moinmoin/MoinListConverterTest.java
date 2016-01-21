package com.atlassian.uwc.converters.moinmoin;

import junit.framework.TestCase;

public class MoinListConverterTest extends TestCase {

	private MoinListConverter conv = new MoinListConverter();
	
	private static String LSEP = MoinListConverter.LSEP;
	
	public void testOne(){
		String input = 
			" * hallo" + LSEP +
			"  * hallo" + LSEP ;
		
		String expected =
			" * hallo" + LSEP +
			" ** hallo" + LSEP;
		
		String res = conv.convertList(input);
		
		assertEquals(expected, res);
		
	}
	
	public void testTwo(){
		String input = 
			" * hallo" + LSEP +
			"  * hallo" + LSEP +
			" * hallo" + LSEP;
		
		String expected =
			" * hallo" + LSEP +
			" ** hallo" + LSEP +
			" * hallo" + LSEP;
		
		String res = conv.convertList(input);
		
		assertEquals(expected, res);
		
	}
	
	public void testThree(){
		String input = 
			"* hallo" + LSEP +
			" * hallo" + LSEP +
			"* hallo" + LSEP;
		
		String expected =
			" * hallo" + LSEP +
			" ** hallo" + LSEP +
			" * hallo" + LSEP;
		
		String res = conv.convertList(input);
		
		assertEquals(expected, res);
		
	}
	
	
	public void testFour(){
		String input = 
			" * hallo" + LSEP +
			"  * hallo" + LSEP +
			"   * hallo" + LSEP;
		
		String expected =
			" * hallo" + LSEP +
			" ** hallo" + LSEP +
			" *** hallo" + LSEP;
		
		String res = conv.convertList(input);
		
		assertEquals(expected, res);
		
	}
	
	public void testFive(){
		String input = 
			" * hallo" + LSEP +
			"  * hallo" + LSEP +
			"" + LSEP +
			"   * hallo" + LSEP;
		
		String expected =
			" * hallo" + LSEP +
			" ** hallo" + LSEP +
			"" + LSEP +
			" * hallo" + LSEP;
		
		String res = conv.convertList(input);
		
		assertEquals(expected, res);
		
	}
	
	public void testSix(){
		String input = 
			" * hallo" + LSEP +
			"  * hallo" + LSEP +
			"    * hallo" + LSEP +
			"  * hallo" + LSEP +
			" * hallo" + LSEP;
		
		String expected =
			" * hallo" + LSEP +
			" ** hallo" + LSEP +
			" *** hallo" + LSEP +
			" ** hallo" + LSEP +
			" * hallo" + LSEP;
		
		String res = conv.convertList(input);
		
		assertEquals(expected, res);
		
	}
	
	public void testSeven(){
		String input = 
			" * hallo" + LSEP +
			"  * hallo" + LSEP +
			"   * hallo" + LSEP +
			"Hallo" + LSEP +
			"  * hallo" + LSEP +
			" * hallo" + LSEP;
		
		String expected =
			" * hallo" + LSEP +
			" ** hallo" + LSEP +
			" *** hallo" + LSEP +
			"Hallo" + LSEP +
			" * hallo" + LSEP +
			"" + LSEP +
			" * hallo" + LSEP;
		
		String res = conv.convertList(input);
		
		assertEquals(expected, res);
		
	}
	
	public void testEight(){
		String input = 
			" * hallo" + LSEP +
			"  * hallo" + LSEP +
			"   * hallo" + LSEP +
			" *Fett* aber glücklich" + LSEP +
			" * hallo" + LSEP +
			" * hallo" + LSEP;
		
		String expected =
			" * hallo" + LSEP +
			" ** hallo" + LSEP +
			" *** hallo" + LSEP +
			" *Fett* aber glücklich" + LSEP +
			" * hallo" + LSEP +
			" * hallo" + LSEP;
		
		String res = conv.convertList(input);
		
		assertEquals(expected, res);
		
	}
	
	public void testNine(){
		String input = 
			" * hallo" + LSEP +
			"  1. hallo" + LSEP;
		
		String expected =
			" * hallo" + LSEP +
			" *# hallo" + LSEP;
		
		String res = conv.convertList(input);
		
		assertEquals(expected, res);
		
	}
	
}

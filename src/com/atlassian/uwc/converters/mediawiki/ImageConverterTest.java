package com.atlassian.uwc.converters.mediawiki;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class ImageConverterTest extends TestCase {

	ImageConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new ImageConverter();
	}
	
	public void testExistingImageConversion() {
		String input = "[[Image:Wiki.png]]"; 
		String expected = "!Wiki.png!";
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[[Image:Wiki.png|thumb]]";
		expected = "!Wiki.png|thumbnail!";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);	
	}
	
	public void testHandleImageProperty() {
		//thumb
		String input = "Wiki.png|thumb";
		String expected = "Wiki.png|thumbnail";
		String actual = tester.handleImageProperty(input);
		assertEquals(expected, actual);
		
		//none
		input = "Wiki.png";
		expected = "Wiki.png";
		actual = tester.handleImageProperty(input);
		assertEquals(expected, actual);
		
		//align
		input = "Wiki.png|right";
		expected = "Wiki.png|align=right"; 
		actual = tester.handleImageProperty(input);
		assertEquals(expected, actual);
		
		//thumb and align
		input = "Wiki.png|center|thumb"; 
		expected = "Wiki.png|thumbnail,align=center"; 
		actual = tester.handleImageProperty(input);
		assertEquals(expected, actual);
	}	
	public void testImageConvertWithContext() {
		String input = 
			"uwc-101: Mediawiki image conversion syntax needs to be case insensitive\n" +
			"[[image:abcd.png|thumb]]\n" +"[[Image:abcd.png]]\n" +
					"After\n";
		String expected = "uwc-101: Mediawiki image conversion syntax needs to be case insensitive\n" +
			"!abcd.png|thumbnail!\n" +
			"!abcd.png!\n" +
			"After\n";
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "Thumbnail image\n" +
			"\n" +
			"[[Image:Wiki.png|thumb]]\n" + 
			"\n" +
			"After\n" +
			"\n";
		expected = "Thumbnail image\n" +
			"\n" +
			"!Wiki.png|thumbnail!\n" +
			"\n" +
			"After\n" +
			"\n";
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}

	public void testAlignmentPreservation() {
		String input = "[[Image:Wiki.png|right]]";
		String expected = "!Wiki.png|align=right!";
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAlignmentPreservationWithPropsMethod() {
		String input = "Wiki.png|right";
		String expected = "Wiki.png|align=right";
		String actual = tester.handleImageProperty(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Wiki.png|right|thumb";
		expected = "Wiki.png|thumbnail,align=right";
		actual = tester.handleImageProperty(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDontFailOnColon() {
//		This is a way to link to the description page. Something Confluence doesn't have.
		String input = "[[:Image:Wiki.png]]"; 
		String expected = "!Wiki.png!"; //FIXME Probably should be [^Wiki.png]
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAltText() {
		//We don't have anywhere to put alt text, so just lose it
		String input = "[[Image:Wiki.png|jigsaw globe]]";
		String expected = "!Wiki.png!";
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testResize() {
		String input = "[[Image:Wiki.png|30 px]]";
		String expected = "!Wiki.png|width=30px!";
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[[Image:Wiki.png|130px]]";
		expected = "!Wiki.png|width=130px!";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[[Image:example.png|200x200px]]";
		expected = "!example.png|width=200px,height=200px!";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[[Image:example.jpg|frame|250px]]";
		expected = "!example.jpg|align=right,width=250px!";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[[Image:example.jpg|150px|frame]]";
		expected = "!example.jpg|align=right,width=150px!";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFrame() {
		//treat frame as align right for now
		String input = "[[Image:Wiki.png|frame|Wikipedia Encyclopedia]]";
		String expected = "!Wiki.png|align=right!";
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCreateConfluenceImage() {
		String img = "Wiki.png";
		boolean thumbnail = false;
		ImageConverter.Alignment align = ImageConverter.Alignment.LEFT;
		String expected = "Wiki.png";
		String actual = tester.createConfluenceImage(img, thumbnail, align);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		thumbnail = true;
		expected = "Wiki.png|thumbnail";
		actual = tester.createConfluenceImage(img, thumbnail, align);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		thumbnail = false;
		align = ImageConverter.Alignment.CENTER;
		expected = "Wiki.png|align=center";
		actual = tester.createConfluenceImage(img, thumbnail, align);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		thumbnail = true;
		expected = "Wiki.png|thumbnail,align=center";
		actual = tester.createConfluenceImage(img, thumbnail, align);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		thumbnail=false;
		align = ImageConverter.Alignment.RIGHT;
		expected = "Wiki.png|align=right";
		actual = tester.createConfluenceImage(img, thumbnail, align);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		thumbnail=true;
		expected = "Wiki.png|thumbnail,align=right";
		actual = tester.createConfluenceImage(img, thumbnail, align);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		
	}
	
	public void testHandleThumb() {
		boolean thumb = true;
		String expected = "|thumbnail";
		String actual = tester.handleThumbnail(thumb);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		thumb = false;
		expected = "";
		actual = tester.handleThumbnail(thumb);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHandleAlign() {
		ImageConverter.Alignment align = ImageConverter.Alignment.LEFT;
		String expected = "";
		String actual = tester.handleAlignment(align, "|");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		align = ImageConverter.Alignment.CENTER;
		expected = "|align=center";
		actual = tester.handleAlignment(align, "|");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		align = ImageConverter.Alignment.RIGHT;
		expected = "|align=right";
		actual = tester.handleAlignment(align, "|");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}

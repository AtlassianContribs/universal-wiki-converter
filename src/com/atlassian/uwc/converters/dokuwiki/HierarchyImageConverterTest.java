package com.atlassian.uwc.converters.dokuwiki;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class HierarchyImageConverterTest extends TestCase {

	HierarchyImageConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	String attachmentdirectory = "sampleData/dokuwiki/junit_resources/attachments";
	protected void setUp() throws Exception {
		tester = new HierarchyImageConverter();
		PropertyConfigurator.configure("log4j.properties");
		tester.setAttachmentDirectory(attachmentdirectory);
		Properties props = new Properties();
		props.setProperty("spacekey", "food");
		props.setProperty("collision-titles-food", "Apple,Fruit");
		props.setProperty("collision-titles-otherspace", "Testing 123");
		props.setProperty("space-food","food,drink");
		props.setProperty("space-otherspace","otherspace");
		props.setProperty("space-image", "images");
		props.put("filepath-hierarchy-ext", "");
		props.put("filepath-hierarchy-ignorable-ancestors", "sampleData/hierarchy/dokuwiki");
		tester.setProperties(props);
	}

	public void testConvertImages() {
		String input, expected, actual;
		input = "{{drink:Wiki.png}} - render\n" + 
				"{{:drink:juice:Wiki.png}} - render\n" + 
				"{{drink:juice:test.pdf|}} - link to attachment\n" + 
				"{{food:pie:test.pdf|Alias?}}\n" + 
				"{{food:pie:fruit:Wiki.png}}\n" +
				"{{food:pie:fruit_jelly:Wiki_123.png}}\n" +
				"{{food:pie:fruit_jelly:Wiki%23123.png}}\n" +
				"{{otherspace:Wiki.png}}\n" +
				"{{:cow.jpg|}}\n" + 
//				"[[start|{{:cow.jpg}}]]\n" + FIXME not doing this yet
				"{{wiki:dokuwiki-128.png}}\n" +
				"{{:images:cows:jpgs:cow.jpg|}}\n" +
				"{{:images:cows:cow.jpg|}}\n" +
				"{{:images:cow.jpg|}}\n";
		expected = "!food:Drink^Wiki.png! - render\n" +
				"!food:Juice^Wiki.png! - render\n" +
				"[food:Juice^test.pdf] - link to attachment\n" +
				"[Alias?|food:Pie^test.pdf]\n" +
				"!food:Pie Fruit^Wiki.png!\n" +
				"!food:Fruit Jelly^Wiki_123.png!\n" +
				"!food:Fruit Jelly^Wiki_23123.png!\n" +
				"!otherspace:Start^Wiki.png!\n" +
				"!food:Start^cow.jpg!\n" +
				"!food:Wiki^dokuwiki-128.png!\n" +
				"!image:Jpgs^cow.jpg!\n" +
				"!image:Cows^cow.jpg!\n" +
				"!image:Images^cow.jpg!\n";
		
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages_wCurrentPath() {
		String input, expected, actual;
		input = 
				"{{drink:food:abc.gif|}}\n" +
				"{{cow.jpg| non useful alias}}\n";
		expected = 
				"!food:Food^abc.gif!\n" +
				"!food:drink^cow.jpg!\n";
		actual = tester.convertImages(input, "drink/");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = 
				"{{drink:food:abc.gif|}}\n" +
				"{{cow.jpg| non useful alias}}\n";
		expected = 
				"!food:Food^abc.gif!\n" +
				"!food:drink^cow.jpg!\n";
		actual = tester.convertImages(input, "test/drink/");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages_wSize() {
		String input, expected, actual;
		input = "{{drink:Wiki.png?50}} - render\n" + 
			"{{:drink:juice:Wiki.png?50}} - render\n" +
			"{{drink:Wiki.png?50x100}} - render\n" + 
			"{{:drink:juice:Wiki.png?50x100}} - render\n" +
			"{{drink:Wiki?50}}\n" + //what do we do if no extension?
			"{{drink:Wiki?words&50}}\n" + //what do we do if non-number params?
			"{{drink:Wiki?words&50x100}}"; //what do we do if non-number params?
		expected = "!food:Drink^Wiki.png|width=50px! - render\n" +
			"!food:Juice^Wiki.png|width=50px! - render\n" +
			"!food:Drink^Wiki.png|width=50px,height=100px! - render\n" +
			"!food:Juice^Wiki.png|width=50px,height=100px! - render\n" +
			"!food:Drink^Wiki|width=50px!\n" +
			"!food:Drink^Wiki|width=50px!\n" +
			"!food:Drink^Wiki|width=50px,height=100px!";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}

package com.atlassian.uwc.converters.xwiki;

import java.io.File;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class AttachmentConverterTest extends TestCase {

	private static final String ATT_DIR = "./tmp/";
	AttachmentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new AttachmentConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void tearDown() {
		File file = new File(ATT_DIR + "TestPage/test.png");
		if (file.exists()) {
			file.delete();
		}
		file = new File(ATT_DIR + "TestPage/img.png");
		if (file.exists()) {
			file.delete();
		}
		file = new File(ATT_DIR + "TestPage");
		if (file.exists()) {
			file.delete();
		}
	}
	
	String TEST_FILE_XML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
			"\n" + 
			"<xwikidoc>\n" + 
			"<attachment>\n" + 
			"<filename>img.png</filename>\n" + 
			"<filesize>959</filesize>\n" + 
			"<author>XWiki.Admin</author>\n" + 
			"<date>1216844290000</date>\n" + 
			"<version>1.2</version>\n" + 
			"<comment></comment>\n" + 
			"<content>iVBORw0KGgoAAAANSUhEUgAAADQAAAA0CAAAAADgE0Q3AAAAAmJLR0QA/4ePzL8AAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfWBxcOEy+3/bdlAAADUElEQVRIx52WTW/cVBSG32tfezxJZiaT0EwnCkVKgVZI3cFAVYkFLEFCKoI/wz9Bgh/ApqqEWLQLFmUHAtJKqKIqMKhSJ50R+aCTTHzPey4L2/PVIru9C8u6Po/f83XtYzxefAUvwcACAMZPjrXc1gfJVm0VgPHA+HCQGmOMKcfQ6jYypeO/jQ1tWAGCng1sHRY4ehzYuBbUwzIojb1nOkmMBf5xNonW46iCUhoxPY3WLPBvEK826lFQAUq8xhawgNrE1muBqZJsFwVQWMAE8Vq0xIxv7a+rjhN38Z3Ezu3HQORgPO6G7VYSLjD9Gx92SJK8e//z15KFF3qJAgBmOZz+d5+ec8455+TSe18/mCw8NEAAPFvXGx9Z55yIiHPr79/suwq9d+fd0ImIc86JuFbrx5EvhwY7LndORCS9uPfo7HkNu7goQiWZXWhOn5wmpZB3M4JK6kTKleAKHVGSozAMKkCS6YiSSh4mjbiyUu4b03qjVpq98eMs3yLihCKj+lpUqvRzV0hRKkmlsjV8epaUKR2tO+co4kQoItJ1fx2Vutc9dCJ5F4mI7FPKO2JrNI+IjNrbjVLovP81s6aIk8HoaXc7KYXsZ40D5hhl8OjC5XNhKWQ2r0ym7k2423urgfLixu2sGZTKk/obVzq2AgRkZVKqnq502hZVoFcPEyU9VVVXl3vo//4aTVJIISmTqBaiktKBcapUVVXWrKkGfdNkjmhkjDcVoOGXJioYjf54uL1iyqDht7+3QdUsD/TRzb2Pdxc/lzAe9+xGM9/t//bLSQuFilIzsvN275VZaC6ag/o//MkVy4Iorqr0Pn7zg0tJsAyN7txn02R29My8o5+iCC/0crkCSm/3NyLmZizc0iIyVQAIbOdarxMUUP2r8wHpqXO+Kf0UmZ7BcOuLTbjIAsDDDQinxpr7VUQ2d2w5fLBZpHzsnV8MfuagLhx1OZjWSZpHupjnWUyLVdxJ84Y1uPa6kJSsRUmhZvdcZnD1kxyqAX6XuX1mKxSKyDNML/sVGo/RsN38CXv76mflVFX1y8jOVVwHXBgYj5NR1EqC732+FF7hvX/OTHd91ns4OV4tn3Gm+QsNjAcwUSSm2vBhYTIlAGk1xmQzinmZcfQ/40v+ffqYGLoAAAAASUVORK5CYII=</content>\n" + 
			"</attachment>\n" + 
			"</xwikidoc>\n";
	
	public void testConvert() {
		tester.setAttachmentDirectory(ATT_DIR);
		String input;
		input = TEST_FILE_XML;
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName("TestPage");
		tester.convert(page);
		Set<File> actual = page.getAttachments();
		assertNotNull(actual);
		assertEquals(1, actual.size());
		Vector<File> v = new Vector<File>();
		v.addAll(actual);
		File file = v.get(0);
		assertNotNull(file);
		assertTrue(file.exists());
		assertEquals("img.png", file.getName());
		assertTrue(file.length() > 0);
	}

	public void testGetAllAttachmentXml() {
		//one attachment
		String input; 
		input = TEST_FILE_XML;
		Vector<String> actual = tester.getAllAttachmentXml(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertTrue(actual.get(0).startsWith("<attachment>"));
		assertTrue(actual.get(0).contains("<filename>img.png</filename>"));
		
		//three attachments
		input = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
				"\n" + 
				"<xwikidoc>\n" + 
				"<attachment>\n" + 
				"<filename>a.png</filename>\n" + 
				"<filesize>15830</filesize>\n" + 
				"<author>XWiki.Admin</author>\n" + 
				"<date>1216844288000</date>\n" + 
				"<version>1.3</version>\n" + 
				"<comment></comment>\n" + 
				"<content>notvalid</content>\n" +
				"</attachment>\n" +
				"<attachment>\n" + 
				"<filename>b.png</filename>\n" + 
				"<filesize>15830</filesize>\n" + 
				"<author>XWiki.Admin</author>\n" + 
				"<date>1216844288000</date>\n" + 
				"<version>1.3</version>\n" + 
				"<comment></comment>\n" + 
				"<content>notvalid2</content>\n" +
				"</attachment>\n" +
				"<attachment>\n" + 
				"<filename>c.png</filename>\n" + 
				"<filesize>15830</filesize>\n" + 
				"<author>XWiki.Admin</author>\n" + 
				"<date>1216844288000</date>\n" + 
				"<version>1.3</version>\n" + 
				"<comment></comment>\n" + 
				"<content>notvalid3</content>\n" +
				"</attachment>\n" +
				"</xwikidoc>\n";
		actual = tester.getAllAttachmentXml(input);
		assertNotNull(actual);
		assertEquals(3, actual.size());
		assertTrue(actual.get(0).startsWith("<attachment>"));
		assertTrue(actual.get(0).contains("<filename>a.png</filename>"));
		assertTrue(actual.get(1).startsWith("<attachment>"));
		assertTrue(actual.get(1).contains("<filename>b.png</filename>"));
		assertTrue(actual.get(2).startsWith("<attachment>"));
		assertTrue(actual.get(2).contains("<filename>c.png</filename>"));
				
	}

	public void testGetAttachmentName() {
		String input, expected, actual;
		input = TEST_FILE_XML;
		expected = "img.png";
		actual = tester.getAttachmentName(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetAttachmentContents() {
		String input, expected, actual;
		input = TEST_FILE_XML;
		expected = "iVBORw0KGgoAAAANSUhEUgAAADQAAAA0CAAAAADgE0Q3AAAAAmJLR0QA/4ePzL8AAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfWBxcOEy+3/bdlAAADUElEQVRIx52WTW/cVBSG32tfezxJZiaT0EwnCkVKgVZI3cFAVYkFLEFCKoI/wz9Bgh/ApqqEWLQLFmUHAtJKqKIqMKhSJ50R+aCTTHzPey4L2/PVIru9C8u6Po/f83XtYzxefAUvwcACAMZPjrXc1gfJVm0VgPHA+HCQGmOMKcfQ6jYypeO/jQ1tWAGCng1sHRY4ehzYuBbUwzIojb1nOkmMBf5xNonW46iCUhoxPY3WLPBvEK826lFQAUq8xhawgNrE1muBqZJsFwVQWMAE8Vq0xIxv7a+rjhN38Z3Ezu3HQORgPO6G7VYSLjD9Gx92SJK8e//z15KFF3qJAgBmOZz+d5+ec8455+TSe18/mCw8NEAAPFvXGx9Z55yIiHPr79/suwq9d+fd0ImIc86JuFbrx5EvhwY7LndORCS9uPfo7HkNu7goQiWZXWhOn5wmpZB3M4JK6kTKleAKHVGSozAMKkCS6YiSSh4mjbiyUu4b03qjVpq98eMs3yLihCKj+lpUqvRzV0hRKkmlsjV8epaUKR2tO+co4kQoItJ1fx2Vutc9dCJ5F4mI7FPKO2JrNI+IjNrbjVLovP81s6aIk8HoaXc7KYXsZ40D5hhl8OjC5XNhKWQ2r0ym7k2423urgfLixu2sGZTKk/obVzq2AgRkZVKqnq502hZVoFcPEyU9VVVXl3vo//4aTVJIISmTqBaiktKBcapUVVXWrKkGfdNkjmhkjDcVoOGXJioYjf54uL1iyqDht7+3QdUsD/TRzb2Pdxc/lzAe9+xGM9/t//bLSQuFilIzsvN275VZaC6ag/o//MkVy4Iorqr0Pn7zg0tJsAyN7txn02R29My8o5+iCC/0crkCSm/3NyLmZizc0iIyVQAIbOdarxMUUP2r8wHpqXO+Kf0UmZ7BcOuLTbjIAsDDDQinxpr7VUQ2d2w5fLBZpHzsnV8MfuagLhx1OZjWSZpHupjnWUyLVdxJ84Y1uPa6kJSsRUmhZvdcZnD1kxyqAX6XuX1mKxSKyDNML/sVGo/RsN38CXv76mflVFX1y8jOVVwHXBgYj5NR1EqC732+FF7hvX/OTHd91ns4OV4tn3Gm+QsNjAcwUSSm2vBhYTIlAGk1xmQzinmZcfQ/40v+ffqYGLoAAAAASUVORK5CYII=";
		actual = tester.getAttachmentContents(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCreateFile() {
		String name = "test.png";
		String contents = "iVBORw0KGgoAAAANSUhEUgAAADQAAAA0CAAAAADgE0Q3AAAAAmJLR0QA/4ePzL8AAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfWBxcOEy+3/bdlAAADUElEQVRIx52WTW/cVBSG32tfezxJZiaT0EwnCkVKgVZI3cFAVYkFLEFCKoI/wz9Bgh/ApqqEWLQLFmUHAtJKqKIqMKhSJ50R+aCTTHzPey4L2/PVIru9C8u6Po/f83XtYzxefAUvwcACAMZPjrXc1gfJVm0VgPHA+HCQGmOMKcfQ6jYypeO/jQ1tWAGCng1sHRY4ehzYuBbUwzIojb1nOkmMBf5xNonW46iCUhoxPY3WLPBvEK826lFQAUq8xhawgNrE1muBqZJsFwVQWMAE8Vq0xIxv7a+rjhN38Z3Ezu3HQORgPO6G7VYSLjD9Gx92SJK8e//z15KFF3qJAgBmOZz+d5+ec8455+TSe18/mCw8NEAAPFvXGx9Z55yIiHPr79/suwq9d+fd0ImIc86JuFbrx5EvhwY7LndORCS9uPfo7HkNu7goQiWZXWhOn5wmpZB3M4JK6kTKleAKHVGSozAMKkCS6YiSSh4mjbiyUu4b03qjVpq98eMs3yLihCKj+lpUqvRzV0hRKkmlsjV8epaUKR2tO+co4kQoItJ1fx2Vutc9dCJ5F4mI7FPKO2JrNI+IjNrbjVLovP81s6aIk8HoaXc7KYXsZ40D5hhl8OjC5XNhKWQ2r0ym7k2423urgfLixu2sGZTKk/obVzq2AgRkZVKqnq502hZVoFcPEyU9VVVXl3vo//4aTVJIISmTqBaiktKBcapUVVXWrKkGfdNkjmhkjDcVoOGXJioYjf54uL1iyqDht7+3QdUsD/TRzb2Pdxc/lzAe9+xGM9/t//bLSQuFilIzsvN275VZaC6ag/o//MkVy4Iorqr0Pn7zg0tJsAyN7txn02R29My8o5+iCC/0crkCSm/3NyLmZizc0iIyVQAIbOdarxMUUP2r8wHpqXO+Kf0UmZ7BcOuLTbjIAsDDDQinxpr7VUQ2d2w5fLBZpHzsnV8MfuagLhx1OZjWSZpHupjnWUyLVdxJ84Y1uPa6kJSsRUmhZvdcZnD1kxyqAX6XuX1mKxSKyDNML/sVGo/RsN38CXv76mflVFX1y8jOVVwHXBgYj5NR1EqC732+FF7hvX/OTHd91ns4OV4tn3Gm+QsNjAcwUSSm2vBhYTIlAGk1xmQzinmZcfQ/40v+ffqYGLoAAAAASUVORK5CYII=";
		String pagename = "TestPage";
		File actual = tester.createFile(name, contents, pagename, ATT_DIR);
		assertNotNull(actual);
		assertTrue(actual.exists());
		assertNotNull(actual.getAbsolutePath());
		assertTrue(!"".equals(actual.getAbsolutePath()));
		assertEquals(name, actual.getName());
		assertTrue(actual.length() > 0);
	}


}

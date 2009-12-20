package com.atlassian.uwc.converters.swiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Converts html lists into confluence syntax. The closing tags (such </ol>,
 * </ul> and </il> are optional.
 * 
 * @author Bruce Sun
 * 
 */

public class HTMLListConverter extends BaseConverter
{
	String endLine = System.getProperty( "line.separator" );
	Logger log = Logger.getLogger(this.getClass());
	final static String HtmlOrderListBegin="<ol>";
	final static String HtmlOrderListEnd="</ol>";
	final static String HtmlUnOrderListBegin="<ul>";
	final static String HtmlUnOrderListEnd="</ul>";
	final static String HtmlItemBegin="<li>";
	final static String HtmlItemEnd="</li>";
	final static int HtmlTagBeginLength=4;
	final static int HtmlTagEndLength=5;
	
	final static String ConfluenceBulletList="*";
	final static String ConfluenceNumberList="#";
	
	public void convert(Page page)
	{
		log.info("Converting HTML lists -- starting");
		String input = page.getOriginalText();
		log.debug("HTML input = " + input);
		StringBuffer bf=new StringBuffer();
		while (true)
		{
			String buffer=getHTMLList(input);
			if (buffer == null)
			{
				bf.append(input);
				break;
			}
			//System.out.println(buffer);
			bf.append(convertHTMLLists("", buffer));
			int length=input.length() < buffer.length() ? input.length() : buffer.length();
			input=input.substring(length);
			
		}
		page.setConvertedText(bf.toString());
		
		log.info("Converting HTML lists -- completed");
	}
	/**
	 * Converts html lists syntax to confluence syntax.
	 * 
	 * @param prefix   the confluence list syntax such as #s or *s. It also shows
	 * the levels of nested lists.
	 * 
	 * @param input
	 * @return String
	 */
	public String convertHTMLLists(String prefix, String input)
	{
		StringBuffer sb = new StringBuffer();
		boolean found=false;
		int begin=-1, end=-1;
		int itemBegin=input.indexOf(HtmlItemBegin);
		String output=input;
		String append="";
		
		int olBegin=input.indexOf(HtmlOrderListBegin);
		int olEnd=input.lastIndexOf(HtmlOrderListEnd);
		int ulBegin=input.indexOf(HtmlUnOrderListBegin);
		int ulEnd=input.lastIndexOf(HtmlUnOrderListEnd);
		if (olBegin >= 0 && olEnd >=0)
		{
			begin=olBegin;
			end=olEnd;
			append = ConfluenceNumberList;
			found=true;
		}
		
		if (ulBegin >= 0 && ulEnd >= 0)
		{
			if (begin < 0 || (ulBegin < begin && ulEnd > end))
			{
				begin=ulBegin;
				end=ulEnd;
				append=ConfluenceBulletList;
			}
			found=true;
		}
		
		//convert the item first if it appears first.
		if (itemBegin >= 0 && ( begin < 0 || itemBegin < begin))
		{
				String temp=input.substring(itemBegin + HtmlTagBeginLength);
				int itemEnd=temp.indexOf(HtmlItemEnd);
				String otherTag=this.getFirstTag(temp, new String [] {
						HtmlOrderListBegin, HtmlUnOrderListBegin, HtmlItemBegin});
				int otherItemIndex=-1;
				if (otherTag != null)
					otherItemIndex=itemBegin + HtmlTagBeginLength + temp.indexOf(otherTag);
				//item missing closing tag
				if (itemEnd < 0 && otherItemIndex >= 0)
					output=appendEndOfLine(input.substring(0, itemBegin )) + 
						appendEndOfLine(prefix + " " + 	input.substring(itemBegin + HtmlTagBeginLength, otherItemIndex)) +
						appendEndOfLine(input.substring(otherItemIndex));
				//item closing tag after other tags. Need to check if item tag has nested
				//tag like <ol> or <ul>
				else if (otherItemIndex >= 0 && itemEnd > otherItemIndex)
				{
					temp=getHTMLList(temp);
					if (temp == null)
						output=appendEndOfLine(input.substring(0, itemBegin )) + 
							appendEndOfLine(prefix + " " + 	input.substring(itemBegin + HtmlTagBeginLength, otherItemIndex)) +
							appendEndOfLine(input.substring(otherItemIndex));
					//with valid ol and ul inside
					else
					{
						output=appendEndOfLine(input.substring(0, itemBegin + HtmlTagBeginLength)) + 
							appendEndOfLine(convertHTMLLists(prefix, temp)) + 
							appendEndOfLine(input.substring(itemBegin + HtmlTagBeginLength + temp.length()));
						
					}
				}
				//item missing closing tag, but no other begin tag after either
				else if (itemEnd < 0 && otherItemIndex < 0)
					output=appendEndOfLine(input.substring(0, itemBegin )) + 
						appendEndOfLine(prefix + " " + input.substring(itemBegin + HtmlTagBeginLength));
				//item with closing tag -- normal case
				else
					output=appendEndOfLine(input.substring(0, itemBegin )) + 
						appendEndOfLine(prefix + " " + input.substring(itemBegin + HtmlTagBeginLength, itemBegin + HtmlTagBeginLength + itemEnd)) +  
						appendEndOfLine(input.substring(itemBegin + HtmlTagBeginLength + itemEnd + HtmlTagEndLength));
				
				return output=convertHTMLLists(prefix, output);
			
	    }	
		//now convert the order or non-order list
		if (found)
		{
			output = appendEndOfLine(input.substring(0, begin)) + 
				appendEndOfLine(convertHTMLLists (prefix+append, input.substring(begin + HtmlTagBeginLength, end))) + 
				appendEndOfLine(convertHTMLLists (prefix,input.substring(end + HtmlTagEndLength)));
		}
		
		return output;
		
	}
	
	/********************************************************
	 * trim the string first, if it is empty return empty string
	 * otherwise add end of line at the end.
	 * @param input
	 * @return
	 */
	protected String appendEndOfLine(String input)
	{
		if (input.trim().length() ==0 )
			return "";
		else
			return input.trim() + endLine;
	}
	/**************************************************************************
	 * to extract the next html list from the input string.
	 * 
	 * @param input
	 * @return
	 */	
	protected String getHTMLList(String input)
	{
		int tagCount=0;
		String temp=input;
		int index=0;
		String [] tags=new String [] { HtmlOrderListBegin, HtmlOrderListEnd, 
				HtmlUnOrderListBegin, HtmlUnOrderListEnd};
		String firstTag=null;
		
		while (true)
		{
			firstTag=this.getFirstTag(temp, tags);
			if (firstTag == null)
				break;
			if (firstTag.equalsIgnoreCase(HtmlOrderListBegin) ||
					firstTag.equalsIgnoreCase(HtmlUnOrderListBegin))
				tagCount++;
			if (firstTag.equalsIgnoreCase(HtmlOrderListEnd) ||
					firstTag.equalsIgnoreCase(HtmlUnOrderListEnd))
				tagCount--;
			index=index + temp.indexOf(firstTag) + firstTag.length();;
			if (tagCount == 0)
				return input.substring(0, index);
			temp=input.substring(index);
		}
		
		if (tagCount == 0)
			return null;
		
		//there is a good chance that the closing tag is missing. For now, just 
		//take care of the top level
		if (tagCount == 1)
		{
			firstTag=this.getFirstTag(input, tags);
			if (firstTag.equalsIgnoreCase(HtmlOrderListBegin))
				return input + HtmlOrderListEnd;
			if (firstTag.equalsIgnoreCase(HtmlUnOrderListBegin))
				return input + HtmlUnOrderListEnd;
		}
		
		return null;
			
	}
	
	/**********************************************************
	 * A utility method to get the first appearance  of given tags in the 
	 * the input. It returns null none of the given tags exists in the input.
	 * 
	 * @param input
	 * @param tags
	 * @return
	 */
	protected String getFirstTag(String input, String [] tags)
	{
		int length=tags.length;
		int i=0;
		int firstPosition=Integer.MAX_VALUE;
		int index=-1;
		boolean found=false;
		
		while (i<length)
		{
			int position=input.indexOf(tags[i]);
			if (position >=0 && position < firstPosition)
			{
				found=true;
				firstPosition=position;
				index = i;
			}
			i++;
		}
		if (found)
			return tags[index];
		return null;
			
		
	}
	
}

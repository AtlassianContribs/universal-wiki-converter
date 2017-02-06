package com.atlassian.uwc.converters.swiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;


/************************************************************************
 * This class is to convert the html tables to Confluence table syntax. 
 * The first level table will be converted to Confluence table macro. The
 * nested tables will be converted to normal confluence table syntax which uses 
 * | or || to separate the cells. The table has to have </table> end tags. 
 * For others such as tr, th and td, the end tags are optional.
 * 
 * @author bsun
 *
 */
public class HTMLTableConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	static final String TableStart="<table", TableEnd="</table>";
		
	public void convert(Page page) {
		log.debug("Table Converter - starting");
		String input = page.getOriginalText();
		
		String converted = convertTables(input);
		
		log.debug("converted = " + converted);
		page.setConvertedText(converted);
		log.debug("Table Converter - complete");

	}
	
	/**
	 * to convert html tables to confluence table syntax
	 * 
	 * @param input
	 * @return
	 */
	public String convertTables(String input)
	{
		String output=input;
		
		while (true)
		{
			int endIndex=output.indexOf(TableEnd);
			if (endIndex < 0)
				break;
			int startIndex=output.lastIndexOf(TableStart, endIndex);
			if (startIndex < 0)
				break;
			int startIndex2=output.indexOf(TableStart);
			String tableString=output.substring(startIndex , endIndex + TableEnd.length());
			String convertedTableString="";
			if (startIndex2 < startIndex)
				//must be a nested table
				convertedTableString=convertNestedTable(tableString);
			else
				convertedTableString=convertTable(tableString);
			output=output.substring(0, startIndex) + 
				convertedTableString + output.substring(endIndex + TableEnd.length());
			
		}
		
		return output;
	}
	
	/*********************************************
	 * to convert the html table to Confluence table macro
	 * 
	 * @param input
	 * @return
	 */
	public String convertTable(String input)
	{
		String output = input;
		
		Pattern pattern = Pattern.compile("(<table[^>]*>)(.*?)(</table>)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(input);
		
		if (matcher.find())
		{
			String start=convertStartTag("table", matcher.group(1));
			String rows=convertRows(matcher.group(2).trim());
			output=start + rows + "{table}";
			
		}
		
		return output;
	}
	
	/****************************************************
	 * to converted the nested table to normal confluence table
	 * syntax, i.e. the cells separated by | or ||s.
	 * 
	 * @param input
	 * @return
	 */
	public String convertNestedTable(String input)
	{
		String temp = input.replaceAll("<table[^>]*>", "");
		temp=temp.replaceAll("</table>", "");
		String output="";
		String rowString="";
		String endLine = System.getProperty( "line.separator" );

		int index=0;
		while (true)
		{
			//convert row first
			int rowIndex=temp.indexOf("<tr", index);
			if (rowIndex < 0)
				break;
			int emptyRowIndex=temp.indexOf("<tr/>", index);
			if (rowIndex == emptyRowIndex)
			{
				index = emptyRowIndex + "<tr/>".length();
				continue;
			}
						
			int nextRowIndex=temp.indexOf("<tr", rowIndex + 3);
			if (nextRowIndex >=0)
			{
				rowString=temp.substring(rowIndex, nextRowIndex);
				index=nextRowIndex;
			}
			else
			{
				rowString = temp.substring(rowIndex);
				index += rowString.length();
			}
			rowString = rowString.replaceAll("<tr[^>]*>", "");
			rowString=rowString.replaceAll("</tr>", "");
			
			//now convert the cells
			if (rowString.indexOf("<th") >= 0)
			{
				rowString=rowString.replaceAll("<th[^>]*>", "||");
				rowString=rowString.replaceAll("</th>", "");
				output += rowString + "||" + endLine;
			}
			if (rowString.indexOf("<td") >=0 )
			{
				rowString=rowString.replaceAll("<td[^>]*>", "|");
				rowString=rowString.replaceAll("</td>", "");
				output += rowString + "|" + endLine;
			}
		}		
		return output;
	}
		
	/*********************************************************
	 * to convert the start tag (table, tr, th and td) to confluence table macro
	 * start tag
	 * @param attr
	 * @param input
	 * @return
	 */
	protected String convertStartTag(String attr, String input)
	{
		String buffer=input.substring(("<" + attr).length(), input.lastIndexOf('>'));
		if (buffer.trim().length() == 0)
			return "{" + attr + "}";
		String output="{" + attr + ":";
        Pattern p = Pattern.compile(" ");
	    String[] items = p.split(buffer);
	    for (String i : items)
		{
	    	if (i.trim().length() > 0)
	    		output += i + "|";
		}
	    
	    output=output.substring(0, output.length() - 1);
	    output += "}";
	    
	    return output;
	    
	}
	
	/**************************************************************
	 * to convert the table rows to Confluence table rows macro.
	 * The end tags </tr> are optional in the input.
	 * 
	 * @param input
	 * @return
	 */
	public String convertRows(String input)
	{
		String output="";
		String rowString="";
		
		int rowIndex=input.indexOf("<tr");
		int emptyRowIndex=input.indexOf("<tr/>");
		if (rowIndex == emptyRowIndex)
			return "{tr}{tr}";
		
		int nextRowIndex=input.indexOf("<tr", rowIndex + 3);
		if (nextRowIndex >=0)
		{
			rowString=input.substring(rowIndex, nextRowIndex);
			if (rowString.indexOf("</tr>") < 0)
				rowString += "</tr>";
			rowString = convertRow(rowString);
			output=input.substring(0, rowIndex) +
			rowString + convertRows(input.substring(nextRowIndex));
		}
		else
		{
			rowString = input.substring(rowIndex);
			if (rowString.indexOf("</tr>") < 0)
				rowString += "</tr>";
			rowString = convertRow(rowString);	
			output=input.substring(0, rowIndex) + rowString ;
			
		}
		
		return output;
	}
	
	/**************************************************************
	 * to convert the table row to Confluence table row macro. The end 
	 * tag </tr> must exist in the input.
	 * 
	 * @param input
	 * @return
	 */
	public String convertRow(String input)
	{
		Pattern pattern = null;
		Matcher matcher = null;
		String output = "";
		
		pattern = Pattern.compile("(<tr[^>]*>)(.*?)(</tr>)", Pattern.DOTALL);
		matcher = pattern.matcher(input);
		if (matcher.find())
		{
			String start=convertStartTag("tr", matcher.group(1));
			String rows=matcher.group(2);
			if (rows.indexOf("<th") >= 0)
				rows=convertCells("th", matcher.group(2).trim());
			else
				rows=convertCells("td", matcher.group(2).trim());
			String converted=start + rows + "{tr}";
			output += converted;
		}

		if(output.length() == 0)
			return input;
		
		return output;
	}

	/**************************************************************
	 * to convert the table cells to Confluence table cells macro. The
	 * end tags such as </td> and </th> are optional in the input.
	 * 
	 * @param input
	 * @return
	 */
	public String convertCells(String attr, String input)
	{
		Pattern pattern1 = null, pattern2 = null;
		Matcher matcher = null;
		String output = "";
		boolean last=false;
		
		//String regex="(<th[^>]*>)(.*?)((<th[^>]*>)|(</th>))";
		String regex1="(<" + attr + "[^>]*>)(.*?)((<" + attr +  
			"[^>]*>)|(</" + attr + ">))";
		pattern1 = Pattern.compile(regex1, Pattern.DOTALL);
		String regex2="(<" + attr + "[^>]*>)(.*)";
		pattern2 = Pattern.compile(regex2, Pattern.DOTALL);
		String temp=input;
		while(true)
		{
			if (temp.trim().length() == 0)
				break;
			matcher = pattern1.matcher(temp);
			if (matcher.find() == false)
			{
				matcher=pattern2.matcher(temp);
				last=true;
				if (!matcher.find())
					break;
			}
			String start=convertStartTag(attr, matcher.group(1));
			String rows=matcher.group(2).trim();
			String converted=start + rows + "{" + attr + "}";		
			output += converted;
			if (last)
				break;
			String g3=matcher.group(3);
			if (g3.indexOf("<" + attr) >= 0)
				temp=temp.substring(matcher.start(3));
			else
				temp=temp.substring(matcher.end(3));
			
		}
		
		if (output.length() == 0)
			return input;
		
		return output;
		
	}
	
	
}

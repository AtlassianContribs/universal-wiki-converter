package com.atlassian.uwc.filters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocialtextFilter extends TimestampFilter {
	
	public boolean accept(File file) {
		//ignore index files always
		//because it might be a symlink, and if the exported data has been moved off the 
		//original file server it won't be pointing to a valid file
		if ("index.txt".equals(file.getName())) return false; //index.txt is never the right answer
		if (".svn".equals(file.getName())) return false; //avoid svn directories COMMENT
		//make sure that the timestamp filename is acceptable
		boolean mostRecentTimestamp = super.accept(file);
		//don't allow "deleted" files
		String input = read(file);
		boolean isDeleted = isDeleted(input);
		return mostRecentTimestamp && !isDeleted;
	}

	public String read(File file) {
		if (!file.exists() || file.isDirectory()) return "";
		String filestring = "";
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				filestring += line + "\n";
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filestring;
	}
	
	
	Pattern meta = Pattern.compile("^([^\\n: ]+:[^\\n]+\\n)+");
	Pattern deleted = Pattern.compile("Control:\\s*Deleted\n");
	public boolean isDeleted(String input) {
		Matcher metaFinder = meta.matcher(input);
		while (metaFinder.find()) {
			String metadata = metaFinder.group();
			return deleted.matcher(metadata).find();
		}
		return false;
	}

}

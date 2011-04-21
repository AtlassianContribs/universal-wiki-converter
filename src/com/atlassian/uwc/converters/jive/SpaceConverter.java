package com.atlassian.uwc.converters.jive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class SpaceConverter extends BaseConverter {

	private static HashMap<String,String> spacenames;
	private static HashMap<String,String> spacedescs;
	private static HashMap<String, String> spacekeys;
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String input = page.getOriginalText();
		//get container id and type
		ContainerInfo info = getContainerInfo(input);
		//get map
		initContainerMap();
		//get space data
		initSpaceData();		
		
		String spacekey = null;
		if ("2020".equals(info.type)) {
			page.setIsPersonalSpace(true);
			if (info.username == null) {
				info.username = this.spacenames.get(info.getSimpleKey());
			}
			page.setPersonalSpaceUsername(info.username);
			spacekey = "~" + info.username;
		}
		else
			spacekey = getProperties().getProperty(info.getPropKey(), null);
		
		if (spacekey == null) return;
		
		String name = this.spacenames.get(info.getSimpleKey());
		String desc = this.spacedescs.get(info.getSimpleKey());
		
		if (name != null || desc != null) {
			page.setSpace(spacekey, name, desc);
		}
		else page.setSpacekey(spacekey);
	}

	Pattern jivemeta = Pattern.compile("\\{jive-export-meta:([^}]+)\\}");
	Pattern idPattern = Pattern.compile("containerid=(\\d+)");
	Pattern typePattern = Pattern.compile("containertype=(\\d+)");
	Pattern usernamePattern = Pattern.compile("usercontainername=(\\w+)");
	private ContainerInfo getContainerInfo(String input) {
		Matcher jivemetaFinder = jivemeta.matcher(input);
		ContainerInfo info = new ContainerInfo();
		if (jivemetaFinder.find()) {
			String params = jivemetaFinder.group(1);
			Matcher idFinder = idPattern.matcher(params);
			String id = (idFinder.find())?idFinder.group(1):null;
			Matcher typeFinder = typePattern.matcher(params);
			String type = (typeFinder.find())?typeFinder.group(1):null;
			Matcher userFinder = usernamePattern.matcher(params);
			String username = (userFinder.find())?userFinder.group(1):null;
			info.id = id;
			info.type = type;
			info.username = username;
			return info;
		}
		return null;
	}

	private void initContainerMap() {
		if (spacekeys == null) {
			spacekeys = new HashMap<String, String>();
			for (String key : getProperties().stringPropertyNames()) {
				if (key.startsWith("spacemap-")) {
					String id = key.replaceFirst("spacemap-", "");
					String spacekey = getProperties().getProperty(key, null);
					spacekeys.put(id, spacekey);
				}
			}
		}
	}
	
	public static HashMap<String, String> getSpacekeys() {
		return spacekeys;
	}
	
	public static void setSpacekeys(HashMap<String,String> keys) { //used by unit tests
		spacekeys = keys;
	}

	Pattern containerdata = Pattern.compile("(?<=^|\n)" +
			"([^\t]*)\t([^\t]*)\t([^\t]*)\t" +
			"([^\t]*)\t([^\t]*)\t([^\t]*)\t" +
			"([^\n]*)" +
			"");
	private void initSpaceData() {
		if (spacenames == null) {
			spacenames = new HashMap<String, String>();
			spacedescs = new HashMap<String, String>();
			String path = getProperties().getProperty("spacedata", null);
			if (path == null) {
				log.debug("No path for spacedata property");
				return;
			}
			path = path.trim();
			String filestring = "";
			String line;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(path));
				while ((line = reader.readLine()) != null) {
					filestring += line + "\n";
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Matcher dataFinder = containerdata.matcher(filestring);
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			while (dataFinder.find()) {
				found = true;
				String id = dataFinder.group(1);
				String type = dataFinder.group(2);
				String name = dataFinder.group(4);
				String desc = dataFinder.group(5);
				if (!id.matches("\\d+")) continue;
				spacenames.put(id+"-"+type, name);
				spacedescs.put(id+"-"+type, desc);
//				log.debug("Init Spacedata: key="+ id+"-"+type+ " name=" + name + " desc="+desc);
			}
			if (!found) {
				log.error("No data found in spacedata file.");
			}
		}
	}
	/**
	 * return the spacename for the given key , or null if it does not exist
	 * or if the spacenames map does not exist
	 * @param simplekey id + "-" + type
	 * @return
	 */
	public static String getSpacename(String simplekey) {
		if (spacenames != null) return spacenames.get(simplekey);
		return null;
	}

	public class ContainerInfo {
		public String id;
		public String type;
		public String username;
		
		public String getPropKey() {
			return "spacemap-" + getSimpleKey();
		}
		
		public String getSimpleKey() {
			return id + "-" + type;
		}
	}

}

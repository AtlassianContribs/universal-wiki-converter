package com.atlassian.uwc.util.test;
/**
 * Created by IntelliJ IDEA.
 * User: brendan
 * Date: Apr 19, 2006
 * Time: 5:16:27 PM
 * To change this template use File | Settings | File Templates.
 */

import junit.framework.*;
import com.atlassian.uwc.util.PropertyFileManager;

import java.util.Map;

public class PropertyFileManagerTest extends TestCase {
    PropertyFileManager propertyFileManager;

    /**
     * Just reads in and writes out the file. Not much of a unit test at
     * all, but handy for hooking up a debugger and inspecting.
     * @throws Exception
     */
    public void testLoadPropertiesFile() throws Exception {
        Map props = PropertyFileManager.loadPropertiesFile("conf/converter.properties");
        props.toString();
        PropertyFileManager.storePropertiesFile(props, "conf/test-out.properties");
    }
}
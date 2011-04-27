Testing the Filter Framework.

Filters Framework doc here: 
http://confluence.atlassian.com/display/CONFEXT/UWC+Filters+Framework

There are 3 types of filters: class, endswith, legacy.

h1. Tests for each type

h2. Class Filter
If the value of the filter property is a class implementing java.io.Filter, then that FileFilter will be used to filter the pages. 
h3. TEST
./run_cmd_devel.sh ../../sampleData/filter/confluenceSettings.properties ../../sampleData/filter/converter.test1.properties  | egrep Filtering

h3. EXPECTED RESULT
If the filter framework is working properly, then the xml file will be excluded from upload.
Conversion status should be SUCCESS.
If you include the pipe egrep described above, results should look like: 
2011-04-27 14:11:56,083 DEBUG [main] - Filtering out filename: SampleFilter-InputEndswith.xml

h2. Endswith Filter
If the value of the filter property is a text string that is not a class implementing java.io.Filter, then it will be treated as an Endswith Filter.
Only files that end with that string will be converted.
h3. TEST
1. ./run_cmd_devel.sh ../../sampleData/filter/confluenceSettings.properties ../../sampleData/filter/converter.test2.properties  | egrep Filtering

h3. EXPECTED RESULT:
If the filter framework is working properly, then only the txt file will be uploaded.
Conversion status should be SUCCESS.
If you include the pipe egrep described above, results should look like:
2011-04-27 14:13:56,113 DEBUG [main] - Filtering out filename: SampleFilter-InputEndswith.txt,v
2011-04-27 14:13:56,113 DEBUG [main] - Filtering out filename: SampleFilter-InputEndswith.xml

h2. Legacy Filter
There is an old way to set the Endswith filter property that is not documented, but we support it for legacy purposes.
If the confluenceSettings.properties pattern property is set, then it is treated as endswith filter.

h3. TEST
1. edit the conf/confluenceSettings.properties
2. change the pattern property, so that it's value is .txt
3. restart the uwc
4. run the nosyntaxconversions type converter on the SampleFilter-InputXXX files in this directory

h3. EXPECTED RESULT:
If the filter framework is working properly, then only the txt file will be uploaded.
Conversion status should be SUCCESS.

h2. Multiple Filters

You can have multiple filters. Only pages that all class filters accept as long as any endswith filter accepts as well will be included. (Class filters are ANDed. Endswith filters are ORed.)

h3. TEST
./run_cmd_devel.sh ../../sampleData/filter/multtest.properties ../../sampleData/filter/converter.test3.properties  | egrep Attempting

h3. EXPECTED RESULT:
If the filter framework is working properly, then only the txt file (not the xml, jpg, or .svn directory files) will be included.
Conversion status should be included.
If using the egrep Attempting pipe above, results should look like:
2011-04-27 14:22:17,105 DEBUG [main] - Attempting to send page: foo.txt to space: uwctest

h1. Error Handling Tests

h2. Non-existant Class
If the property is of a class that does not exist, then the filters framework will try to treat it as an endswith property. Unless the page in question happens to end with the fully qualified (non-existant) class name, this will result in all pages being disqualified from upload. 

h3. TEST
1. Add the following property to a testfilter converter properties class
MyWiki.0001.somedescription.filter=com.atlassian.uwc.filters.XWikiFilter222
2. run the test filter converter on the SampleFilter-InputXXX files in this directory.

h3. EXPECTED RESULT:
The following error will be displayed:
CONVERTER_ERROR All pages submitted were disqualified for various reasons. Could not complete conversion.
No pages will be uploaded.
The Conversion status will be ENCOUNTERED ERRORS.

h2. Existing class that is not a file filter
If the property is of an existing class that does not implement java.io.FileFilter, then it will be treated as an endswith filter, and one will CONVERTER_ERRORS.

h3. TEST
1. Add the following property to a testfilter converter properties file:
MyWiki.0001.somedescription.filter=com.atlassian.uwc.ui.Page
2. Run the testfilter converter on the SampleFilter-InputXXX files in this directory.

h3. EXPECTED RESULT
There will be a converter error:
CONVERTER_ERROR All pages submitted were disqualified for various reasons. Could not complete conversion.
The conversion status will be ENCOUNTERED ERRORS 


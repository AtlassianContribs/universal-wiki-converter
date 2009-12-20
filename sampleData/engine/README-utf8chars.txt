To test that utf8 characters in pagenames are being properly migrated:
1. Go to http://babelfish.yahoo.com/ or something translation page
2. translate 'testing' or something like that into Korean characters
3. Create a file and copy the korean chars into the filename somewhere. End the filename with the extension '.uwc'.
4. Run the uwc on this file with the moinmoin converter (which does not request url decoding, but needs the .uwc extension).
5. Examine the resulting Confluence page and verify that the korean characters were preserved in the page title

To test that the encoding framework works, 
1. cp converter.testencoding.properties to target/uwc/conf/
2. edit it, and set the value of the encoding property to Cp1252.
3. Run the UWC on encoding/euro-cp1252.txt with upload turned on
4. Examine the page in confluence. A Euro symbol should be visible.
5. edit the properties file, and set the value of the encoding property to iso-8859-1.
6. Run the UWC on encoding/fraction-8859-1.txt with upload turned on
7. Examine the page in confluence. A fraction symbol should be visible.
8. Edit the properties file, and set the value of the encoding property to iso-8859-15.
9. Run the UWC on encoding/euro-8859-15.txt with upload turned on.
10. Examine the page in confluence. A Euro symbol should be visible.
These are the most likely encodings to have problems, and these tests are of characters that do not overlap with utf-8.
11. Finally, as a caution, the file deriv-oracle-cesu8.txt uses the broken character encoding cesu-8, which is not going to be fixable for several reasons. We have this test file as an example of what happens when the UWC tries to handle a broken character encoding (which is to say, odd errors from the CRJW that cuase upload to fail).

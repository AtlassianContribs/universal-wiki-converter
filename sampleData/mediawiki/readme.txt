There are three test sets here:

1. Basic Tests
$shell$ ./testall
everything should pass (filename listed without diff) except optional converters

2. Optional Converters
Turn on the relevant properties for the optional converters and test to see if those files pass in those situations. Do not be alarmed if some of the Basic Tests fail as a result. Optional converters sometimes have mutually exclusive expected outputs for the same input.
Note: The HtmlInvalid test will cause converter errors unless it's options have been turned on.

3. Page title encodings.
The mediawiki exporter should url encode any complicated characters in the filenames. 
Run the converter on SampleMediawiki-InputEncoded%C3%A5%C3%A4%C3%B6.txt and check the uploaded page to see that the characters have been properly decoded.

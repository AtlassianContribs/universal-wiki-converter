Test Notes

* In order for the AttSpace test to work, you must have a directory in the attachment directory that matches the following branch:
Ed:~/attachmentdirectory$ find SampleJspwiki-InputAttSpace.txt-att/
SampleJspwiki-InputAttSpace.txt-att/
SampleJspwiki-InputAttSpace.txt-att//foo+bar.pdf-dir
SampleJspwiki-InputAttSpace.txt-att//foo+bar.pdf-dir/1.pdf

* To test uwc-330
uncomment 1020-margin

* To test uwc-335, pages with unreferred to images attached, examine SampleJspwiki-InputAttNoRef in Confluence after it has been converted and check to see if an image got attached

* To test uwc-349 
1. Set Jspwiki.0200.jspwiki-pagedir.property=/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki
2. in addition to comparing the syntax results of SampleJspwiki-Input+WithSpaces.txt and SampleJspwiki-Input.WithDots.txt, you must also:
-- upload the former to Confluence
-- make sure the pagename is transformed to "SampleJspwiki-Input WithSpaces"

* To test uwc-354
Uncomment 302 properties

* Test uwc-383
Make sure SampleJspwiki-Input.WithDots page has attachments after upload

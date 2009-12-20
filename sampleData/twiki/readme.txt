Info about steps to take for non standard tests

1. Attachments2
To test that attachments actually are found and attached.
Run the SampleTwiki-InputAttachments2.txt 
with twiki wikitype 
and attachments directory set to
/Users/laura/Code/Subversion/uwc-current/devel/sampleData/twiki/pub
and uploads turned on

The  page in Confluence should have 1 attachment (cow.jpg) and it should be rendered in the page.

Extra step: You might want to delete the page on the Confluence first, or at least the attachment from that page.

Note: the 'twiki' in the pub directory is because the way that attachments figure out the directory structure is that it looks at the parent directory of the page to determine the 'web'. In this artificial case, that would be the work 'twiki'.

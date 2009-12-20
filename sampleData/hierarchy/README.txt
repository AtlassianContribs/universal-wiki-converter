To test hierarchies, use the following procedures.

1. Basic Hierarchy,
use filetype testHierarchy
set pages to be the "basic" directory
make sure the space doesn't have any of the pages listed in the basic directory
run the converter, examine the space tree view, and look for the hierarchy to match the directory structure of the basic directory

2. case sensitive pages
use filetype testHierarchy
set pages to be the case-sensitivity directory
run the converter 
This test should create NAMESPACE COLLISION errors.
.svn files can interfere with this test, so add the following filter for this particular test:
test.1234.thistest.filter=oo.txt
or
test.1234.thistest.filter=com.atlassian.uwc.filters.NoSvnFilter


3. dots
use filetype testHierarchy
set pages to be the "dots" directory
make sure the space doesn't have any of the pages listed in the basic direcotyr already.
run the converter, examine the space tree view, and make sure the pagenames and directory names are maintained like so:
dots
--- A
------ 9.0-test
------ Foo
--- B
------ Bar

4. plusspace
change the title handling from 
TestHierarchy.1000-remove-extension.class=com.atlassian.uwc.converters.ChopPageExtensionsConverter
to
TestHierarchy.1000-testplus.class=com.atlassian.uwc.converters.jotspot.TitleConverter
use filetype testHierarchy
set pages to be the "plusspace" directory
run the converter, examine the space tree view, and make sure you have only two additional pages: 
--- has space
------ child
What we're trying to test is that we don't end up with 
--- has+space
------- child
--- has space
Basically, we're testing that the page titles framework and the hierarchy framework don't ignore each other.

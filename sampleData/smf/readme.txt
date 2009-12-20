Instructions for tests that are not junit or basic comparison.

1. SampleSmf-InputTitleIll_top2.txt
-- Run the converter on this file, upload to Confluence. The pagename should start with "Bad characters in title", and should not have any illegal characters in the title

2. SampleSmf-InputTitleEntities_top3.txt
-- Similarly. The pagename should be: Bad characters in title  and some quotes "

3. SampleSmf-InputTitleCollision_top4.txt
-- Similarly. This page and #1 should be get differentiated as different pages.

4. SampleSmf-InputAttach_re16.txt
-- Set the attachments directory
-- Run the converter on this file and upload to COnfluence
-- The file should have two attachments. And inline references to them should resolve.

5. JUnit tests
So, many of the exporter's tests directly query the database, and are assuming certain data exists in a database, and that there are correct connection settings.
To get those tests to work, you'll need to:
A) Restore the database
B) Install an SMF (Version 1.1.9 or you'll never get this working) and somehow hook it up to the existing database. (This is a pain!)
C) Create a series of properties files with the expected SQL to perform the correct tests. More on this below.

Step A - Restore the database
I have committed a mysql dump of my test database. It's at location:
sampleData/smf/junit_resources/TestSMF.sql
To restore, do something like this:
shell> mysql TestSMF < TestSMF.sql
You can learn more about database restores here:
http://dev.mysql.com/doc/refman/5.1/en/mysqldump.html
and here:
http://www.devshed.com/c/a/MySQL/Backing-up-and-restoring-your-MySQL-Database/
You will probably need to create a database admin account and give it full grants to the restored database.

Step B - Install an SMF, Version 1.1.9 and hook it up to the database.
Simple Machines Downloads are here:
http://download.simplemachines.org/

Unfortunately, hooking it up to the existing database isn't as easy as just changing the db settings in Settings.php, because certain settings are maintained in  the database.  Try the following:
1) Install the SMF and point its database settings someplace unimportant. It will create its own tables, so you don't want to aim it at the restored database yet.
2) Use repair_settings.php to point the SMF at the right location:
http://docs.simplemachines.org/index.php?topic=663
3) If you want to interact with the SMF in it's webapp territory, you may need to update the admin acct's password so you can log into your restored SMF. (NOTE: This is different from the database admin's account, which you should already know the password to, and should already have been given full grants to the database.) If this seems dodgy, it is, but a quick and dirty method is to create a non-admin account using the usual SMF registration method, and then just make the admin's password the same md5 hash as the new account using an SQL Update statement, and voila, now you know the password. Tricksy, but effective. You shouldn't need this to run the junit tests. You should only need this if you want to interact with the TestSMF as a web app.

Step C - Add the right "properties" methods with the right settings to keep the junit tests happy. You need:

test.attachments.properties     test.otheratt.properties
test.basic.properties           test.output.properties
test.hashsqlfalse.properties    test.childboards.properties
test.gchildboards.properties

I will include the contents of the properties files (minus the database settings which you need to provide) here:
*************************
test.attachments.properties

## Exporter settings for the SMF Exporter

## Class that handles the export - Don't change this
exporter.class=com.atlassian.uwc.exporters.SMFExporter

## Database settings - Set These to your database settings.
db.url=jdbc:mysql://localhost:3306
db.name=TestSMF
db.user=
db.pass=
## Your jdbc driver class. 
## If you're using something other than Mysql:
## -- Set this to the correct class, and
## -- add the driver jar to the lib directory before starting the UWC
jdbc.driver.class=com.mysql.jdbc.Driver

## Output dir - Set this to wherever you want the export to go
output.dir=/Users/laura/tmp

## SQL - Set these as necessary
db.encoding=utf-8

db.col.cat.id=ID_CAT
db.col.cat.name=name
db.table.cat=smf2_categories
db.sql.cat=select {db.col.cat.id},{db.col.cat.name} from {db.table.cat} LIMIT 1;

db.col.board.id=ID_BOARD
db.col.board.cat=ID_CAT
db.col.board.level=childLevel
db.col.board.parent=ID_PARENT
db.col.board.parenttype=parenttype
db.col.board.name=name
db.col.board.desc=description
db.table.board=smf2_boards
db.sql.board=select {db.col.board.id},{db.col.board.cat},{db.col.board.level},{db.col.board.parent},{db.col.board.name},{db.col.board.desc},(case when {db.col.board.parent}='0' then 'cat' else 'brd' end) as {db.col.board.parenttype} from {db.table.board} LIMIT 1;

db.col.msg.id=m.id_msg
db.col.msg.topic=m.id_topic
db.col.msg.board=m.id_board
db.col.msg.userid=m.id_member
db.col.msg.username=m.posterName
db.col.msg.useremail=m.posterEmail
db.col.msg.time=m.posterTime
db.col.msg.title=m.subject
db.col.msg.content=m.body
db.col.msg.isfirst=is_first
db.col.msg.firstid=t.id_first_msg
db.table.msg=smf2_messages
db.sql.messages=select {db.col.msg.id},{db.col.msg.topic},{db.col.msg.board},{db.col.msg.userid},{db.col.msg.username},{db.col.msg.useremail},{db.col.msg.time},{db.col.msg.title},{db.col.msg.content},{db.col.msg.firstid},(case when m.id_msg=t.id_first_msg then 'true' else 'false' end) as {db.col.msg.isfirst} from {db.table.msg} as m left join smf2_topics as t on (m.id_topic = t.id_topic) where m.id_msg=16;

db.col.att.id=ID_ATTACH
db.col.att.thumb=ID_THUMB
db.col.att.message=ID_MSG
db.col.att.name=filename
db.col.att.hash=file_hash
db.table.att=smf2_attachments
db.sql.att=select {db.col.att.id},{db.col.att.thumb},{db.col.att.message},{db.col.att.name},{db.col.att.hash} from {db.table.att} where ID_MSG=16;
## If the filehash is a column in the attachments table, set this to true.
## If set to false, we'll use unsalted md5 to extrapolate the filename
db.att.hashInSQL=true

*************************
test.basic.properties

## Exporter settings for the SMF Exporter

## Class that handles the export - Don't change this
exporter.class=com.atlassian.uwc.exporters.SMFExporter

## Database settings - Set These to your database settings.
db.url=jdbc:mysql://localhost:3306
db.name=TestSMF
db.user=
db.pass=
## Your jdbc driver class. 
## If you're using something other than Mysql:
## -- Set this to the correct class, and
## -- add the driver jar to the lib directory before starting the UWC
jdbc.driver.class=com.mysql.jdbc.Driver

## Output dir - Set this to wherever you want the export to go
output.dir=/Users/laura/tmp

## SQL - Set these as necessary
db.encoding=utf-8

db.col.cat.id=ID_CAT
db.col.cat.name=name
db.table.cat=smf2_categories
db.sql.cat=select {db.col.cat.id},{db.col.cat.name} from {db.table.cat};

db.col.board.id=ID_BOARD
db.col.board.cat=ID_CAT
db.col.board.level=childLevel
db.col.board.parent=ID_PARENT
db.col.board.parenttype=parenttype
db.col.board.name=name
db.col.board.desc=description
db.table.board=smf2_boards
db.sql.board=select {db.col.board.id},{db.col.board.cat},{db.col.board.level},{db.col.board.parent},{db.col.board.name},{db.col.board.desc},(case when {db.col.board.parent}='0' then 'cat' else 'brd' end) as {db.col.board.parenttype} from {db.table.board};

db.col.msg.id=m.id_msg
db.col.msg.topic=m.id_topic
db.col.msg.board=m.id_board
db.col.msg.userid=m.id_member
db.col.msg.username=m.posterName
db.col.msg.useremail=m.posterEmail
db.col.msg.time=m.posterTime
db.col.msg.title=m.subject
db.col.msg.content=m.body
db.col.msg.isfirst=is_first
db.col.msg.firstid=t.id_first_msg
db.table.msg=smf2_messages
db.sql.messages=select {db.col.msg.id},{db.col.msg.topic},{db.col.msg.board},{db.col.msg.userid},{db.col.msg.username},{db.col.msg.useremail},{db.col.msg.time},{db.col.msg.title},{db.col.msg.content},{db.col.msg.firstid},(case when m.id_msg=t.id_first_msg then 'true' else 'false' end) as {db.col.msg.isfirst} from {db.table.msg} as m left join smf2_topics as t on (m.id_topic = t.id_topic);

db.col.att.id=ID_ATTACH
db.col.att.thumb=ID_THUMB
db.col.att.message=ID_MSG
db.col.att.name=filename
db.col.att.hash=file_hash
db.table.att=smf2_attachments
db.sql.att=select {db.col.att.id},{db.col.att.thumb},{db.col.att.message},{db.col.att.name},{db.col.att.hash} from {db.table.att};

## If the filehash is a column in the attachments table, set this to true.
## If set to false, we'll use unsalted md5 to extrapolate the filename
db.att.hashInSQL=true

*************************
test.hashsqlfalse.properties

## Exporter settings for the SMF Exporter

## Class that handles the export - Don't change this
exporter.class=com.atlassian.uwc.exporters.SMFExporter

## Database settings - Set These to your database settings.
db.url=jdbc:mysql://localhost:3306
db.name=TestSMF
db.user=
db.pass=
## Your jdbc driver class. 
## If you're using something other than Mysql:
## -- Set this to the correct class, and
## -- add the driver jar to the lib directory before starting the UWC
jdbc.driver.class=com.mysql.jdbc.Driver

## Output dir - Set this to wherever you want the export to go
output.dir=/Users/laura/tmp

## Control SMF attachment char handling
attachment-chars-remove=,[]:

## SQL - Set these as necessary
db.encoding=utf-8

db.col.cat.id=ID_CAT
db.col.cat.name=name
db.table.cat=smf2_categories
db.sql.cat=select {db.col.cat.id},{db.col.cat.name} from {db.table.cat};

db.col.board.id=ID_BOARD
db.col.board.cat=ID_CAT
db.col.board.level=childLevel
db.col.board.parent=ID_PARENT
db.col.board.parenttype=parenttype
db.col.board.name=name
db.col.board.desc=description
db.table.board=smf2_boards
db.sql.board=select {db.col.board.id},{db.col.board.cat},{db.col.board.level},{db.col.board.parent},{db.col.board.name},{db.col.board.desc},(case when {db.col.board.parent}='0' then 'cat' else 'brd' end) as {db.col.board.parenttype} from {db.table.board};

db.col.msg.id=m.id_msg
db.col.msg.topic=m.id_topic
db.col.msg.board=m.id_board
db.col.msg.userid=m.id_member
db.col.msg.username=m.posterName
db.col.msg.useremail=m.posterEmail
db.col.msg.time=m.posterTime
db.col.msg.title=m.subject
db.col.msg.content=m.body
db.col.msg.isfirst=is_first
db.col.msg.firstid=t.id_first_msg
db.table.msg=smf2_messages
db.sql.messages=select {db.col.msg.id},{db.col.msg.topic},{db.col.msg.board},{db.col.msg.userid},{db.col.msg.username},{db.col.msg.useremail},{db.col.msg.time},{db.col.msg.title},{db.col.msg.content},{db.col.msg.firstid},(case when m.id_msg=t.id_first_msg then 'true' else 'false' end) as {db.col.msg.isfirst} from {db.table.msg} as m left join smf2_topics as t on (m.id_topic = t.id_topic);

db.col.att.id=ID_ATTACH
db.col.att.thumb=ID_THUMB
db.col.att.message=ID_MSG
db.col.att.name=filename
db.col.att.hash=file_hash
db.table.att=smf2_attachments
db.sql.attachments=select {db.col.att.id},{db.col.att.thumb},{db.col.att.message},{db.col.att.name},{db.col.att.hash} from {db.table.att} where ID_MSG=16;
## If the filehash is a column in the attachments table, set this to true.
## If set to false, we'll use unsalted md5 to extrapolate the filename
db.att.hashInSQL=false

*************************
test.otheratt.properties

## Exporter settings for the SMF Exporter

## Class that handles the export - Don't change this
exporter.class=com.atlassian.uwc.exporters.SMFExporter

## Database settings - Set These to your database settings.
db.url=jdbc:mysql://localhost:3306
db.name=TestSMF
db.user=
db.pass=
## Your jdbc driver class. 
## If you're using something other than Mysql:
## -- Set this to the correct class, and
## -- add the driver jar to the lib directory before starting the UWC
jdbc.driver.class=com.mysql.jdbc.Driver

## Output dir - Set this to wherever you want the export to go
output.dir=/Users/laura/tmp

## SQL - Set these as necessary
db.encoding=utf-8

db.col.cat.id=ID_CAT
db.col.cat.name=name
db.table.cat=smf2_categories
db.sql.cat=select {db.col.cat.id},{db.col.cat.name} from {db.table.cat};

db.col.board.id=ID_BOARD
db.col.board.cat=ID_CAT
db.col.board.level=childLevel
db.col.board.parent=ID_PARENT
db.col.board.parenttype=parenttype
db.col.board.name=name
db.col.board.desc=description
db.table.board=smf2_boards
db.sql.board=select {db.col.board.id},{db.col.board.cat},{db.col.board.level},{db.col.board.parent},{db.col.board.name},{db.col.board.desc},(case when {db.col.board.parent}='0' then 'cat' else 'brd' end) as {db.col.board.parenttype} from {db.table.board};

db.col.msg.id=m.id_msg
db.col.msg.topic=m.id_topic
db.col.msg.board=m.id_board
db.col.msg.userid=m.id_member
db.col.msg.username=m.posterName
db.col.msg.useremail=m.posterEmail
db.col.msg.time=m.posterTime
db.col.msg.title=m.subject
db.col.msg.content=m.body
db.col.msg.isfirst=is_first
db.col.msg.firstid=t.id_first_msg
db.table.msg=smf2_messages
db.sql.messages=select {db.col.msg.id},{db.col.msg.topic},{db.col.msg.board},{db.col.msg.userid},{db.col.msg.username},{db.col.msg.useremail},{db.col.msg.time},{db.col.msg.title},{db.col.msg.content},{db.col.msg.firstid},(case when m.id_msg=t.id_first_msg then 'true' else 'false' end) as {db.col.msg.isfirst} from {db.table.msg} as m left join smf2_topics as t on (m.id_topic = t.id_topic) where m.id_msg=22;

db.col.att.id=ID_ATTACH
db.col.att.thumb=ID_THUMB
db.col.att.message=ID_MSG
db.col.att.name=filename
db.col.att.hash=file_hash
db.table.att=smf2_attachments
db.sql.att=select {db.col.att.id},{db.col.att.thumb},{db.col.att.message},{db.col.att.name},{db.col.att.hash} from {db.table.att};
## If the filehash is a column in the attachments table, set this to true.
## If set to false, we'll use unsalted md5 to extrapolate the filename
db.att.hashInSQL=true

*************************
test.output.properties

## Exporter settings for the SMF Exporter

## Class that handles the export - Don't change this
exporter.class=com.atlassian.uwc.exporters.SMFExporter

## Database settings - Set These to your database settings.
db.url=jdbc:mysql://localhost:3306
db.name=TestSMF
db.user=
db.pass=
## Your jdbc driver class. 
## If you're using something other than Mysql:
## -- Set this to the correct class, and
## -- add the driver jar to the lib directory before starting the UWC
jdbc.driver.class=com.mysql.jdbc.Driver

## Output dir - Set this to wherever you want the export to go
output.dir=/Users/laura/tmp

## SQL - Set these as necessary
db.encoding=utf-8

db.col.cat.id=ID_CAT
db.col.cat.name=name
db.table.cat=smf2_categories
db.sql.cat=select {db.col.cat.id},{db.col.cat.name} from {db.table.cat} LIMIT 1;

db.col.board.id=ID_BOARD
db.col.board.cat=ID_CAT
db.col.board.level=childLevel
db.col.board.parent=ID_PARENT
db.col.board.parenttype=parenttype
db.col.board.name=name
db.col.board.desc=description
db.table.board=smf2_boards
db.sql.board=select {db.col.board.id},{db.col.board.cat},{db.col.board.level},{db.col.board.parent},{db.col.board.name},{db.col.board.desc},(case when {db.col.board.parent}='0' then 'cat' else 'brd' end) as {db.col.board.parenttype} from {db.table.board} LIMIT 1;

db.col.msg.id=m.id_msg
db.col.msg.topic=m.id_topic
db.col.msg.board=m.id_board
db.col.msg.userid=m.id_member
db.col.msg.username=m.posterName
db.col.msg.useremail=m.posterEmail
db.col.msg.time=m.posterTime
db.col.msg.title=m.subject
db.col.msg.content=m.body
db.col.msg.isfirst=is_first
db.col.msg.firstid=t.id_first_msg
db.table.msg=smf2_messages
db.sql.messages=select {db.col.msg.id},{db.col.msg.topic},{db.col.msg.board},{db.col.msg.userid},{db.col.msg.username},{db.col.msg.useremail},{db.col.msg.time},{db.col.msg.title},{db.col.msg.content},{db.col.msg.firstid},(case when m.id_msg=t.id_first_msg then 'true' else 'false' end) as {db.col.msg.isfirst} from {db.table.msg} as m left join smf2_topics as t on (m.id_topic = t.id_topic) LIMIT 1;

db.col.att.id=ID_ATTACH
db.col.att.thumb=ID_THUMB
db.col.att.message=ID_MSG
db.col.att.name=filename
db.col.att.hash=file_hash
db.table.att=smf2_attachments
db.sql.att=select {db.col.att.id},{db.col.att.thumb},{db.col.att.message},{db.col.att.name},{db.col.att.hash} from {db.table.att} LIMIT 1;

## If the filehash is a column in the attachments table, set this to true.
## If set to false, we'll use unsalted md5 to extrapolate the filename
db.att.hashInSQL=true

*************************
test.childboards.properties
## childboards is the same as output except that 
## (1) the boards SQL is 
## returning properties related to child boards with same names, and their 
## parent board
## so instead of LIMIT 1, there's a where clause:
## where name="BoardSameName" or id_board="2"
## (2) the message SQL is
## returning properties related to topics of those child boards, so instead
## of LIMIT 1, there's a where clause using the specific topic id:
## where id_msg=28;
## NOTE: ids are used in the junit tests explicitly, so you may need to update
## unit tests if you change the ids


*************************
test.gchildboards.properties
## gchildboards is the same as childboards except that
## (1) boards SQL is
## returning info about a grandchild board and its ancestors. The where clause:
## where id_board="2" or id_board="3" or id_board="7"
## (2) the messages SQL is
## returning info about a grandchildboard topic
## where id_msg=31;
## NOTE: ids are used in the junit tests explicitly, so you may need to update
## unit tests if you change the ids


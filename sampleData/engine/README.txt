The following files are necessary to get ConverterEngineTest.java to work:
* test.basic.properties
* test.ssl.properties
* test.orphan.properties
* test.ssl.badtrust.properties
* test.ssl.badtrust2.properties
* test.ssl.trustall.properties
* test.ssl.notrust.properties
* test.comment.properties
* test.autodetect.properties
* test.autodetect-perms.properties

test.basic.properties
must contain settings the same as conf/confluenceSettings.properties such that
the user has access to a running confluence server, and can add comments to
the specified space.

test.ssl.properties
must contain settings the same as conf/confluenceSettings.properties such that the user has access to a running SSL protected confluence server. Remember to explicitly set the truststore location and associated password.

test.orphan.properties
must contain settings the same as conf/confluenceSettings.properties such that the user has access to a running confluence server, and: 
1. uploadOrphanAttachments setting must be set to true
2. attachments setting must be set to a directory with attachments you're willing to upload.
3. url must include protocol (For example: http://localhost)
4. You have to manually check that the Orphan attachments page has been created and the attachments that should be there are there.

* test.ssl.badtrust.properties
must contain settings the same as conf/confluenceSettings.properties such that the truststore file exists, but is not a valid truststore format.

* test.ssl.badtrust2.properties
must contain settings the same as conf/confluenceSettings.properties such that the truststore file is assigned but does not exist

* test.ssl.trustall.properties
must contain settings the same as conf/confluenceSettings.properties such that truststore and/or trustpass are not valid, and trustall is set to true

* test.ssl.notrust.properties
must contain settings the same as conf/confluenceSettings.properties such that none of the trust properties (truststore, trustpass, trustall) are set

* test.comment.properties
must contain settings the same as conf/confluenceSettings.properties such that
the user does not have privileges for adding comments

* test.autodetect.properties
must contain settings the same as test.basic.properties without a space setting

* test.autodetect-perms.properties
must contain settings the same as test.autodetect.properties but with login credentials that work, but do not have space creation permissions

******************

To test that the movepage method is working.
1. cp sampleData/engine/converter.testmove.properties to target/uwc/conf
2. Run the uwc on the sampleData/hiearchy/basic directory, using the movepage converter.
3. Examine results. No hierarchy should be in evidence.
4. change the type to testHierarchy, and rerun conversion.
5. Examine results. The pages should have changed positions to reflect the proper hierarchy.

1. Test Contents
Sharepoint tests are dependent on a particular sharepoint. At this time, the sharepoint exporter/converter project does not have a sharepoint to test against, which means that content dependent tests will fail with connection settings errors.

2. junit_resources. 
In order for certain junit tests to work, please add a directory structure to this directory:
junit_resources/
junit_resources//sharepointexporter-test-error
junit_resources//sharepointexporter-test-error2
junit_resources//sharepointexporter-test-error2/abc
The sharepointexporter-test-error and sharepointexporter-test-error2 directories should not be writable by the current user. I suggest making them owned by another user.


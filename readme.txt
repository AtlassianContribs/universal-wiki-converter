Universal Wiki Converter readme.txt file

To build the UWC use ANT (http://ant.apache.org/):
* cd devel (the devel dir.)
* ant      (the default target will build the UWC under 

To run the newly built UWC
* cd target/uwc/
* chmod a+x *sh
* ./run_uwc_devel.sh

More details and documentation is here: https://migrations.atlassian.net/wiki/display/UWC/Universal+Wiki+Converter

Note: you do not need to build the UWC to run it, just if you're doing development work on it. 

==

This code is open source and is up to date with Atlassian's latest storage format of Atlassian Confluence (introduced in Confluence 4). We successfully user/run the UWC for Confluence 5.X releases, however, there are *many* flavors and versions of MIGRATE_FROM wikis. 

As such, we like to say this is a tool, not always the end-to-end solution or silver bullet. Wiki formats are 
varied - and so please understand that the UWC will get you further along- but there could be some post-processing or scripts or other things to also assist in the process. 

We do provide ongoing small and big support for migrations, depending on needs. Email us at info@appfusions.com and let us know what you are trying to do and we can see if we can help you!  We have many client references too.

Please also refer to the Wiki Migration Checklist (http://www.appfusions.com/display/Dashboard/Wiki+Migration+Checklist) to educate yourself on what is invoived in a migration. The checklist is not to suggest that all content elements are problematic. They aren't. But some are, and not always the same between different flavors of wikis that are being migrated from. 


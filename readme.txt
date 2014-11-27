Universal Wiki Converter readme.txt file

To build the UWC use ANT (http://ant.apache.org/):
* cd devel (the devel dir.)
* ant      (the default target will build the UWC under 

To run the newly built UWC
* cd target/uwc/
* chmod a+x *sh
* ./run_uwc_devel.sh

Documentation here: https://migrations.atlassian.net/wiki/display/UWC/Universal+Wiki+Converter

Note: you do not need to build the UWC to run it, just if you're doing development work on it. 

==

This code is open source and is no longer actively maintained  - but the latest release was for Confluence 4.3.7 which is the latest storage format of Atlassian Confluence (introduced in Confluence 4). You can create a staging server to 4.3.7, and run against that, and then upgrade easy enough. That said however, there are *many* flavors of FROM wiki's for a conversion tool, and so this is a tool, not always the end-to-end solution. Wiki formats are varied - and so please understand that the UWC will get you further along- but there could be some post-processing or scripts or other things to also assist in the process. 

We do provide ongoing small and big support for migrations, depending on needs. Email us at info@appfusions.com and let us know what you are trying to do and we can see if we can help you!  

We have references too.

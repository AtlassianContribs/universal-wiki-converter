#!/bin/bash

echo "Links"
./compare.sh Links

echo "Lists"
./compare.sh Lists

echo "Bold"
./compare.sh Bold

echo "Headers"
./compare.sh Headers

echo "HR"
./compare.sh HR

echo "Table"
./compare.sh Table

echo "Filter"
./compare.sh Filter

echo "Attach"
./compare.sh Attach

echo "Labels"
./compare.sh Labels

echo "Html"
./compare.sh Html

echo "Recent"
./compare.sh Recent

echo "Search"
./compare.sh Search

echo "Include"
./compare.sh Include

echo "Deleted"
./testdeleted.sh Deleted

echo "Rss"
./compare.sh Rss

echo "Taglist"
./compare.sh Taglist

echo "UserDate"
./compare.sh UserDate

echo "****************************"
echo "****************************"
echo "* OPTIONAL CONVERTERS"
echo "SearchOpt"
./compare.sh SearchOpt

echo "Many2OneLinks"
./compare.sh Many2OneLinks

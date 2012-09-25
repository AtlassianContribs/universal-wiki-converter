#!/bin/bash

echo "Basic"
./compare.sh Basic

echo "Code"
./compare.sh Code

echo "Manfred"
./compare.sh Manfred

echo "Mailto"
./compare.sh Mailto

echo "Lists"
./compare.sh Lists

echo "Tables"
./compare.sh Tables 

echo "Esc"
./compare.sh Esc

echo "Discussion"
./compare.sh Discussion

## Not completely implemented yet
#echo "Image"
#./compare.sh Image
echo "****************************"
echo "* OPTIONAL CONVERTERS"

## This might not work if the user macro hasn't been installed on Confluence 
## ahead of time as of Conf 4 updates.
#echo "Ext"
#./compare.sh Ext



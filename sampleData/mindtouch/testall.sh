#!/bin/bash

echo "Basic"
./compare.sh Basic

echo "Headers"
./compare.sh Headers

echo "ChangedTitle"
./compare.sh ChangedTitle 2

echo "Comments"
./compare.sh Comments

echo "Tags"
./compare.sh Tags

echo "Attachments" 
./compare.sh Attachments 40

echo "Clean" 
./compare.sh Clean

echo "Tables" 
./compare.sh Tables

echo "Links" 
./compare.sh Links

## If we need to specify the prefix number, then we do it this way
#echo "Attachments"
#./compare.sh Attachments 2



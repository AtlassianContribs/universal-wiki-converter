#!/bin/bash

echo "DefList"
./compare.sh DefList

echo "Math"
./compare.sh Math

echo "Media"
./compare.sh Media

echo "EscBrace"
./compare.sh EscBrace

echo "EscBrace2"
./compare.sh EscBrace2

echo "LinkSpace"
./compare.sh LinkSpace

echo "Label"
./compare.sh Label

echo "LinksWS"
./compare.sh LinksWS

echo "Bild"
./compare.sh Bild

echo "1"
./compare.sh 1

echo "2"
./compare.sh 2

echo "3"
./compare.sh 3

echo "5"
./compare.sh 5

echo "6"
./compare.sh 6

echo "8"
./compare.sh 8

echo "9"
./compare.sh 9

echo "10"
./compare.sh 10

echo "11"
./compare.sh 11

echo "12"
./compare.sh 12

echo "ImagePx"
./compare.sh ImagePx

echo "TableDollar"
./compare.sh TableDollar

echo "TableCleanup"
./compare.sh TableCleanup

echo "BasicHtml"
./compare.sh BasicHtml

echo "HtmlHeader"
./compare.sh HtmlHeader

echo "HtmlList"
./compare.sh HtmlList

echo "TableList"
./compare.sh TableList

echo "HtmlTable"
./compare.sh HtmlTable

echo "TableLines"
./compare.sh TableLines

echo "Code"
./compare.sh Code

echo "HeaderEq"
./compare.sh HeaderEq

echo "TableCurly"
./compare.sh TableCurly

echo "ImageSize"
./compare.sh ImageSize

echo "Mailto"
./compare.sh Mailto

#OPT-IN ISSUES (converters have to be turned on for these to work):
echo "*************************"
echo "** OPT-IN ISSUES" 
echo "** converters must be turned on for these to pass"

echo "LinkNamespaceIssue"
./compare.sh LinkNamespaceIssue

echo "Wikipedia"
./compare.sh Wikipedia

#echo "HtmlInvalid"
#./compare.sh HtmlInvalid

echo "UserDate"
./compare.sh UserDate

echo "Splist"
./compare.sh Splist

echo "TableSpan"
./compare.sh TableSpan


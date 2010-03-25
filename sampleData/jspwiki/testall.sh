#!/bin/bash

echo "1"
./compare.sh 1

echo "2"
./compare.sh 2

echo "3"
./compare.sh 3

echo "4"
./compare.sh 4

echo "5"
./compare.sh 5

echo "6"
./compare.sh 6

echo "7"
./compare.sh 7

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

echo "13"
./compare.sh 13

echo "14"
./compare.sh 14

echo "15"
./compare.sh 15

echo "16"
./compare.sh 16

echo "17"
./compare.sh 17

echo "18"
./compare.sh 18

echo "19"
./compare.sh 19

echo "20"
./compare.sh 20

echo "21"
./compare.sh 21

echo "22"
./compare.sh 22

echo "FileLink"
./compare.sh FileLink

echo "FileLink2"
./compare.sh FileLink2

echo "FileLink3"
./compare.sh FileLink3

echo "AttSpace"
./compare.sh AttSpace

echo "TableDollar"
./compare.sh TableDollar

echo "LinkSpace"
./compare.sh LinkSpace

echo "ListWS"
./compare.sh ListWS

echo "Bracket"
./compare.sh Bracket

echo "AltColor"
./compare.sh AltColor

echo "Quote"
./compare.sh Quote

echo "AltUnderline"
./compare.sh AltUnderline

echo "AltItalics"
./compare.sh AltItalics

echo "AltCombo"
./compare.sh AltCombo

echo "LinkQuery"
./compare.sh LinkQuery

echo "AttNoRef"
./compare.sh AttNoRef

echo "Code"
./compare.sh Code

echo "AttFile"
./compare.sh AttFile

echo "Combo"
./compare.sh Combo

echo "MultiLineMono"
./compare.sh MultiLineMono

echo "Backslashes"
./compare.sh Backslashes

echo "FtpLink"
./compare.sh FtpLink

echo "FileLinkBack"
./compare.sh FileLinkBack

echo "AltBackslash"
./compare.sh AltBackslash

echo "EscTilde"
./compare.sh EscTilde

echo "Mailto"
./compare.sh Mailto

echo "****************************"
echo "* OPTIONAL CONVERTERS"
echo "Margin"
./compare.sh Margin

echo "+WithSpace"
cp "../../target/uwc/output/output/SampleJspwiki-Input WithSpace" SampleJspwiki-Output+WithSpace
diff -u SampleJspwiki-Output+WithSpace SampleJspwiki-Expected+WithSpace.txt

echo ".WithDots"
./compare.sh .WithDots

echo "AltDefList"
./compare.sh AltDefList

echo "Tabs"
./compare.sh Tabs

echo "LinkCase"
./compare.sh LinkCase


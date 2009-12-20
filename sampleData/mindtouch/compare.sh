#!/bin/bash

PREF="SampleMindtouch_"
IN="Input"
OUT="Output"
EXP="Expected"
NUM="1"
EXT=".xml"
FLAGS="-u"

if [ -n "$1" ]
then
	if [ $1 == "-w" ] || [ $1 == "-B" ] || [ $1 == "-y" ]
	then
		FLAGS="$1"
		if [ -n "$2" ]
		then
			NUM="$2"
		fi
	else
		NUM="$1"
		if [ -n "$2" ]
		then
			PREF="$2_$PREF"
		else
			PREF="1_$PREF"
		fi
	fi
fi

SUF="$NUM$EXT"
INFILE="$PREF$IN$NUM"
OUTFILE="$PREF$OUT$NUM"
EXPFILE="$PREF$EXP$SUF"

cp ../../target/uwc/output/output/$INFILE $OUTFILE

diff $FLAGS $OUTFILE $EXPFILE

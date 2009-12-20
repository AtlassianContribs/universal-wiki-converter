#!/bin/bash

PREF="SampleTokenizer-"
IN="Input"
OUT="Output"
EXP="Expected"
NUM="1"
EXT=".txt"
FLAGS="-u"

if [ -n "$1" ]
then
	if [ $1 == "-w" ] || [ $1 == "-B" ] || [ $1 == "-y" ]
	then
		FLAGS="$1"
	else
		NUM="$1"
	fi
fi

if [ -n "$2" ]
then
	NUM="$2"
fi
SUF="$NUM$EXT"
INFILE="$PREF$IN$NUM"
OUTFILE="$PREF$OUT$NUM"
EXPFILE="$PREF$EXP$SUF"

OUTPUTDIR="../../target/uwc/output/output/"
cp $OUTPUTDIR/$INFILE $OUTFILE

diff $FLAGS $OUTFILE $EXPFILE

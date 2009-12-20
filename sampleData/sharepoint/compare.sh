#!/bin/bash

PREF="SampleSharepoint-"
IN="Input"
OUT="Output"
EXP="Expected"
NUM="1"
EXT=".txt"
FLAGS="-ub"

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
INFILE="$PREF$IN$SUF"
OUTFILE="$PREF$OUT$SUF"
EXPFILE="$PREF$EXP$SUF"

OUTPUTDIR="../../target/uwc/output/output"
cp $OUTPUTDIR/$INFILE $OUTFILE

## Note: output and expected are switched - extra newline hack
diff $FLAGS $OUTFILE $EXPFILE

#!/bin/bash

OUT="../../target/uwc/output/output/"
PREF="SampleSocialtext-"
IN="Input"
NUM="1"

if [ -n "$1" ]
then
	NUM="$1"
fi

INFILE="$OUT$PREF$IN$NUM"

if [ -f $INFILE ]
then
	echo "... FAIL"
	rm $INFILE
	echo "... Deleting $INFILE"
fi


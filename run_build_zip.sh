#!/bin/bash

# assuming you're running bash and have Java and ANT-1.6.2 or up installed more or less the way they should be

MYCWD=`pwd`
# save the orig classpath if any (you shouldn't need one at all)
CLASSPATHORIG=$CLASSPATH
# add the libs needed for ANT to run, we have to do this because of the ANT taskdef
CLASSPATH=$CLASSPATH:$MYCWD/lib/bcel.jar:$MYCWD/lib/javac2.jar:$MYCWD/lib/jdom.jar
export CLASSPATH
ant
ant package
# set the classpath back
CLASSPATH=$CLASSPATHORIG
export CLASSPATH

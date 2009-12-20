## Many thanks to Eric Sorenson for writing this
## BEGIN
#!/bin/bash

MYCWD=`pwd`

CLASSPATHORIG=$CLASSPATH

CLASSPATH=$MYCWD/classes
for file in lib/*.jar ; do
    CLASSPATH=$MYCWD/$file:$CLASSPATH
done

CLASSPATH=$CLASSPATH:$CLASSPATHORIG

export CLASSPATH

## Apple specific VM arguments can be specified as the first command line argument
APPLE_ARGS=""
if [ -n "$1" ]
then
	APPLE_ARGS="$1"
fi

# run out of the sample_files dir
#cd sample_files
java -Xms256m -Xmx256m $APPLE_ARGS -classpath $CLASSPATH com.atlassian.uwc.ui.UWCForm3
## END

## Many thanks to Eric Sorenson for writing this
## BEGIN
#!/bin/bash

MYCWD=`pwd`

CLASSPATHORIG=$CLASSPATH

CLASSPATH="uwc.jar"
for file in lib/*.jar ; do
    CLASSPATH=$MYCWD/$file:$CLASSPATH
done

CLASSPATH=$CLASSPATH:$CLASSPATHORIG

export CLASSPATH

# run out of the sample_files dir
#cd sample_files
java -Xms256m -Xmx256m $APPLE_ARGS -classpath $CLASSPATH com.atlassian.uwc.ui.UWCCommandLineInterface $1 $2 $3 $4
## END

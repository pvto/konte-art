#!/bin/bash

java -version 2>&1 |grep -e "\(8\|9\|10\)" > /dev/null

if [[ "$?" != "0" ]] ; then
  echo "Could not detect Java 8 or above. Aborting." ;
  exit 1 ;
fi

mvn -v > /dev/null

if [[ "$?" != "0" ]] ; then
  echo "Please install maven. Aborting." ;
  exit 1 ;
fi

mvn clean install -DskipTests=true | grep -e "\(Building\|Downloading\|SUCCESS\)"

if [[ "$?" != "0" ]] ; then
  echo "Could not build Konte. Try 'mvn clean install manually and fix the issue'"
  exit 1;
fi

DEST="/usr/lib/konte"
SRC1="./target/"
SRC2="./target/dependency/"
EXE="./bin/konte"


mkdir "$DEST" 2> /dev/null
RES=$?

if [[ "$RES" == "130" ]] ; then 
  echo "You need administrator privileges to install Konte. Aborting." ; 
  exit 1; 
fi 

if [[ "$RES" == "0" ]] ; then
  echo "Created $DEST" ;
fi

echo "installing from $SRC1"
cp target/*.jar /usr/lib/konte/

if [[ "$?" != "0" ]] ; then
  echo "Could not find libraries under $SRC1. Aborting." ;
  exit 1;
fi

echo "installing from $SRC2"
cp target/dependency/*.jar /usr/lib/konte/

if [[ "$?" != "0" ]] ; then
  echo "Could not find libraries under $SRC2. Aborting." ;
  exit 1;
fi

echo "Installing executable '$EXE'."
cp "$EXE" /usr/bin/

if [[ "$?" != "0" ]] ; then
  echo "Could not find Konte executable $EXE. Aborting." ;
  exit 1;
fi

chmod +x /usr/bin/konte

if [[ "$?" != "0" ]] ; then
  echo "Could not grant execute privileges. Konte may not function correctly."
  exit 1;
fi

echo "Done. Run 'konte' from command line to execute."

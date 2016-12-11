#!/bin/bash

DEST="/usr/lib/konte"
SRC1="./target/"
SRC2="./target/dependency/"
EXE="./bin/konte"
EXE2="./bin/kontecli"

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

echo "Installing executables '$EXE', '$EXE2'."
cp "$EXE" /usr/bin/
cp "$EXE2" /usr/bin/

if [[ "$?" != "0" ]] ; then
  echo "Could not find Konte executable $EXE/$EXE2. Aborting." ;
  exit 1;
fi

chmod +x /usr/bin/konte
chmod +x /usr/bin/kontecli

if [[ "$?" != "0" ]] ; then
  echo "Could not grant execute privileges. Konte may not function correctly."
  exit 1;
fi

echo "Done. Run 'konte' or 'kontecli' from command line to execute."

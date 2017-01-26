#!/bin/bash

DEST="/usr/local/lib/konte"
BINDEST="/usr/local/bin"
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
cp target/*.jar "$DEST"/

if [[ "$?" != "0" ]] ; then
  echo "Could not find libraries under $SRC1. Aborting." ;
  exit 1;
fi

echo "installing from $SRC2"
cp target/dependency/*.jar "$DEST"/

if [[ "$?" != "0" ]] ; then
  echo "Could not install libraries from $SRC2 to $DEST. Aborting." ;
  exit 1;
fi

echo "Installing executables '$EXE', '$EXE2'."
cp "$EXE" "$BINDEST"/
cp "$EXE2" "$BINDEST"/

if [[ "$?" != "0" ]] ; then
  echo "Could not find Konte executable $EXE/$EXE2. Aborting." ;
  exit 1;
fi

chmod +x "$BINDEST"/konte
chmod +x "$BINDEST"/kontecli

if [[ "$?" != "0" ]] ; then
  echo "Could not grant execute privileges. Konte may not function correctly."
  exit 1;
fi

echo "Done. Run 'konte' or 'kontecli' from command line to execute."

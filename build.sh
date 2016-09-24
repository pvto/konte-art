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
  echo "Could not build Konte. Try 'mvn clean install' manually and fix the issue"
  exit 1;
fi


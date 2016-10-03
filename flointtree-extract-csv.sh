#!/bin/bash

VALS=$(cat flointtree-benchmark.txt |sed -r 's/[^,]+,[^,]+,([^,]+),.*/\1/'|uniq)
for ff in $VALS ; do  cat flointtree-benchmark.txt|grep FlointTree|grep ",$ff," > FT-$ff.dat; done
for ff in $VALS ; do  cat flointtree-benchmark.txt|grep TreeMap|grep ",$ff," > TM-$ff.dat; done
for foo in $VALS ; do bar=$(cat "FT-$foo.dat"|sed 's/.*,//g'|awk '{foo=foo+$1;n=n+1} {print foo/n}' | tail -n1) && baz=$(cat "TM-$foo.dat"|sed 's/.*,//g'|awk '{foo=foo+$1;n=n+1} {print foo/n}' | tail -n1) && echo "$foo $bar $baz" ; done
rm FT-*.dat
rm TM-*.dat

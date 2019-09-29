#! /bin/bash

( find . -path "*.java" )| xargs wc -l| sort -n
num=`( find . -path "*.java" )| xargs wc -l| sort -n| awk 'END {print}'| cut -d " " -f 2 `
echo "Num in master: 2237"
echo "My num:" $[((num))-2237]

#!/bin/bash

for (( i = 0; i < 50; i++ )); do
	wget -q http://192.168.0.28:7777/file2.txt -O $i &
done
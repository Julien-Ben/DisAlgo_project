#!/bin/bash

>host
for ((i=1;i<=$1;i++))
do
	echo $i localhost 1100$i >> host
done

nohup ../barrier.py --processes $1 > log.txt 2> errors.txt < /dev/null &

for ((i=1;i<=$1;i++))
do
	nohup ./run.sh --id $i --hosts host --barrier localhost:11000 --output out.txt > log.txt 2> errors.txt < /dev/null &
	PID=$!
	echo $PID >> pid.txt
done
sleep 4

killall java
less log.txt

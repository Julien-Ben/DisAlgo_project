#!/bin/bash

>host
for ((i=1;i<=$1;i++))
do
	echo $i localhost 1100$i >> host
	> out$i.txt #Deleting all previous output file
done

#Launching barrier
nohup ../barrier.py --processes $1 > log.txt 2> errors.txt < /dev/null &

for ((i=1;i<=$1;i++))
do
	#Launching the processes
	nohup ./run.sh --id $i --hosts host --barrier localhost:11000 --output out$i.txt > log.txt 2> errors.txt < /dev/null &
	PID=$!
	echo $PID >> pid.txt
done
sleep 4

killall java
less log.txt

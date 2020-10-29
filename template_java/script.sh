#!/bin/bash
logfolder='./log/'
>host
for ((i=1;i<=$1;i++))
do
	echo $i localhost 1100$i >> host
	> ${logfolder}out$i.txt #Deleting all previous output file
	echo ======file $i begining====== > ${logfolder}out$i.txt 
done

#Launching barrier
nohup ../barrier.py --processes $1 > ${logfolder}log.txt 2> ${logfolder}errors.txt < /dev/null &

for ((i=1;i<=$1;i++))
do
	#Launching the processes
	nohup ./run.sh --id $i --hosts host --barrier localhost:11000 --output ${logfolder}out$i.txt > ${logfolder}log$i.txt 2> ${logfolder}errors$i.txt < /dev/null &
	PID=$!
	echo $PID >> pid.txt
done
sleep 4

killall java
for ((i=1;i<=$1;i++))
do
	cat ${logfolder}out$i.txt #Deleting all previous output file
done

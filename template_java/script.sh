#!/bin/bash
logfolder='./log/'
>host
>config
echo $2 > config
for ((i=1;i<=$1;i++))
do
	echo $i localhost 1100$i >> host
	> ${logfolder}out$i.txt #Deleting all previous output file
	echo ======file $i begining====== > ${logfolder}out$i.txt 
done

#Launching barrier
nohup ../barrier.py --host localhost --port 11000 --processes $1 > ${logfolder}log.txt 2> ${logfolder}errors.txt < /dev/null &
nohup ../finishedSignal.py --host localhost --port 11999 --processes $1 > ${logfolder}log.txt 2> ${logfolder}errors.txt < /dev/null &

for ((i=1;i<=$1;i++))
do
	#Launching the processes
	nohup ./run.sh --id $i --hosts host --barrier localhost:11000 --signal localhost:11999 --output ${logfolder}out$i.txt config > ${logfolder}log$i.txt 2> ${logfolder}errors$i.txt < /dev/null &
done
sleep $3

killall java
sleep 0.5
for ((i=1;i<=$1;i++))
do
	cat ${logfolder}out$i.txt
done

for ((i=1;i<=$1;i++))
do
	echo file number $i lines :
	wc -l ${logfolder}out$i.txt
done
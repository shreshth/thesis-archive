#Known parameters
param totalPackets integer;		#Number of packets

#Known sets
set BigPackets := 1..totalPackets;	#Set of big packets to send/receive

#Known parameters
param totalChannels integer;		#Total number of channels including the last dummy channel for 0-sized packets
#param channels integer := totalChannels-1;
param mobileChannel :=1;		#Index of 3G channel
param sizeBig {i in BigPackets} integer;#Size of big packets
param availTime {i in BigPackets};	#Availability times of packets
param needTime {i in BigPackets};	#Time when we need the packets back (depends on delay tolerance of applications)
param bw {1..totalChannels};			#Bandwidth of channels
param latency {1..totalChannels};		#Latency of channels
param begin {1..totalChannels};  		#Begining time of a channel availability
param end {1..totalChannels};    		#Ending time of a channel availability
#param pmax {i in BigPackets};		#Max number of small packets
param tmax;				#Max time = maxD[i]+(maxS[i]/minbw[n]) (everything must be finished by this time)

param msu_data := 1.42578125;
param msu_header := 0.01953125;
param defaultPacketsperPortion ;
param defaultData := defaultPacketsperPortion *msu_data;
param defaultHeader := defaultPacketsperPortion *msu_header;
param totalSmallPackets {i in BigPackets} := ceil(sizeBig[i]/defaultData);
param sizeSmall{i in BigPackets, j in 1..totalSmallPackets[i]} :=
      if j==totalSmallPackets[i] then sizeBig[i]-((totalSmallPackets[i]-1)*defaultData)
      else	defaultData		 ;		#Size of small packets.

param header {i in BigPackets, j in 1..totalSmallPackets[i]} :=
      if j==totalSmallPackets[i] then (ceil(sizeSmall[i,j]/msu_data))*msu_header
      else defaultHeader;				#Size of header added to each packet



#Variables
#var sizeSmall{i in BigPackets, j in 1..totalSmallPackets[i]} integer;		#Size of small packets. 
#var sizeNonZero{i in BigPackets, j in 1..totalSmallPackets[i]} binary ;		#Number of non-zero sized small packets
var time{i in BigPackets, j in 1..totalSmallPackets[i]} ;     			#time when we will send the requests for the package
var channelScheduled{i in BigPackets, j in 1..totalSmallPackets[i], n in 1..totalChannels} binary; 	   	 #binary variable showing if the packet is scheduled to channel n
var seq{i in BigPackets, j in 1..totalSmallPackets[i],k in BigPackets, l in 1..totalSmallPackets[k]: ((k !=i or j !=l) and (availTime[i]<needTime[k]) and (needTime[i]>availTime[k])) } binary;  #binary variable showing if packet[i][j] is scheduled after packet[k][l]
#var w_obj{i in BigPackets, j in 1..totalSmallPackets[i]} integer;  		#additional variable to convert obj. func. into linear form
var bytesTotal;
var isChannelWifi{i in BigPackets, j in 1..totalSmallPackets[i]} binary;	#this is to make the input for real implementation easier

minimize cost:
	 sum{i in BigPackets, j in 1..totalSmallPackets[i]} (sizeSmall[i,j]+ header[i,j])*channelScheduled[i,j,mobileChannel];

#Constraints for variables

subject to totalBytes:
	bytesTotal=sum{i in BigPackets, j in 1..totalSmallPackets[i]} (sizeSmall[i,j]+ header[i,j]);
	
# subject to smallPacketSizeTotal{i in BigPackets}:
# 	sum{j in 1..totalSmallPackets[i]} sizeSmall[i,j] = sizeBig[i];

# subject to smallPacketSizeMin{i in BigPackets, j in 1..totalSmallPackets[i]}:
# 	sizeSmall[i,j] >= sizeNonZero[i,j]*smin[i];
	
# subject to smallPacketSizeMax{i in BigPackets, j in 1..totalSmallPackets[i]}:
# 	sizeSmall[i,j] <= sizeNonZero[i,j]*sizeBig[i] ;

#subject to firstPacketSizeZero{i in BigPackets}:
#	sizeNonZero[i,1] =1;

# subject to gatherSizeNonZeros{i in BigPackets, j in 1..totalSmallPackets[i]}:
# 	sum{k in j..totalSmallPackets[i]} sizeNonZero[i,k] >=  sizeNonZero[i,j];

# subject to gatherSizeNonZeros2{i in BigPackets, j in 1..totalSmallPackets[i]}:
# 	sum{k in j..totalSmallPackets[i]} sizeNonZero[i,k] <=  totalSmallPackets[i]*sizeNonZero[i,j];

# subject to gatherSizeNonZeros{i in BigPackets, j in 1..(totalSmallPackets[i]-1)}:
# 	 sizeNonZero[i,j] >=  sizeNonZero[i,j+1];

#subject to totalSizeZeroMin{i in BigPackets}:
#	sum{j in 1..totalSmallPackets[i]}  sizeNonZero[i,j] >=1;

#subject to totalSizeZeroMax{i in BigPackets}:
#	sum{j in 1..totalSmallPackets[i]}  sizeNonZero[i,j] <= totalSmallPackets[i];
			    	
#if in the same big packet, order the small packets. This is done just to speed up the solution.
subject to sequenceSinglePacketOne {i in BigPackets, j in 1..totalSmallPackets[i], l in 1..totalSmallPackets[i]: (l < j)}:
	seq[i,j,i,l]=1;

subject to sequence {i in BigPackets, j in 1..totalSmallPackets[i],k in BigPackets, l in 1..totalSmallPackets[k]: ((k != i or l != j) and (availTime[i]<needTime[k]) and (needTime[i]>availTime[k])) }:
	seq[i,j,k,l]=1-seq[k,l,i,j];

#if in the same big packet, order the small packets. This is done just to speed up the solution.
#subject to sequenceSinglePacketZero {i in BigPackets, j in 1..totalSmallPackets[i], l in 1..totalSmallPackets[i]: (j < l)}:
#	seq[i,j,i,l]=0;
	
#subject to sequence {i in BigPackets, j in 1..totalSmallPackets[i],k in BigPackets, l in 1..totalSmallPackets[k]:(((k !=i) or (l!=j)) and i>=k)}:
#	seq[i,j,k,l]=1-seq[k,l,i,j];
	
#Additional constraints required for linearizing the objective function
# subject to addConstraint1{i in BigPackets, j in 1..totalSmallPackets[i]}:
# 	 w_obj[i,j] <= sizeBig[i] *channelScheduled[i,j,mobileChannel];

# subject to addConstraint2{i in BigPackets, j in 1..totalSmallPackets[i]}:
# 	w_obj[i,j] >= 0;

# subject to addConstraint3{i in BigPackets, j in 1..totalSmallPackets[i]}:
# 	w_obj[i,j] <= sizeSmall[i,j];  # will not be necessary in the solver. 

# subject to addConstraint4{i in BigPackets, j in 1..totalSmallPackets[i]}:
# 	w_obj[i,j] >=  (sizeBig[i]*(channelScheduled[i,j,mobileChannel] -1))+sizeSmall[i,j];

#if size is zero for a packet, it will be scheduled to last channel (it is a dummy channel)
# subject to sizeZeroChannel{i in BigPackets, j in 1..totalSmallPackets[i]}:
# 	channelScheduled[i,j,totalChannels]=1-sizeNonZero[i,j];

subject to oneChannel{i in BigPackets, j in 1..totalSmallPackets[i]}: 
	sum{n in 1..totalChannels} channelScheduled[i,j,n] =1;

subject to scheduleTime{i in BigPackets, j in 1..totalSmallPackets[i], n in 1..totalChannels}: 
	channelScheduled[i,j,n]*begin[n]<= time[i,j];

subject to totalDuration{i in BigPackets, j in 1..totalSmallPackets[i], n in 1..totalChannels}: 
	time[i,j]+((sizeSmall[i,j]+header[i,j])/bw[n]) + latency[n] <= (tmax*(1-channelScheduled[i,j,n]))+end[n];

subject to totalLatency{i in BigPackets, j in 1..totalSmallPackets[i], n in 1..totalChannels}:
	time[i,j] + ((sizeSmall[i,j]+header[i,j])/bw[n]) + latency[n]<= tmax-((tmax-needTime[i])*channelScheduled[i,j,n]);

#latency won't effect this. 
subject to bandwidth{i in BigPackets, j in 1..totalSmallPackets[i],k in BigPackets, l in 1..totalSmallPackets[k], n in 1..totalChannels :((k != i or j != l) and (availTime[i]<needTime[k]) and (needTime[i]>availTime[k]))  }:	   
	time[k,l]+((sizeSmall[k,l]+header[k,l])/bw[n])<= (tmax* (2-channelScheduled[k,l,n] -seq[i,j,k,l])) +time[i,j];

#subject to bandwidth2{i in BigPackets, j in 1..totalSmallPackets[i],k in BigPackets, l in 1..totalSmallPackets[k], n in 1..totalChannels :((k != i or j > l) and i>=k)}:	   
#	time[i,j]+((sizeSmall[i,j]+header[i,j])/bw[n])+latency[n]<= (tmax* (3-channelScheduled[i,j,n]-channelScheduled[k,l,n] -(1-seq[i,j,k,l]))) +time[k,l];

subject to avaliability{i in BigPackets, j in 1..totalSmallPackets[i]}: 
	time[i,j] >= availTime[i];

#this is just for easier debugging
subject to scripting{i in BigPackets, j in 1..totalSmallPackets[i]}:
	isChannelWifi[i,j] = channelScheduled[i,j,1];
	
	




# Dynamic Window 

The window will adapt its size depending on the incoming stream rate. 

The metric used to adapt the window size, is adapted from 
the paper about VC-TWJoin, and it is as follows for each stream: 

N_tuples/ N_hashlimit  {metric}

Where *N_tuples* is the number of tuples currently in the window of the stream 
at the time of checking. 
*N_hashlimit* signifies the pseudo hash bucket limit
(flink's internal map state is *unlimited* in the number of buckets).

The metric is also used to adjust the pseudo bucket limit as follows: 
```
Next bucket limit =  Current bucket limit * (1 + metric) 
```


The window is getting updated at a dynamic interval with the following characteristics: 
```
If $metric > $epsilon: 
     interval /= 2
else: 
     interval *= 2
```

Therefore, if the window gets filled very fast, the check interval will be shorter 
to process and clear out the windows' contents as frequently as possible. 
Otherwise, the interval will be made longer, since the stream rate is low and more 
tuples need to be collected before reasonable joins/stateful operations could be 
applied. 


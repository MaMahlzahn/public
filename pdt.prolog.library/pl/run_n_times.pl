/* $LICENSE_MSG$(gk) */

% Different implementations of predicates that call another predicate 
% a fixed number of times.
	

% ?-time(run_n_times(true,1000000)).
% 3,000,008 inferences, 0.296 CPU in 0.297 seconds (100% CPU, 10121420 Lips)

:- meta_predicate call_n_times(0, -).

call_n_times(Goal, N) :-
	repeatN(N),
	  Goal,
	fail.
call_n_times(_Goal, _N).
    
repeatN(_).
repeatN(N) :-
	N > 1,
	N2 is N - 1,
	repeatN(N2).    

% The following flag-basede version is slower than the one with repeatN (above):
% ?- time(call_n_times_slow(true,1000000)).
% 3,000,010 inferences, 0.515 CPU in 0.513 seconds (100% CPU, 5827488 Lips)

:- meta_predicate call_n_times_slow(0, -). 

call_n_times_slow(Goal,N) :-
    flag( xxx_run_n_times, _,1),   % Counter = 1
    repeat,
       flag( xxx_run_n_times, Calls,Calls+1),
       call(Goal),
    Calls >= N,
    !.


:- meta_predicate succeeds_n_times(0, -).           
                                           
succeeds_n_times(Goal, Times) :-          
   Counter = counter(0),             
   (   Goal,                         
          arg(1, Counter, N0),          
          N is N0 + 1,                  
          nb_setarg(1, Counter, N),
       fail                          
   ;   arg(1, Counter, Times)        
   ).
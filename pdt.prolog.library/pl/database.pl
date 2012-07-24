/* $LICENSE_MSG$(gk) */

 /**
   * Assert the fact in arg1 only if it isn't there already. Succeed otherwise.
   */
   
:- module_transparent assert_unique_ground_fact/1, 
                      assert_unique_fact/1,
                      assert_unique/1.
                      
assert_unique_ground_fact(Head) :-
    ( ground(Head)
    -> assert_unique_fact(Head)
     ; ctc_error('Fact assumed to be ground is not: ~w',[Head])
    ). 

assert_unique(Head) :- 
   assert_unique_fact(Head) .
      
assert_unique_fact(Head) :-
% much slower than not(Head):    ( not(clause(Head,true)) 
    ( not(Head) 
      -> assert(Head)
      ;  true
    ).
% 
/* $LICENSE_MSG$ */

:- module(edge_counter,[count_call_edges_between_predicates/0,
						call_edges_for_predicates/3]).

:- ensure_loaded('../pdt_factbase').

:- dynamic call_edges_for_predicates/3. %call_edges_for_predicates(SourceID,TargetID,Counter)
count_call_edges_between_predicates:-
    retractall(call_edges_for_predicates(_,_,_)),
	forall(	call_edge(TargetId, SourceLiteralId),
			(	(	literalT(SourceLiteralId,_,SourceRule,_,_,_),
    				pred_edge(SourceRule,SourceId)
   				)
			->	inc_call_edges_for_predicates(SourceId,TargetId)
			;	(	predicateT(TargetId,_,TFunctor,TArity,TModule),
					format('Problem with call-edge: ~w -> ~w (~w:~w/~w)~n',[SourceLiteralId, TargetId, TModule, TFunctor, TArity])
				)
			)
		).	

inc_call_edges_for_predicates(SourceID,TargetID):-
    call_edges_for_predicates(SourceID,TargetID,Counter),
	retract(call_edges_for_predicates(SourceID,TargetID,Counter)),
 	New_Counter is (Counter + 1),
    assert(call_edges_for_predicates(SourceID,TargetID,New_Counter)).
inc_call_edges_for_predicates(SourceID,TargetID):-  
    assert(call_edges_for_predicates(SourceID,TargetID,1)).


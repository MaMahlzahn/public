:-module(metapred_finder, [	infer_meta_arguments_for/3,
							find_meta_pred_args_in_clause/3]).

:- use_module(metafile_referencer).
:- use_module(org_cs3_lp_utils(utils4modules)).



:- dynamic new_meta_pred/2.	%new_meta_pred(MetaSpec, Module)

find_all_meta_predicates:-
    initialize_meta_pred_search,
    repeat,
    	collect_candidates(Candidates),
    	forall(
    		(	member(Module:Candidate, Candidates),
    			infer_meta_arguments_for(Module,Candidate,MetaSpec)
			),
			assert(new_meta_pred(MetaSpec, Module))
		),
		(	new_meta_pred(_)
		->	prepare_next_step
		;	fail
		).
	    
    
    
initialize_meta_pred_search:-
    retractall(metafile_referencer:user_defined_meta_pred(_,_,_,_)),
    retractall(new_meta_pred(_,_,_)),
    forall(	
    	(   current_predicate(Module:Functor/Arity),
    		functor(Head,Functor,Arity),
    		predicate_property(Module:Head, built_in),
    		predicate_property(Module:Head, meta_predicate(Spec))
    	),
    	assert(dynamic new_meta_pred(Module, Head, Spec))
    ).
    
prepare_next_step:-
    forall(	
    	new_meta_pred(MetaSpec, Module),
    	(	functor(MetaSpec, Functor, Arity),
    		assert(metafile_referencer:user_defined_meta_pred(Functor, Arity, Module, MetaSpec)),
    		retract(new_meta_pred(MetaSpec, Module))
    	)
    ).
    
    


/**
 * infer_meta_arguments_for(?Module,?AHead,?MetaSpec) is det
 *
 * Arg3 is the infered meta_predicate specification of the
 * predicat Arg2 in module Arg1. 
 * 
 * Fails, if Arg2 is not a meta-predicate.
 * 
 * Currently only infers ? and 0.
 * For built-in predicates the original specification is used. 
 **/
infer_meta_arguments_for(Module,AHead,MetaSpec):-
    (	var(AHead)
    ->	defined_in_module(Module,Functor,Arity)
    ;	functor(AHead, Functor, Arity)	
    ),
    functor(Head, Functor, Arity), 	%get most general head to find all clauses of the predicate
    findall(	MetaArgs,
				find_meta_pred_args_in_clause(Module, Head, MetaArgs),
				AllMetaArgs
			),
	(	AllMetaArgs = []
	->	fail
	;	(	combine_meta_args(AllMetaArgs,CombinedArgs),
			MetaSpec =.. [Functor|CombinedArgs]
		)
	). 


find_meta_pred_args_in_clause(Module, Head, MetaArgs):-
    \+(var(Head)),
    predicate_property(Module:Head, built_in), !,
    predicate_property(Module:Head, meta_predicate(Spec)),
    Spec =.. [_|MetaArgs].
find_meta_pred_args_in_clause(Module, Head, MetaArgs):-
	 clause(Module:Head, Body),
	 find_meta_vars_in_body(Body, Module, [],  MetaVars),
	 find_meta_vars_in_head(Head, MetaVars, MetaArgs).
	 
	 
	 
find_meta_vars_in_body(A, _, MetaVars, MetaVars):-
    atomic(A),
    !.
find_meta_vars_in_body(A, _, MetaVars, MetaVars):-
    var(A),
    !.
find_meta_vars_in_body(Module:Term, _, KnownMetaVars, MetaVars):-
    !, 
    find_meta_vars_in_body(Term, Module, KnownMetaVars, MetaVars).
    
find_meta_vars_in_body((TermA, TermB), Context, KnownMetaVars, MetaVars):-
	!, 														
   	find_meta_vars_in_body(TermB, Context, KnownMetaVars, MetaVarsB),		
   	find_meta_vars_in_body(TermA, Context, MetaVarsB, MetaVars).		%erst B dann A -> nach vorne propagieren
   															% alternativ evtl einfach aliasse / unifizierungen merken
find_meta_vars_in_body((TermA; TermB), Context, KnownMetaVars, MetaVars):-
    !, 
   	find_meta_vars_in_body(TermB, Context, KnownMetaVars, MetaVarsB),
   	find_meta_vars_in_body(TermA, Context, MetaVarsB, MetaVars).
   	  
find_meta_vars_in_body((TermA = TermB), _Context, KnownMetaVars, MetaVars):-
    !,
%    find_meta_vars_in_body(TermB, Context, KnownMetaVars, MetaVarsB),
%   	find_meta_vars_in_body(TermA, Context, MetaVarsB, OwnMetaVars),
   	(	occurs_in(TermA, KnownMetaVars/*OwnMetaVars*/)
   	->	add_var_to_set(TermB, KnownMetaVars/*OwnMetaVars*/, OwnMetaVars2)
   	;	OwnMetaVars2 = KnownMetaVars%OwnMetaVars
   	),
   	(	occurs_in(TermB, OwnMetaVars2)
   	->	add_var_to_set(TermA, OwnMetaVars2, MetaVars3)
   	;	MetaVars3 = OwnMetaVars2
   	),
   	check_inner_vars(TermA, TermB, MetaVars3, MetaVars).
     
find_meta_vars_in_body(Term, Context, KnownMetaVars, MetaVars):-
    is_metaterm(Context, Term, MetaCombos), !, 
    extract_vars(MetaCombos, MetaArgs),
    handel_meta_args(MetaArgs, Context, KnownMetaVars, MetaVars).

find_meta_vars_in_body(_Term, _Context, MetaVars, MetaVars). 
		% everything else is a direct call
      
      

/**
 * find_meta_vars_in_head(+Head, +MetaVars, ?MetaArgs) is det
 *
 * Succeeds if Arg1 is the head of a meta-predicate-clause and Arg2 all
 * possible bindings for meta-arguments used in the body in the clause.
 * In this case, Arg3 is bound to a list that represents the 
 * meta-argument-binding of the arguments of the Clause.
 * 
 * (Currently only working with ? and 0, but should work for each 
 *  number and +, - in the futuroe.)
 */
find_meta_vars_in_head(Head, MetaVars, MetaArgs):-		%TODO: hier noch sharing realisieren
    Head =.. [_Functor|Args],
    find_args_in_list(Args, MetaVars, MetaArgs, IsMeta),
    (	IsMeta = true
    ->	true
    ;	fail
    ).
    
    
find_args_in_list([],_,[], false).
find_args_in_list([Arg|Rest], MetaVars, MetaArgs, IsMeta):-
    find_args_in_list(Rest,MetaVars,RestMetaArgs, MetaFound),
    (	occurs_in(Arg,MetaVars)
    ->	(	MetaArgs=[0|RestMetaArgs],
    		IsMeta = true
    	)
    ;	(	MetaArgs=[?|RestMetaArgs],
    		IsMeta = MetaFound
    	)
    ).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%      
      
extract_vars([],[]).
extract_vars([(_,Var)|RestCombo], [Var|RestVars]):-
    extract_vars(RestCombo, RestVars).
    
    
    
    
  
handel_meta_args([], _, Known, Known).
handel_meta_args([A|Rest], Context, Known, MetaVars):-
    var(A), !,  
    add_var_to_set(A, Known, OwnMetaVars),
    handel_meta_args(Rest, Context, OwnMetaVars, MetaVars).
handel_meta_args([A|Rest], Context, Known, MetaVars):-
    handel_meta_args(Rest, Context, Known, AllOthers),
    find_meta_vars_in_body(A, Context, AllOthers, MetaVars).
   
   

	 
	
check_inner_vars(TermA,TermB,OldMetaVars,NewMetaVars):-
	unifiable(TermA, TermB, Unifiers),	!,		%TODO: diese L�sung funktioniert nur f�r Variablen nicht f�r
	check_unifier_list(Unifiers,OldMetaVars,NewMetaVars).%      Terme in OldMetaVars			
check_inner_vars(_, _, MetaVars, MetaVars).


check_unifier_list([], Metas, Metas).
check_unifier_list([A=B|Rest], OldMetas, Metas):-	%	TODO: p(A):- term(A,B)= term(C,C), call(B)
	(	occurs_in(A, OldMetas)						% 	funktioniert so nicht! 
	->	add_var_to_set(B, OldMetas, Metas1)			
	;	Metas1 = OldMetas
	),
	(	occurs_in(B, OldMetas)
	->	add_var_to_set(A, Metas1, Metas2)
	;	Metas2 = Metas1
	),
	check_unifier_list(Rest, Metas2, Metas). 




combine_sets_nonbinding([],Set,Set).
combine_sets_nonbinding([E|Rest],OldSet,NewSet):-
    add_var_to_set(E,OldSet,Set),
    combine_sets_nonbinding(Rest,Set,NewSet).
    
    
/* add_var_to_set(?Var, +Set, ?NewSet) is det.
 * 
 * Arg3 is the same as Arg2 but if Arg1 is not already an element
 * of Arg2 it is addes as a first element to Arg3.
 * 
 * Attention: the comparision is based on == instead of =, so
 * 			  different variables are treated differently.
 */
add_var_to_set(Var, Set, NewSet):-
    (	occurs_in(Var, Set)
    ->	NewSet = Set
    ;	NewSet = [Var|Set]
    ).
    
    
/* occurs_in(?Var, +Set) is det.
 * 
 * Succseds, if Arg1 is equal to a member of Arg2.
 * The comparision is done with == instead of =!
 */ 
occurs_in(Var, Set):-
	findall(	OldVar, 
    			(	nth1(_, Set, OldVar),
    				OldVar == Var
    			),
    			AllOldVar
    		),
    not(AllOldVar == []).  
    



combine_meta_args([],[]):- !.    
combine_meta_args([List],List):- !.
combine_meta_args([MetaArgs|RestMetaArgs],CombinedArgs):-
    combine_meta_args(RestMetaArgs,RestCombinedArgs),
    combine_two_arg_lists(MetaArgs, RestCombinedArgs, CombinedArgs).
    
combine_two_arg_lists([], [], []):- !.
combine_two_arg_lists([ArgA|ArgsA], [ArgB|ArgsB], [CombinedArg|CombinedRest]):-
	 combine_two_arg_lists(ArgsA,ArgsB,CombinedRest),
	(	number(ArgA)
	->	(	number(ArgB)
		->	max_list([ArgA,ArgB],CombinedArg)
		;	CombinedArg = ArgA
		)
	;	(	number(ArgB)
		->	CombinedArg = ArgB
		;	CombinedArg = ?
		)   
	).
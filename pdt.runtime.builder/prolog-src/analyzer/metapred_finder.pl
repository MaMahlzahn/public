:-module(metapred_finder, [	get_all_userdefined_meta_predicates/1,
							find_all_meta_predicates/0]).

:- use_module(metafile_referencer).
:- use_module(pdt_prolog_library(utils4modules)).
:- use_module(term_based_metapred_finder).
:- use_module('../modules_and_visibility.pl').


:- dynamic new_meta_pred/2.	%new_meta_pred(MetaSpec, Module)

get_all_userdefined_meta_predicates(MetaPreds):-
    findall(
    	Module:NewMetaSpec,
    	metafile_referencer:user_defined_meta_pred(_Functor, _Arity, Module, NewMetaSpec),
    	MetaPreds
    ).
    

find_all_meta_predicates:-
    initialize_meta_pred_search,
    repeat,
    	collect_candidates(Candidates),
    	forall(
    		(	member(Module:Candidate, Candidates),
    			(	Module = user
    			->	(	functor(Candidate, Functor, Arity),
    					visible_in_module(AModule,Functor,Arity),
    					infer_meta_arguments_for(AModule, Candidate, MetaSpec)
    				)	
    			;	infer_meta_arguments_for(Module,Candidate,MetaSpec)
    			)
 			),
 			assert(new_meta_pred(MetaSpec, Module))
		),
		(	new_meta_pred(_,_)
		->	(	prepare_next_step,
				fail
			)
		;	true
		),
	!.
	    
    
    
initialize_meta_pred_search:-
    retractall(metafile_referencer:user_defined_meta_pred(_,_,_,_)),
    retractall(new_meta_pred(_,_)),
    forall(	
    	(   find_predefined_metas(Spec, Module)
    	),
    	assert(new_meta_pred(Spec, Module))
    		%format('Initial: ~w:~w~n', [Module, Spec])
    ).
    
  
find_predefined_metas(Spec, Module):-
    declared_in_module(Module,Functor, Arity, Module),
    functor(Head,Functor,Arity),
    predicate_property(Module:Head, built_in),
   	predicate_property(Module:Head, meta_predicate(Spec)),
   	is_metaterm(Module, Head, MetaArgs),
    (MetaArgs \= []).
        
        
% GK: Hier wird mit Listen und list_to_set gearbeitet, um duplikate zu eliminieren.
% TODO: Messen, ob assert_unique von fakten nicht schneller w�re und vor allem
% �ber die geringere Stackbelastung an der Aufrufstelle Vorteile b�te.     
% Bzw. noch sauberer mit setof, wenn es skaliert.
collect_candidates(Candidates):-
	findall(
		CandModule:Candidate,
    	(	new_meta_pred(MetaSpec, Module),
    		retract(new_meta_pred(MetaSpec, Module)),
    		functor(MetaSpec, Functor, Arity),
    		%visible_in_module(AModule, Functor, Arity),		%TODO: hier m�sste man eigentlich die Module suchen, die das Modul sehen
    														%		f�r die ..T-Fakten m�glich, aber nicht f�r die vordefinierten...
    														%		andererseits: der genaue Test ist ja eh sp�ter, hier nur Kandidaten.
    		(	parse_util:predicateT_ri(Functor,Arity,Module,PredId)
    		->	parse_util:call_edge(PredId,LiteralId)
			;	parse_util:call_built_in(Functor, Arity, Module, LiteralId)
			),
			parse_util:literalT(LiteralId,_ParentId,ClauseId,_AModule,_Functor,_Arity),
			parse_util:clauseT(ClauseId,_,CandModule,CandFunctor,CandArity),
			%% GK: Hier evtl zu grob, es macht keinen Sinn sich alle 
			% Klauseln des Pr�dikats sp�ter noch mal anzusehen, wenn 
			% man jetzt schon genau weiss, in welcher Klausel ein Meta-Aufruf steckt.
			% Also: Nur ClauseRef merken!
			functor(Candidate, CandFunctor, CandArity),
			\+ (predicate_property(CandModule:Candidate, built-in))%,
			%format('Candidate: ~w:~w~n', [CandModule, Candidate])
        ),
        CandidateList
	), 
	list_to_set(CandidateList, Candidates).	
	
	
    
prepare_next_step:-
    forall(	
    	new_meta_pred(MetaSpec, Module),
    	(	functor(MetaSpec, Functor, Arity),
    		(	metafile_referencer:user_defined_meta_pred(Functor, Arity, Module, OldMetaSpec)
    		->	(	(MetaSpec \= OldMetaSpec)
    			->	(	combine_two_arg_lists(OldMetaSpec, MetaSpec, NewMetaSpec),
    					retractall(metafile_referencer:user_defined_meta_pred(Functor,Arity,Module,_)),
    					assert(metafile_referencer:user_defined_meta_pred(Functor, Arity, Module, NewMetaSpec))
    				)
    			;	retract(new_meta_pred(MetaSpec, Module))	% was already there, no need to handle it again 		
    			)
    		;	assert(metafile_referencer:user_defined_meta_pred(Functor, Arity, Module, MetaSpec)),
    			update_factbase(Functor, Arity, Module)
    		)
    	)
    ).
    
update_factbase(Functor, Arity, Module):-
    parse_util:predicateT_ri(Functor,Arity,Module,PId),
 	assert(parse_util:meta_predT(PId, found)).
    
    


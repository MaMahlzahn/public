:-module(clause_indexer,[
	pdt_update_index/1,
	pdt_clear_index/1
]).


%this module can be used as an annotator to updates the clause index according to the clauses defined (or not defined) in
%the parsed file. It can also be used stand alone, provided that the file to be indexed has already been annotated.
%The index table id used by the indexer is 'clause_definitions'.



:- use_module(library('org/cs3/pdt/annotate/pdt_annotator')).
:- use_module(library('org/cs3/pdt/model/pdt_index')).
:- use_module(library('org/cs3/pdt/model/pdt_handle')).
:- use_module(library('org/cs3/pdt/util/pdt_util')).
:- use_module(library('org/cs3/pdt/util/pdt_util_hashtable')).

%This module registers itself as a property factory for handles of type 'predicate_definition'. See pdt_handle.
:- pdt_register_property_factory(predicate_definition,clause_indexer).

get_property(handle(id(File,Module,Name,Arity),predicate_definition,_),clauses,Value):-
    current_file_annotation(File,_,Terms),
	filter_clauses(Terms,Module:Name/Arity,Clauses),
	Value=..[array|Clauses].

filter_clauses([],_,[]).
filter_clauses([Term|Terms],Sig,[Term|Clauses]):-
    Term=aterm(Anns,_),
    memberchk(clause_of(Sig),Anns),
    !,
    filter_clauses(Terms,Sig,Clauses).
filter_clauses([_|Terms],Sig,Clauses):-
    filter_clauses(Terms,Sig,Clauses).

file_post_annotation_hook([File|_],_,_,Annos,[indexed(IxTime)|Annos]):-
    time_file(File,ModTime),
    time_index(File,IxTime),
    update_index(File,Annos,ModTime,IxTime).
    
pdt_update_index(FileSpec):-
    pdt_file_spec(FileSpec,File),
    time_file(File,ModTime),
    time_index(File,IxTime),
    current_file_annotation(File,Annos,_),
    update_index(File,Annos,ModTime,IxTime).

pdt_clear_index(FileSpec):-
    pdt_file_spec(FileSpec,File),
    current_file_annotation(File,Annos,_),
    clear_index(File,Annos).
    
update_index(_,_,ModTime,IxTime):-
	    ModTime @=< IxTime,
	    !.
	    
update_index(File,Annos,_,_):-	    
	get_time(IxTime),
	clear_index(File,Annos),
	pdt_ht_set(index_times,File,IxTime),	
	pdt_index_load(clause_definitions,IX),
	index_clauses(File,Annos,Definitions,IX,NewIX),
	pdt_index_store(clause_definitions,NewIX).
	
clear_index(File,Annos):-
	pdt_ht_remove_all(index_times,File,_),
	member(defines(Definitions),Annos),
	pdt_index_load(clause_definitions,IX),
	unindex_clauses(File,Definitions,IX,NewIX),
	pdt_index_store(clause_definitions,NewIX).


% clause_definition_index_entry(Module:Name/Arity,File,Key,Value)
clause_definition_index_entry(Module:Name/Arity,File,Name/Arity,ix_entry(Module,File)).
	

index_clauses(File,Annos,IX,NewIX):-
    	member(defines(Clauses),Annos),
   	member(defines_multifile(MultifileDefs),Annos),
   	member(defines_dynamic(DynamicDefs),Annos),
   	member(defines_module_transparent(TransparentDefs),Annos),
   	member(exports(Exports),Annos),
	index_clauses_X(Clauses,DynamicDefs0,MultifileDefs0,TransparentDefs0,[],
	                        DynamicDefs, MultifileDefs, TransparentDefs ,Props),
	
    pdt_index_put(IX,Key,Value,NextIX),
    !,
    index_clauses(File,Annos,Definitions,NextIX,NewIX).


unindex_clauses(_,[],IX,IX).
unindex_clauses(File,[Definition|Definitions],IX,NewIX):-
    clause_definition_index_entry(Definition,File,Key,Value),    
    pdt_index_remove(IX,Key,Value,NextIX),
    index_clauses(File,Definitions,NextIX,NewIX).
	    

time_index(File,IxTime):-
    pdt_ht_get(index_times,File,IxTime),
    !.    
time_index(_, -1).    



% matcher(+Elm,+Elms,+Prop,+InProps,-OutProps,-OutElms)
%
% Elms should be a sorted, list.
% Searches in Elms for elements matching Elm. If one is found,
% OutProps is unified with [Prop|OutProps].
% OutElms is unified with the first suffix of elms whos elements
% are stricly greater then Elm.
% 
matcher(Elm,[Elm|Elms],Prop,InProps,[Prop|InProps],OutElms):-
    !,%head matches
    pdt_chop_after(Elm,Elms,OutElms).
matcher(Elm,Elms,_,InProps,InProps,Elms):-
    %head > Elm. There cannot be another match.
    pdt_chop_before(Elm,Elms,Elms), % same as chop_after in this case.
    !.
matcher(Elm,Elms,Prop,InProps,OutProps,OutElms):-
	%head < Elms. There may be a match further down the list.
	pdt_chop_before(Elm,Elms,NextElms),	    
	matcher(Elm,NextElms,Prop,InProps,OutProps,OutElms).




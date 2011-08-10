:- module(modules_and_visibility, [	compute_visibility_graph/0,
									visible_in_module/2,
									get_predicate_referenced_as/4,
									exporting/3]).

:- ensure_loaded('pdt_factbase').

:- dynamic exporting/3.	%exporting(Module,PredId,FileId)

compute_visibility_graph:-
    compute_exports.
    
    
compute_exports:-
    retractall(exporting(_,_,_)),
    export_dir(Exports,Directive),		
    directiveT(Directive,FileId,_),			
    	flatten(Exports,ExportsFlatt),
    	build_export_edge_from_list(ExportsFlatt, FileId),
    fail.
compute_exports:-
    fileT(FileId,_,user),
    parse_util:predicateT(Id,FileId,_,_,_),    
    	assert(exporting(user,Id,FileId)),
    fail.
compute_exports.
    
build_export_edge_from_list([], _).    
build_export_edge_from_list([A|B], FileId):-
    build_export_edge(A, FileId),
    build_export_edge_from_list(B, FileId).
    
    
    
build_export_edge(reexport(Directive,all), FileId):-
	import_dir(RefFileId,Directive),
	fileT(RefFileId,_,Module),
	forall(	
		parse_util:predicateT_ri(_,_,Module,Id),
		assert(exporting(Module,Id,FileId))
	),!.
build_export_edge(reexport(_,[]),_):-
	!.
build_export_edge(reexport(Directive,[A|B]),FileId):-
    build_export_edge(reexport(Directive,A),FileId),
    build_export_edge(reexport(Directive,B),FileId),
    !.
build_export_edge(reexport(Directive,[Functor/Arity]),FileId):-
    import_dir(RefFileId,Directive),
    fileT(RefFileId,Directive),
    fileT(RefFileId,_,Module),
    parse_util:predicateT_ri(Functor,Arity,Module,Id),
    assert(exporting(Module,Id,FileId)).
build_export_edge(Functor/Arity,FileId):-
    fileT(FileId,_,Module),
    parse_util:predicateT_ri(Functor,Arity,Module,Id),    
    assert(exporting(Module,Id,FileId)),
    !.
build_export_edge(Functor/Arity,FileId):-
    format('Warning for ~w/~w -> ~w: ',[Functor,Arity,FileId]),
    fileT(FileId,_,Module),
    format('~w fails to create export-edge ~n',[Module]),!,
    (	parse_util:predicateT_ri(Functor,Arity,AModule,Id)
    -> format('Module: ~w, Id: ~w~n',[AModule, Id]) 
    ; true
    ). 
%build_export_edge(_,_):-!.



    
%% 
% get_predicate_referenced_as(+Module, +Functor, +Arity, ?PId)
%
get_predicate_referenced_as(Module, Functor, Arity, PId):-
    predicateT_ri(Functor, Arity, _AModule,PId),
    visible_in_module(PId, Module)
    , !.
get_predicate_referenced_as(Module, Functor, Arity, PId):-
    visible_in_module_as(PId, Module, Functor,[Module]),    
	predicateT(PId,_,_,Arity,_),
	!.
get_predicate_referenced_as(Module, Functor, Arity, Predefined):-
    functor(Term, Functor, Arity),
    predicate_property(Module:Term, built_in),
    declared_in_module(Module, Term, DefModule),
    Predefined = predefined(DefModule, Functor, Arity).
    


visible_in_module(Predicate,Module):-
    visible_in_module_as(Predicate,Module,_,[Module]).
    
    
    
visible_in_module_as(Predicate,Module,Functor,_):-
    predicateT(Predicate,_,Functor,_,Module).%,
    %!.
visible_in_module_as(Predicate,Module,Functor,PreviousModules):-
    fileT(ModuleFile,_,Module),
    load_edge(ModuleFile,DefiningFile,Imports,_),				   %TODO: import_dir verarbeiten irgendwo!!!!
    fileT(DefiningFile,_,DefiningModule),
    \+ member(DefiningModule, PreviousModules),
    compute_importing_functor(Imports,DefiningFunctor,Functor),	   %Eva: !!!!! TEST this!!!!!   
    visible_in_module_as(Predicate,DefiningModule,DefiningFunctor, [DefiningModule|PreviousModules]),
    exporting(DefiningModule,Predicate,_).   
   																	
compute_importing_functor(all,Functor,Functor):-
    !.
compute_importing_functor([A|B],Functor,NewFunctor):-
	compute_importing_functor_for_list([A|B],Functor,NewFunctor).
compute_importing_functor(except(List),Functor,NewFunctor):-   
    compute_importing_functor_with_exceptions(List,Functor,NewFunctor).
   
   
compute_importing_functor_for_list([A|B],Functor,NewFunctor):-
    (	(is_searched_functor(A,Functor,NewFunctor), !)
    ;	compute_importing_functor_for_list(B,Functor,NewFunctor)
    ).
    
compute_importing_functor_with_exceptions([],Functor,Functor).
compute_importing_functor_with_exceptions([A|B],Functor,NewFunctor):-
    (	is_searched_functor(A,Functor,AFunctor)
    ->	(	Functor == AFunctor
    	->	fail
    	;	NewFunctor = AFunctor
    	)
    ;	compute_importing_functor_for_list(B,Functor,NewFunctor)
    ).
    
    
    
is_searched_functor(Functor/_Arity,Functor,Functor):-
    !.
is_searched_functor(Functor/_A 'as' NewFunctor,Functor,NewFunctor):- 
    !.   
     



    

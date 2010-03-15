:- module(literal_parser, [parse_bodies]).

:- consult(crossref).
:- ensure_loaded(parse_util).

%Todo: Kommentar verfassen
/*parse_bodies:-				%TODO: wieder zum laufen bringen!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	ruleT(ClauseId,_,_,_,_),
		termT(ClauseId,ClauseTerm),							% no directives considered
		ClauseTerm = (_Head :- Body),						% facts without a body will not be considered
		pos_and_vars(ClauseId,BodyPos,VarNames),
		parse_body_literals(Body, BodyPos, ClauseId, ClauseId, Module, VarNames),
		retract(pos_and_vars(ClauseId,BodyPos,VarNames)),
		fail.*/
parse_bodies:-				%TODO: wieder auf Files einschr�nken!!!!!!!!!!
	pos_and_vars(ClauseId,BodyPos,VarNames),
		termT(ClauseId,ClauseTerm),
		(	(	ClauseTerm = (_Head :- Body),
				ruleT(ClauseId,_,Module,_,_)
			)
		;	(	ClauseTerm = (:- Body),
				directiveT(ClauseId,_,Module)
			)
		),
		parse_body_literals(Body, BodyPos, ClauseId, ClauseId, Module, VarNames),
		retract(pos_and_vars(ClauseId,BodyPos,VarNames)),
		fail.
parse_bodies.


parse_body_literals(Module:Literal, Pos, ParentId, ClauseId, _OrigModule, VarNames) :-
    !, %write('module:: '),writeln(Module:Literal),
    Pos = term_position(From, To, _FFrom, _FTo, SubPos),
    SubPos = [ModuleFrom-ModuleTo, LiteralPos],
	assert_new_node(Module:Literal,From,To,Id),   %<===  
    assert_new_node(Module,ModuleFrom,ModuleTo,_MId),
    parse_body_literals(Literal, LiteralPos, Id, ClauseId, Module, VarNames).				%<--Achtung hier stimmt die Parent-Kette nicht!!!! 
/*****
* ToDo: What does I have to assert from all the above?
******/
   
parse_body_literals([A|B], Pos, ParentId, ClauseId, Module, VarNames) :- 
   !,
   Pos = list_position(From, To, _ElemPos, _TailPos),		
   assert_new_node([A|B],From,To,_Id).		
   	
/**
* ToDo: list elemens should be visited!!!!
**/
   
parse_body_literals(Body, Pos, ParentId, ClauseId, Module, VarNames) :- 
   	xref:is_metaterm(Body, MetaArguments),
   	!, %write('meta:: '),writeln(Body),
   	Pos = term_position(From, To, _FFrom, _FTo, SubPos),
   	assert_new_node(Body,From,To,Id),   %<===
   	functor(Body,Functor,Arity),
    assert(metaT(Id,ParentId,ClauseId,Module,Functor,Arity)),
	forall( member(Meta, MetaArguments), 
           process_meta_argument(Meta, SubPos, Id, ClauseId, Module, VarNames)			
   	).		
/**
* ToDo: edge-references for meta-literals
**/
parse_body_literals('$Var'(_A), _Pos, _ParentId, _ClauseId, _Module, _VarNames) :- 
	!. 
%	Pos = From - To.
%	assert_new_node('$Var'(A),From,To,_Id).    %<===
/**
* ToDo: is that all that should happening for Variables?
**/  							
  
/*parse_body_literals(Body, Pos, ParentId, ClauseId, Module, VarNames) :- 
	atom(Body), 
	!, 
	Pos = From - To,
	assert_new_node(Body,From,To,Id),  %<===
   	functor(Body,Functor,Arity),
    assert(literalT(Id,ParentId,ClauseId,Functor,Arity)).  							
*/   	
  
parse_body_literals(Body, Pos, ParentId, ClauseId, Module, _VarNames) :- 
	% Phuuu, finally a simple literal:
	% Store it! 
	(	Pos = term_position(From, To, _FFrom, _FTo, _SubPos)
   	;	Pos = From - To
   	),
	assert_new_node(Body,From,To,Id),   %<===
	functor(Body,Functor,Arity),
    assert(literalT(Id,ParentId,ClauseId,Module,Functor,Arity)).

process_meta_argument( (Nr,MetaTerm), Pos, ParentId, ClauseId, Module, VarNames) :- 
    nth1(Nr,Pos,TermPos),
    parse_body_literals(MetaTerm, TermPos, ParentId, ClauseId, Module, VarNames). 

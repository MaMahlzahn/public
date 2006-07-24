%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This file is part of the Prolog Development Tool (PDT)
% 
% Author: Lukas Degener (among others) 
% E-mail: degenerl@cs.uni-bonn.de
% WWW: http://roots.iai.uni-bonn.de/research/pdt 
% Copyright (C): 2004-2006, CS Dept. III, University of Bonn
% 
% All rights reserved. This program is  made available under the terms 
% of the Eclipse Public License v1.0 which accompanies this distribution, 
% and is available at http://www.eclipse.org/legal/epl-v10.html
% 
% In addition, you may at your option use, modify and redistribute any
% part of this program under the terms of the GNU Lesser General Public
% License (LGPL), version 2.1 or, at your option, any later version of the
% same license, as long as
% 
% 1) The program part in question does not depend, either directly or
%   indirectly, on parts of the Eclipse framework and
%   
% 2) the program part in question does not include files that contain or
%   are derived from third-party work and are therefor covered by special
%   license agreements.
%   
% You should have received a copy of the GNU Lesser General Public License
% along with this program; if not, write to the Free Software Foundation,
% Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
%   
% ad 1: A program part is said to "depend, either directly or indirectly,
%   on parts of the Eclipse framework", if it cannot be compiled or cannot
%   be run without the help or presence of some part of the Eclipse
%   framework. All java classes in packages containing the "pdt" package
%   fragment in their name fall into this category.
%   
% ad 2: "Third-party code" means any code that was originaly written as
%   part of a project other than the PDT. Files that contain or are based on
%   such code contain a notice telling you so, and telling you the
%   particular conditions under which they may be used, modified and/or
%   distributed.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:- module(pdt_util_aterm,[
	pdt_strip_annotation/3,
	pdt_splice_annotation/3,
	pdt_top_annotation/2,
	pdt_term_annotation/3,
	pdt_subterm/3,
	pdt_outer_match/4,
	pdt_inner_match/4,
	pdt_subst/4,
	pdt_aterm/1,
	pdt_aterm_member/3,
	pdt_functor/2,
	pdt_operand/4
]).

pdt_aterm(aterm(_,_)).



    

pdt_aterm_member(List,Path, Elm):-
    match_elms(List,Path,Elm).
pdt_operand(Operator,List,Path, Elm):-
    match_operand(Operator,List,Path,Elm).
pdt_functor(Aterm,Functor/Arity):-
    pdt_aterm(Aterm),
    !,
    pdt_term_annotation(Aterm,Term,_),
    functor(Term,Functor,Arity).
pdt_functor(Term,Functor/Arity):-
    functor(Term,Functor,Arity).
    

match_elms(List,[1],Elm):-
    pdt_subterm(List,[1],Elm).
%match_elms(List,[2],Elm):-
%    pdt_subterm(List,[2],Elm).
match_elms(List,[2|T],Elm):-
    pdt_subterm(List,[2],Elms),
    match_elms(Elms,T,Elm).
    	

match_operand(Operator,ATerm,[],ATerm):-
    \+ pdt_functor(ATerm,Operator),
    !.
match_operand(_Operator,ATerm,[1],Elm):-    
    pdt_subterm(ATerm,[1],Elm).
match_operand(Operator,ATerm,[2|T],Elm):-
    pdt_subterm(ATerm,[2],Elms),
    match_operand(Operator,Elms,T,Elm).


%pdt_subterm(+Term,+Path,?Subterm).
%succeeds if Term is a term or an annotated term and Path is a list of integers
%such that if each element of Path is interpreted as an argument position, Path induces a
%path from Term to (annotated or not annotated) sub term SubTerm.
pdt_subterm(Term,[], Term).
pdt_subterm(ATerm,Path,SubTerm):-
	pdt_aterm(ATerm),
	!,
	pdt_term_annotation(ATerm,Term,_),
	pdt_subterm_rec(Term,Path,SubTerm).
pdt_subterm(Term,Path,SubTerm):-
	pdt_subterm_rec(Term,Path,SubTerm).

pdt_subterm_rec(Term,[ArgNum|ArgNums],SubTerm):-
	compound(Term),
	arg(ArgNum,Term,ArgVal),
	pdt_subterm(ArgVal,ArgNums,SubTerm).
	    
:-module_transparent pdt_outer_match/4.
:-module_transparent pdt_inner_match/4.
	    
pdt_outer_match(ArgVal,ArgNums,SubTerm,Goal):-
    context_module(Module),
    pdt_util_aterm:outer_match(ArgVal,ArgNums,SubTerm,Module,Goal).
pdt_inner_match(ArgVal,ArgNums,SubTerm,Goal):-
    context_module(Module),
    pdt_util_aterm:inner_match(ArgVal,ArgNums,SubTerm,Module,Goal).
	    
outer_match(Term,[], Term,Module,Goal):-
    Module:call(Goal),
    !.
outer_match(ATerm,Path,SubTerm,Module,Goal):-
	pdt_aterm(ATerm),
	!,
	pdt_term_annotation(ATerm,Term,_),
	outer_match_rec(Term,Path,SubTerm,Module,Goal).
outer_match(Term,Path,SubTerm,Module,Goal):-	
	outer_match_rec(Term,Path,SubTerm,Module,Goal).

outer_match_rec(Term,[ArgNum|ArgNums],SubTerm,Module,Goal):-
	compound(Term),
	arg(ArgNum,Term,ArgVal),
	outer_match(ArgVal,ArgNums,SubTerm,Module,Goal).


%%TODO this needs testing. 
inner_match(ATerm,Path,SubTerm,Module,Goal):-
	pdt_aterm(ATerm),
	!,
	pdt_term_annotation(ATerm,Term,_),
	inner_match_X(Term,Path,SubTerm,Module,Goal).
inner_match_(Term,ArgNums,SubTerm,Module,Goal):-
    inner_match_X(Term,ArgNums,SubTerm,Module,Goal).
	
inner_match_X(Term,ArgNums,SubTerm,Module,Goal):-
    (	inner_match_recursive(Term,ArgNums,SubTerm,Module,Goal)
    *->	true
    ;	inner_match_local(Term,ArgNums,SubTerm,Module,Goal)
    ).
inner_match_recursive(Term,[ArgNum|ArgNums],SubTerm,Module,Goal):-
	compound(Term),
	arg(ArgNum,Term,ArgVal),
	inner_match(ArgVal,ArgNums,SubTerm,Module,Goal).
inner_match_local(Term,[], Term,Module,Goal):-
    Module:call(Goal).


pdt_subst(_InTerm,[], OutTerm,OutTerm).
pdt_subst(InATerm,Path,SubATerm,OutATerm):-
	pdt_aterm(InATerm),
	pdt_aterm(SubATerm),
	!,
	pdt_term_annotation(InATerm,InTerm,Anno),
	pdt_subst_X(InTerm,Path,SubATerm,OutTerm),
	pdt_term_annotation(OutATerm,OutTerm,Anno).
pdt_subst(InTerm,Path,SubTerm,OutTerm):-	
	pdt_subst_X(InTerm,Path,SubTerm,OutTerm).
	% Don't "inline" pdt_subst_X! This would _NOT_ be semantic-preserving! 
	% Think of what happens if the parsed file actually contains aterm/2 terms.

	
pdt_subst_X(InTerm,[ArgNum|ArgNums],SubTerm,OutTerm):-
	compound(InTerm),
	arg(ArgNum,InTerm,ArgVal),
	pdt_subst(ArgVal,ArgNums,SubTerm,OutArg),
	InTerm=..[Functor|InArgs],
	subst_nth_elm(InArgs,ArgNum,OutArg,OutArgs),
	OutTerm=..[Functor|OutArgs].
    

subst_nth_elm([_InArg|InArgs],1,SubstArg,[SubstArg|InArgs]):-
	!.
subst_nth_elm([InArg|InArgs],ArgNum,SubstArg,[InArg|OutArgs]):-
    %ArgNum > 1,
    N is ArgNum - 1,
    subst_nth_elm(InArgs,N,SubstArg,OutArgs).
    
    
    

%@deprecated: use pdt_term_annotation/3
pdt_top_annotation(aterm(A,_),A).

%pdt_term_annotation(?AnnotatedTerm,?Term,?Annotation).
% succeeds if AnnotatedTerm is an annotated term, Annotation is the top annotation, and
% Term is the unwrapped toplevel with annotated argument terms.
pdt_term_annotation(aterm(A,T),T,A).

pdt_strip_annotation(AnotatedTerm,Term,Anotation):-
    nonvar(AnotatedTerm),
    !,
    strip(AnotatedTerm,Term,Anotation).

pdt_splice_annotation(Term,Anotation,AnotatedTerm):-
    splice(Term,Anotation,AnotatedTerm).


	
	    
unbound_tail(Term):-
    var(Term),!.
unbound_tail('.'(_,Tail)):-
    unbound_tail(Tail).

strip(aterm(HeadAnotation,AnotatedTerm),AnotatedTerm,(HeadAnotation,[])):-
	var(AnotatedTerm),
	!.
strip(aterm(HeadAnotation,AnotatedTerm),Term,(HeadAnotation,ArgAnotations)):-
	AnotatedTerm=..[Functor|AnotatedArgs],
	strip_args(AnotatedArgs,Args,ArgAnotations),
	Term=..[Functor|Args].

strip_args([],[],[]).
strip_args(	[AnotatedArgsH|AnotatedArgsT],
			[ArgsH|ArgsT],
			[ArgAnotationsH|ArgAnotationsT]):-
	strip(AnotatedArgsH,ArgsH,ArgAnotationsH),
	strip_args(AnotatedArgsT,ArgsT,ArgAnotationsT).
	
splice(Term,(HeadAnotation,[]),aterm(HeadAnotation,Term)):-
	var(Term),
	!.	
splice(Term,(HeadAnotation,ArgAnotations),aterm(HeadAnotation,AnotatedTerm)):-
    Term=..[Functor|Args],
    splice_args(Args,ArgAnotations,AnotatedArgs),
    AnotatedTerm=..[Functor|AnotatedArgs].

splice_args([],[],[]).
splice_args([ArgsH|ArgsT],
			[ArgAnotationsH|ArgAnotationsT],
			[AnotatedArgsH|AnotatedArgsT]):-
    splice(ArgsH,ArgAnotationsH,AnotatedArgsH),
    splice_args(ArgsT,ArgAnotationsT,AnotatedArgsT).

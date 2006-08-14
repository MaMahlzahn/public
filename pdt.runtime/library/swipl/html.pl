/*  $Id$

    Part of SWI-Prolog

    Author:        Jan Wielemaker
    E-mail:        wielemak@science.uva.nl
    WWW:           http://www.swi-prolog.org
    Copyright (C): 2006, University of Amsterdam

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    As a special exception, if you link this library with other files,
    compiled with a Free Software compiler, to produce an executable, this
    library does not by itself cause the resulting executable to be covered
    by the GNU General Public License. This exception does not however
    invalidate any other reasons why the executable file might be covered by
    the GNU General Public License.
*/

:- module(pldoc_html,
	  [ doc_write_html/3		% +Stream, +Title, +Term
	  ]).
:- use_module(modes).
:- use_module(library('http/html_write')).

/** <module> PlDoc HTML backend

This module translates the Herbrand term from the documentation
extracting module wiki.pl into HTML+CSS.
*/

doc_write_html(Out, Title, Doc) :-
	page_dom(Title, Doc, DOM),
	phrase(html(DOM), Tokens),
	print_html_head(Out),
	print_html(Out, Tokens).

page_dom(Title, Body, DOM) :-
	DOM = html([ head([ title(Title),
			    link([ rel(stylesheet),
				   type('text/css'),
				   href('pldoc.css')
				 ])
			  ]),
		     body(Body)
		   ]).

print_html_head(Out) :-
	format(Out,
	       '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" \
	       "http://www.w3.org/TR/html4/strict.dtd">~n', []).

% Rendering rules
%
% These rules translate \-terms produced by wiki.pl

tags(Tags) -->
	html(dl(class=tags, Tags)).

tag(Tag, Value) -->
	{   tag_title(Tag, Title)
	->  true
	;   Title = Tag
	},
	{   tag_class(Tag, Class)
	->  true
	;   Class = tag
	},
	html([dt(class=Class, Title), dd(Value)]).

tag_title(compat, 'Compatibility:').
tag_title(tbd,    'To be done:').

tag_class(tbd, 		warn).
tag_class(bug, 		error).
tag_class(depreciated,	warning).

params(Params) -->
	html([ dt(class=tag, 'Parameters:'),
	       dd(table(class=paramlist,
			\param_list(Params)))
	     ]).

param_list([]) -->
	[].
param_list([H|T]) -->
	param(H),
	param_list(T).

param(param(Name,Descr)) -->
	html(tr([td(var(Name)), td(class=argdescr, ['- '|Descr])])).


		 /*******************************
		 *	 PRED MODE HEADER	*
		 *******************************/

%%	pred_dt(+Modes)// is det.
%
%	Emit the predicate header.
%	
%	@param Modes	List as returned by process_modes/4.

pred_dt(Modes) -->
	html(dt(class=preddef,
		\pred_modes(Modes))).

pred_modes([]) -->
	[].
pred_modes([H|T]) -->
	pred_mode(H),
	pred_modes(T).
		
pred_mode(mode(Head,Vars)) --> !,
	{ bind_vars(Vars) },
	pred_mode(Head).
pred_mode(Head is Det) --> !,
	pred_head(Head),
	pred_det(Det),
	html(br([])).
pred_mode(Head) -->
	pred_head(Head),
	html(br([])).

bind_vars([]).
bind_vars([Name=Var|T]) :-
	Var = '$VAR'(Name),
	bind_vars(T).


pred_head(//(Head)) --> !,
	pred_head(Head),
	html(//).
pred_head(Head) -->
	{ atom(Head) }, !,
	html(b(class=pred, Head)).
pred_head(Head) -->
	{ Head =.. [Functor|Args] },	% TBD: operators!
	html([ b(class=pred, Functor),
	       var(class=arglist,
		   [ '(', \pred_args(Args), ')' ])
	     ]).

pred_args([]) -->
	[].
pred_args([H|T]) -->
	pred_arg(H),
	(   {T==[]}
	->  []
	;   html(', '),
	    pred_args(T)
	).

pred_arg(Term) -->
	{ Term =.. [Ind,Arg],
	  mode_indicator(Ind)
	}, !,
	html([Ind, \pred_arg(Arg)]).
pred_arg(Arg:Type) --> !,
	html([\argname(Arg), :, \argtype(Type)]).
pred_arg(Arg) -->
	argname(Arg).

argname('$VAR'(Name)) --> !,
	html(Name).
argname(Name) --> !,
	html(Name).

argtype(Term) -->
	{ format(string(S), '~q', [Term]) },
	html(S).

pred_det(unknown) -->
	[].
pred_det(Det) -->
	html([' is ', b(class=det, Det)]).

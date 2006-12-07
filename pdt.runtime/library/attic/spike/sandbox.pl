:- use_module(library('/org/cs3/pdt/util/pdt_source_term')).
:- use_module(library('org/cs3/pdt/util/pdt_util_context')).

:- pdt_define_context(layout(mode,op,offset,row,col,indent_base,indent_current,module,stream)).

init_layout_cx(Cx1):-
    layout_new(Cx0),
    layout_set(Cx0, [
    	mode=[],
    	op=[],
    	offset=0,
    	row=0,
    	col=0,
    	indent_base=0,
    	indent_current=0,
    	module=user,
    	stream=current_output
    ], Cx1 ).
do_goal(Goal,CxIn, CxOut):-
	meta_call(CxIn,Goal),
	!,
	goal_left_paren(Goal,CxIn,Cx1),
	do_meta_call(Goal,Cx1,Cx2),
	goal_right_paren(Goal,Cx2,CxOut).
do_goal(Goal,CxIn, CxOut):-
    goal_left_paren(Goal,CxIn,Cx1),
	do_data(Goal,Cx1,Cx2),
	goal_right_paren(Goal,Cx2,CxOut).

do_meta_call(Goal,CxIn,CxOut):-	
	conjunction(Goal,CxIn,Goals),
	!,
	push_mode(conjunction,CxIn,Cx1),
	push_functor(Goal,Cx1,Cx2),
	do_conjunction(Goals,Cx2,Cx3),
	pop_functor(Cx3,Cx4),
	pop_mode(Cx4,CxOut).
do_meta_call(Goal,CxIn,CxOut):-	
	prefix_op(Goal,CxIn),
	!,
	source_term_functor(Goal,Name,_),
	output_op(Name,CxIn,Cx1),
	inc_indent(Cx1,Cx2),
	source_term_arg(1,Goal,Arg1),
	output_goal(Arg1,Cx2,Cx3),
	dec_indent(Cx3,CxOut).
do_meta_call(Goal,CxIn,CxOut):-	
	postfix_op(Goal,CxIn),
	!,
	source_term_arg(1,Goal,Arg1),
	output_goal(Arg1,CxIn,Cx1),
	source_term_functor(Goal,Name,_),
	output_op(Name,Cx1,CxOut).
do_meta_call(Goal,CxIn, CxOut):-
	infix_op(Goal,CxIn),
	!,
	source_term_arg(1,Goal,Arg1),
	output_goal(Arg1,CxIn,Cx4),
	new_line(Cx4,Cx5),
	align(-1,Cx5,Cx6),
	source_term_functor(Goal,Name,_),
	output_op(Name,Cx6,Cx7),
	align(0,Cx7,Cx8),
	source_term_arg(2,Goal,Arg2),	
	output_goal(Arg2,Cx8,CxOut).
do_meta_call(Goal,CxIn, CxOut):-
	source_term_functor(Goal,Name,Arity),
	output_functor(Name,CxIn,Cx1),
	output_args(Arity,Goal,Cx1,CxOut).	



output_args(0,_,Cx,Cx).
output_args(Arity,Goal,CxIn,CxOut):-
    push_mode(arguments,CxIn,Cx0),
	output('(',Cx0,Cx1),
	inc_indent(Cx1,Cx2),	
	output_args(1,Arity,Goal,Cx2,Cx3),
	dec_indent(Cx3,Cx4),	
	new_line(Cx4,Cx5),
	align(0,Cx5,Cx6),
	output(')',Cx6,Cx7),
	pop_mode(Cx7,CxOut).
	
output_args(I,Arity,_,Cx,Cx):-
    I>Arity,
    !.
output_args(1,Arity,Goal,CxIn,CxOut):-
	source_term_arg(1,Goal,Arg),
	output_goal(Arg,CxIn,Cx1),
	output_args(2,Arity,Goal,Cx1,CxOut).
output_args(I,Arity,Goal,CxIn,CxOut):-
	J is I+1,
	output(',',CxIn,Cx1),
	new_line(Cx1,Cx2),
	align(0,Cx2,Cx3),
	source_term_arg(1,Goal,Arg),
	output_goal(Arg,Cx3,Cx4),
	output_args(J,Arity,Goal,Cx4,CxOut).
	
		
goal_left_paren(Goal,CxIn,CxOut):-
    needs_parenthesis(Goal,CxIn),
    !,
	output('(',CxIn,Cx1),
	inc_indent(Cx1,Cx2),
	align(0,Cx2,Cx3),
    push_functor(Goal,Cx3,Cx4),
    push_mode(operands,Cx4,CxOut).
goal_left_paren(_,Cx,Cx).

goal_right_paren(Goal,CxIn,CxOut):-    	
	pop_functor(CxIn,Cx0),
	pop_mode(Cx0,Cx1),
    needs_parenthesis(Goal,Cx1),
    !,
	dec_indent(Cx1,Cx2),
	new_line(Cx2,Cx3),
	align(0,Cx3,Cx4),
	output(')',Cx4,CxOut).
goal_right_paren(_,Cx,Cx).


conjunction(Goal,_CxIn,Goals):-
    source_term_functor(Goal,',',2),
    !,
    flatten_right(Goal,Goals).


do_conjunction([],Cx,Cx).
do_conjunction([Goal],CxIn,CxOut):-
    do_goal(Goal,CxIn,CxOut).
do_conjunction([Goal|Goals],CxIn,CxOut):-
	output_goal(Goal,CxIn,Cx1),
	output(',',Cx1,Cx2),
	new_line(Cx2,Cx3),
	align(0,Cx3,Cx4),
	do_conjunction(Goals,Cx4,CxOut).
    

list(Term,_,[]):-
	source_term_functor(Term,[],0),
	!.
list(Term,Cx,[Elm|Elms]):-
    source_term_arg(1,Term,Elm),
    source_term_functor(Term,'.',2),
    source_term_arg(2,Term,Tail),
    list(Tail,Cx,Elms).



    
flatten_right(Goal,[Left|RightGoals]):-
    source_term_arg(2,Goal,Right),
    source_term_functor(Goal,F,2),
    source_term_functor(Right,F,2),
    !,
    source_term_arg(1,Goal,Left),
    flatten_right(Right,RightGoals).
flatten_right(Goal,[Left,Right]):-
	source_term_functor(Goal,_F,2),
    source_term_arg(1,Goal,Left),	
    source_term_arg(2,Goal,Right).
    
    
    


meta_call(_CxIn,Goal):-
    source_term_expand(Goal,Term),
	xref_meta(Term,_Terms).
	
do_data(Goal,Cx1,Cx2):-
    source_term_expand(Goal,Term),
    output(Term,Cx1,Cx2).
    
push_mode(NewMode,CxIn,CxOut):-
    layout_mode(CxIn,Modes),
    layout_set_mode(CxIn,[NewMode|Modes],CxOut).

pop_mode(CxIn,CxOut):-
    layout_mode(CxIn,[_Mode|Modes]),
    layout_set_mode(CxIn,Modes,CxOut).
    

push_functor(Goal,CxIn,CxOut):-
    (	term_op(Goal,CxIn,Op)
    ->	push_operator(Op,CxIn,CxOut)
    ;	source_term_functor(Goal,Name,Arity),
    	push_operator(Name/Arity,CxIn,CxOut)
    ).
pop_functor(CxIn,CxOut):-
    pop_operator(CxIn,CxOut).
    
push_operator(Op,CxIn,CxOut):-
    layout_op(CxIn,Ops),
    layout_set_op(CxIn,[Op|Ops],CxOut).
pop_operator(CxIn,CxOut):-
    layout_op(CxIn,[_Op|Ops]),
    layout_set_op(CxIn,Ops,CxOut).
    

prefix_op(Goal,CxIn):-
    term_op(Goal,CxIn,op(_,Type,_)),
	prefix_op_X(Type).    

prefix_op_X(fx).    
prefix_op_X(fy).    

infix_op(Goal,CxIn):-
    term_op(Goal,CxIn,op(_,Type,_)),
	infix_op_X(Type).    

infix_op_X(xfx).    
infix_op_X(xfy).    
infix_op_X(yfx).    
infix_op_X(yfy).    

postfix_op(Goal,CxIn):-
    term_op(Goal,CxIn,op(_,Type,_)),
	postfix_op_X(Type).    

postfix_op_X(xf).    
postfix_op_X(yf).    

term_op(Goal,Cx,op(P,T,Name)):-
    layout_module(Cx,Module),
    source_term_functor(Goal,Name, Arity),
    Module:current_op(P,T,Name),
    atom_length(T,L),
    L is Arity + 1.

output_op(Op,CxIn,CxOut):-
    output(Op,CxIn,CxOut).

output_goal(Goal,CxIn,CxOut):-
    do_goal(Goal,CxIn,CxOut).

    
inc_indent(CxIn,CxOut):-
    layout_indent_base(CxIn,Old),
    New is Old + 1,
    layout_set_indent_base(CxIn,New,CxOut).
dec_indent(CxOut,CxOut):-
    layout_indent_base(CxIn,Old),
    New is Old - 1,
    layout_set_indent_base(CxIn,New,CxOut).
    

align(Pos,CxIn,CxOut):-
    layout_indent_base(CxIn,Base),
    layout_indent_current(CxIn,Current),
    New is Base + Pos,
    (	New < 0
    ->	throw(error(negative_indent))
    ;	New < Current
    -> throw(error(cannot_move_left))
    ;	layout_set_indent_current(CxIn,New,CxOut),
    	Delta is New - Current,
    	n_times(Delta,output_raw('\t',CxOut))
    ).


n_times(0,_Goal).
n_times(N,_Goal):-
    N<0,
    throw(domain_error(positive_integer,N)).
n_times(N,Goal):-
    Goal,
    M is N -1,
    n_times(M,Goal).

needs_parenthesis(Goal,Cx):-
    term_op(Goal,Cx,Op),
    (	peek_mode(Cx,arguments)
    ->	arg_needs_parenthesis
    ;	op_needs_parenthesis(Op,Cx)
    ).
    
op_needs_parenthesis(op(P,_T,_O),Cx):-
	peek_op(Cx,op(NP,_NT,_NO)),
	P>NP. % No? YES! ;-)
	
new_line(CxIn,CxOut):-
    output_raw('\n',CxIn),
    layout_set_indent_current(CxIn,0,CxOut).

output(Term,Cx,Cx):-
	output_raw(Term,Cx).    
	
output_raw(Term,Cx):-
    layout_stream(Cx,Out),
    write(Out,Term).
	%format("output_raw(~w, ~w)~n",[Term,Cx]).


peek_op(Cx,Op):-
	layout_op(Cx,[Op|_]).    
	
peek_mode(Cx,Mode):-
	layout_mode(Cx,[Mode|_]).    	
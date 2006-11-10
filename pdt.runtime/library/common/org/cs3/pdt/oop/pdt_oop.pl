% experimental. Don't lough :-)

% no docs yet, just a braindump (german)
/*
prolopg "oop": ich brauche:
 - polymorphismus
 - effiziente Type-checks
 - Liskov-ersetzbarkeit.
 - instanzen auf dem stack.
 
Idee: Kontextterme + forwarding.

Die grundidee: instanzen tragen typinformationen, insb. das modul das die jeweilige Klasse definiert). 
Aufrufe von abstrakten Pr�dikaten k�nnen so immer an die konkreteste implementierung weitergeleitet werden.
Module von �berklassen werden zu den Import Modulen von Unterklassen hinzugef�gt.
Es gibt nur eine Klasse pro modul.

Instanzfelder werden durch kontext terme realisiert. Hierbei mu� nur Sorge getragen werden, da� die accessor 
pr�dikate in abstrakteren "Klassen" mit den Kontexttermen konkretere IMplementierungen arbeiten k�nnen. 
Die von pdt_util_context generierten accessor pr�dikate m�ssen erweitert werden, so da� es mit 
folgenden termen umgehen kann:
 $pdt_object(Modul:CxName,Data)
 
 und die accessor aufrufe entsprechend forwarden kann.
Anders ausgedr�ckt: Es wird (automatisch) daf�r gesorgt, da� alle accessor pr�dikate �berschrieben werden. 
Da anders nicht auf die felder zugegriffen werden "kann", ist zumindest syntaktische Liskov-ersetzbarkeit 
gew�hrleistet.


*/

:- module(pdt_oop,[/*pdt_define_class/1*/]).

pdt_define_abstract(Module,Name/Arity,ThisPos):-
    add_forward(Module,Name,Arity,ThisPos).

pdt_define_abstract(Name/Arity):-
	context_module(Module),
	add_forward(Module,Name,Arity,1).
	
/*add_forward(Module,Name,Arity,ThisPos):-
	functor(Head,Name,Arity),
	arg(ThisPos,Head,$pdt_object(Target,_)),
	Forwarder=':-'(Head,
	*/
	
%pdt_define_class(Template):-
    
%   my_mode:foo_new(foo(_,_,_,_)).
% 	my_mode:foo_bar(foo(B,_,_,_),B).
% 	my_mode:foo_baz(foo(_,B,_,_),B).
% 	my_mode:foo_rumpel(foo(_,_,B,_),B).
% 	my_mode:foo_knarz(foo(_,_,_,B),B).
% 	my_mode:foo_set_bar(foo(_,A,B,C),Bar,(Bar,A,B,C)).
% 	my_mode:foo_set_baz(foo(A,_,B,C),Baz,(A,Baz,B,C)).
% 	my_mode:foo_set_rumpel(foo(A,B,_,C),Rumpel,(A,B,Rumpel,C)).
% 	my_mode:foo_set_knarz(foo(A,B,C,_),Knarz,(Bar,A,B,C)).
% 
% 	my_mode:foo_get(Foo,FieldValueList):-
%   	pdt_util_context:pdt_context_get_values(my_mode,Foo,FieldValueList).
% 
% 	my_mode:foo_set(FooIn,FieldValueList,FooOut):-
%   	pdt_util_context:pdt_context_set_values(my_mode,FooIn,FieldValueList,FooOut).
:- multifile ct/3.
:- dynamic ct/3.
:- multifile test/1.
:- multifile tearDown/1.
:- multifile setUp/1.

debugme.

/**
 * JTT (JTransfomer transitional version)
 *
 * GK, 20.10.05: Zusammen mit Tobias refactored als Basis f�r die  
 * sprachunabh�ngige version (StarTransformer), auf die ich zur Zeit mit 
 * Sebastian Lorenz hinarbeite und die eigentlich schon lange unser Ziel war.
 * Ausserdem als Grundlage f�r die m�glichst konfliktfreie parallele
 * Erweiterung durch Malte, Marc und Patrick. 
 *
 * WESENTLICHE NEUERUNGEN: Alle definitiv sprachspezifischen Funktionalit�ten
 * sind nun in plugin-Projekte ausgelagert. Dazu geh�rt
 *  - st.L/src/reader/...  Quellprogramm in Sprache L --> AST (in Java) 
 *  - st.L/pl/writer/...   AST --> Quellprogramm in Sprache L (in Prolog)
 *  - st.L/pl/astSpec/...  AST-Definitionen                   (in Prolog) 
 *  - st.L/pl/ctSpec/...   Erweiterungen der CT-Sprache durch cond/1 und
 *                         action/1 Deklarationen (high_level_api, aop_api, ...) 
 * TO DO: 
 *  1. Einige der nachfolgend beschriebenen Verlagerungen in andere Projekte
 *     real durchf�hren (die entsprechenden folder heissen zur Zeit 
 *     --P--file um dran zu erinnern, dass sie nach Projekt P verschoben 
 *     werden muessen).
 *  2. Konsistenz der main.pl Dateien checken!!!
 *  3. Ladetest 1: In aktueller JT version das Laden derjenigen Module 
 *     auskommentieren, die hier vollkommen gel�scht wurden. Dann starten und
 *     Testen, ob alles noch l�uft. Falls erfolgreich mit Punkt 4 weiter. 
 *  4. Ladetest 2: Da keine Pr�dikate umbenannt wurden, m�sste in einer
 *     Instanz des aktuellen JTransformer die Latetest 1 �berstanden hat
 *     und der refactorten Version die gleichen Pr�dikate geladen werden.
 *     Dies in zwei unterschiedlichen Prozessen pr�fen.
 *  5. Inhalts-Abgleich: Mit Hilfe von BeyondCompare durch die einzelnen Dateien
 *     die die beiden Version enthalten durchgehen, und die Inhalte abgleichen!
 *  6. Ladetest 2: wiederholen.
 *  7. Funktionstests von JTT. TESTEN, TESTEN, TESTEN.
 *  8. Neues CVS aufsetzen und allen zug�nglich machen 
 *
 * WER: Tobias, Malte, Lukas, Dagmar, G�nter. Letztendlich sollten wir alle 
 * versuchen, diesen Schritt gemeinsam so schnell und gr�ndlich wie m�glich 
 * �ber die B�hne zu kriegen, um danach von einer besser strukturierten Basis 
 * aus wieder weitermachen zu k�nnen. Da ich morgen bis 16 Uhr Dauerbesprechung
 * habe, gebe ich hiermit die Koordination des ganzen an Tobias weiter. 
 * -- G�nter, 20.10.2005, 20.00 Uhr  
 */ 
 
% Libraries allgemeiner Art (nicht JT-spezifisch) -------------------------- :

:- ['abba'].
:- ['error_handling'].  % dependent on JavaAST, to be generalized (gk&tr, TODO)
%:- ['test/main'].                 % --> general library for PDT (gk&tr)
%:- ['compatiblitySWI'].           % --> general library for PDT (gk&tr)
%:- ['util/main'].                 % --> general library for PDT (tr&gk)


% Der Kern von JTransformer -------------------------------------- :

:- ['linker/main'].% ex interpreter% Replace local by global IDs upon loading
:- ['ast/main'].                   % Manipulate AST (for Java only, to be generalized)
:- ['check/main'].                 % Check AST consistency
:- ['apply/main'].                 % Apply CTs, garbage collection, rollback
:- ['debug/main'].                 % 
:- ['persistence/main'].           % Write all PEFs to a file
	
:- [library('org/cs3/pdt/test/main.pl')].
:- [library('org/cs3/pdt/util/main.pl')].
:- [library('org/cs3/pdt/compatibility/main.pl')].
%:- use_module(library(pdtplugin)).

% :- ['facade/main'].              % --> src_file_handling     (gk&tr, 20.10.05)
% :- [astAlternative/...].         % Future AST representation ???

%:- ['java_lang_init']. 

tree(_id, null, packageT) :- packageT(_id,_).
tree(_id, null, projectT) :- fail.

:- generateDerivedPredicates('Java').
% Non generic code:
packageT(null,'').


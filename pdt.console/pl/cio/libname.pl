/* $LICENSE_MSG$ */


:- prolog_load_context(directory,A), user:assertz(file_search_path(library,A)).
:- use_module(lib_pdt_console_pl('cio/single_char_interceptor.pl')).

full_name:-
	arch_lib_name(Name),writeln(Name).
	
base_name:-
	sci_setting(cio_base_name,Name),writeln(Name).	


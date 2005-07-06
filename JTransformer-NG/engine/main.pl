:- multifile ct/3.
:- dynamic ct/3.
:- multifile test/1.
:- multifile tearDown/1.
:- multifile setUp/1.

debugme.

:- ['facade/main'].
:- ['test/main'].
:- ['error_handling'].
:- ['compatiblitySWI'].
%:- ['factbase']. --> nun in ast/javaFactbase
:- ['generator/main'].
:- ['api/main'].
:- ['apply/main'].
:- ['interpreter/main'].
:- ['depend/main'].
:- ['util/main'].
:- ['ct/main'].
:- ['check/main'].
:- ['persistance/main'].
:- ['ast/main'].
:- ['metrics/main'].
%:- [pdtplugin]. --> this is part of the pdt. don't load it here!
:- ['java_lang_init'].

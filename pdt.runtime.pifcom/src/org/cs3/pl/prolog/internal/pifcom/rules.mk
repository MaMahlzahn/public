################################################################
# SWI-Prolog make include file for building SWI-Prolog on Windows
#
# This is the place to customise the build.  Notably define
# destination path, compiler, library search-path, etc.
################################################################

# Installation target directory.  At the moment, the build will probably
# fail if there is whitespace in the $prefix directory.  You can however
# copy the result to wherever you want.

# prefix=C:\Program Files
#HOME=$(USERPROFILE)
prefix=z:

# We get pthreadVC.dll, pthreadVC.lib, pthread.h, sched.h and semaphore.h
# from the locations below
WINDLLDIR=$(WINDIR)\system32
PTHREADLIBDIR=$(HOME)\lib
PTHREADINCDIR=$(HOME)\include

# The OpenSSL library and include files
# http://www.slproweb.com/products/Win32OpenSSL.html
OPENSSL=C:\OpenSSL
OPENSSLLIBDIR=$(OPENSSL)\lib\VC
OPENSSLINCDIR=$(OPENSSL)\include

# Setup the environment.  Use this to additional libraries and include
# files to the path.  In particular provide access to the jpeg and xpm
# libraries required to build XPCE

INCLUDE=$(INCLUDE);$(HOME)\include
LIB=$(LIB);$(HOME)\lib



!IF "$(DBG)" == "true"
SYMOPT="/Zi"
PDB=true
!ENDIF

!IF "$(SYMBOLS)" == "true"
SYMOPT=/Zi
PDB=true

!ENDIF

!IF "$(CFG)" == "rt"
CMFLAGS=/DO_RUNTIME
BINDIR=$(PLBASE)\runtime
!ENDIF


# Define programs.  The setup here is for standard Microsoft MSVC tools
# on Windows-NT or Windows-2000

# If you are developing, choose /incremental:yes for LD.  It is a *lot*
# faster linking pl2xpce.dll from the XPCE package

CC=cl.exe
!IF "$(LNK)" == "inc"
LD=link.exe /nologo /incremental:yes
!ELSE
LD=link.exe /nologo
!ENDIF
AR=lib.exe
RSC=rc.exe
CMD=cmd.exe
INSTALL=copy
INSTALL_PROGRAM=$(INSTALL)
INSTALL_DATA=$(INSTALL)
MKDIR=mkdir
MAKE=nmake CFG="$(CFG)" DBG="$(DBG)" MT="$(MT)" /nologo /f Makefile.mak

LIBS=user32.lib shell32.lib gdi32.lib advapi32.lib wsock32.lib
!if "$(MT)" == "true"
LIBS=$(LIBS) $(PLHOME)\lib\pthreadVC.lib
!ENDIF

# Architecture identifier for Prolog's current_prolog_flag(arch, Arch)

ARCH=i386-win32

# Some libraries used by various packages

PLLIB=$(PLHOME)\lib\libpl.lib
TERMLIB=$(PLHOME)\lib\plterm.lib
UXLIB=$(PLHOME)\lib\uxnt.lib

!IF "$(DBG)" == "false"
CFLAGS=/TC /MD /W3 /O2 $(SYMOPT) /GX /DNDEBUG /DWIN32 /D_WINDOWS $(CMFLAGS) /nologo /c
!IF "$(PDB)" == "true"
LDFLAGS=/DEBUG /OPT:REF
!ELSE
LDFLAGS=
!ENDIF
D=
DBGLIBS=
!ELSE
CFLAGS=/MD /W3 $(SYMOPT) $(DBGOPT) /GX /D_DEBUG /DWIN32 /D_WINDOWS $(CMFLAGS) /nologo /c
LD=link.exe /nologo /incremental:yes
LDFLAGS=/DEBUG
D=D
DBGLIBS=msvcrtd.lib
!ENDIF


.c.obj:
	@$(CC) -I. -Irc  $(CFLAGS) /Fo$@ $<
.cxx.obj:
	@$(CC) -I. -Irc  $(CFLAGS) /Fo$@ $<

################################################################
# Windows-versions garbage.  Most likely this won't work on Windows 98
# anyhow as we use constructs from cmd.exe such as FOR
################################################################

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

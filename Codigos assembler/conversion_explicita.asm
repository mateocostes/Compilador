.386
.model flat, stdcall
option casemap :none
include \masm32\include\windows.inc
include \masm32\include\kernel32.inc
include \masm32\include\user32.inc
includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\user32.lib
.data
@aux2bytes dw ? 
@ERROR_DIVISION_POR_CERO db "ERROR: Division por cero", 0
@ERROR_RESTA_NEGATIVA db "ERROR: Resultados negativos en restas de enteros sin signo", 0
@ERROR_INVOCACION db "ERROR: Invocacion de funcion a si misma no permitida", 0
_var1@prueba_nombre_programa dd ? 
_funcion1@prueba_nombre_programa dd ? 
_var2@prueba_nombre_programa@funcion1 dd ? 
@2@0 REAL4 2.0
_funcion2@prueba_nombre_programa dd ? 
_var2@prueba_nombre_programa@funcion2 dd ? 
@1 dd 1
@2 dd 2
.code
funcion1@prueba_nombre_programa:
RET
funcion2@prueba_nombre_programa:
MOV ECX, @1
MOV _var2@prueba_nombre_programa@funcion1, ECX
CALL funcion1@prueba_nombre_programa
MOV ECX, _funcion1@prueba_nombre_programa
MOV _var1@prueba_nombre_programa, ECX
RET
start:
MOV ECX, @2
MOV _var2@prueba_nombre_programa@funcion2, ECX
CALL funcion2@prueba_nombre_programa
MOV ECX, _funcion2@prueba_nombre_programa
MOV _var1@prueba_nombre_programa, ECX
invoke ExitProcess, 0
end start
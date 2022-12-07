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
@ERROR_OVERFLOW_PRODUCTO db "ERROR: Overflow en operacion de producto", 0
@ERROR_INVOCACION db "ERROR: Invocacion de funcion a si misma no permitida", 0
_var1@prueba_nombre_programa dd ? 
_funcion1@prueba_nombre_programa dd ? 
_var2@prueba_nombre_programa@funcion1 dd ? 
entro@a@funcion1 db "entro a funcion1", 0
@2 dd 2
llamo@a@funcion1 db "llamo a funcion1", 0
.code
funcion1@prueba_nombre_programa:
MOV ECX, _var2@prueba_nombre_programa@funcion1
MOV _var1@prueba_nombre_programa, ECX
invoke MessageBox, NULL, addr _var1@prueba_nombre_programa, addr _var1@prueba_nombre_programa, MB_OK
invoke MessageBox, NULL, addr entro@a@funcion1, addr entro@a@funcion1, MB_OK 
RET
start:
invoke MessageBox, NULL, addr llamo@a@funcion1, addr llamo@a@funcion1, MB_OK 
MOV ECX, _var2@prueba_nombre_programa@funcion1
MOV @2, ECX
CALL funcion1@prueba_nombre_programa
MOV ECX, _funcion1@prueba_nombre_programa
MOV _var1@prueba_nombre_programa, ECX
invoke ExitProcess, 0
end start
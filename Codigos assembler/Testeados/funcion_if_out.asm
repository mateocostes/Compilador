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
@1 dd 1
@3 dd 3
var1@es@3 db "var1 es 3", 0
var1@no@es@3 db "var1 no es 3", 0
llamo@a@funcion1 db "llamo a funcion1", 0
@2 dd 2
@aux0 dd ? 
.code
funcion1@prueba_nombre_programa:
invoke MessageBox, NULL, addr entro@a@funcion1, addr entro@a@funcion1, MB_OK 
MOV ECX, _var2@prueba_nombre_programa@funcion1
ADD ECX, @1
MOV @aux0, ECX
MOV ECX, @aux0
MOV _var1@prueba_nombre_programa, ECX
MOV ECX, @3
CMP _var1@prueba_nombre_programa, ECX
JNE L18
invoke MessageBox, NULL, addr var1@es@3, addr var1@es@3, MB_OK 
JMP L21
L18:
invoke MessageBox, NULL, addr var1@no@es@3, addr var1@no@es@3, MB_OK 
L21:
RET
start:
invoke MessageBox, NULL, addr llamo@a@funcion1, addr llamo@a@funcion1, MB_OK 
MOV ECX, @2
MOV _var2@prueba_nombre_programa@funcion1, ECX
CALL funcion1@prueba_nombre_programa
MOV ECX, _funcion1@prueba_nombre_programa
MOV _var1@prueba_nombre_programa, ECX
invoke ExitProcess, 0
end start
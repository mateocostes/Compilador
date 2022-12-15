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
_funcion1@prueba_nombre_programa dd 3
_var2@prueba_nombre_programa@funcion1 dd ? 
_var1@prueba_nombre_programa@funcion1 dd ? 
@4 dd 4
@1 dd 1
@5 dd 5
@2 dd 2
@11 dd 11
@es@menor@a@11 db "es menor a 11", 0
@es@mayor@o@igual@a@11 db "es mayor o igual a 11", 0
@aux0 dd ? 
@aux1 dd ? 
@aux2 dd ? 
.code
funcion1@prueba_nombre_programa:
MOV ECX, @4
MOV _var1@prueba_nombre_programa@funcion1, ECX
MOV ECX, _var2@prueba_nombre_programa@funcion1
ADD ECX, _var1@prueba_nombre_programa@funcion1
MOV @aux0, ECX
MOV ECX, @aux0
ADD ECX, @1
MOV @aux1, ECX
MOV ECX, @aux1
MOV _var2@prueba_nombre_programa@funcion1, ECX
RET
start:
MOV ECX, @5
MOV _var1@prueba_nombre_programa, ECX
MOV ECX, @2
MOV _var2@prueba_nombre_programa@funcion1, ECX
CALL funcion1@prueba_nombre_programa
MOV ECX, _var2@prueba_nombre_programa@funcion1
ADD ECX, _var1@prueba_nombre_programa
MOV @aux2, ECX
MOV ECX, @aux2
MOV _var1@prueba_nombre_programa, ECX
MOV ECX, @11
CMP _var1@prueba_nombre_programa, ECX
JAE L33
invoke MessageBox, NULL, addr @es@menor@a@11, addr @es@menor@a@11, MB_OK 
JMP L36
L33:
invoke MessageBox, NULL, addr @es@mayor@o@igual@a@11, addr @es@mayor@o@igual@a@11, MB_OK 
L36:
invoke ExitProcess, 0
end start
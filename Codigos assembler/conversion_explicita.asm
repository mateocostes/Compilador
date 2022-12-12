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
_a@prueba_nombre_programa dd ? 
_b@prueba_nombre_programa dd ? 
@1 dd 1
@2 dd 2
@entro@if db "entro if", 0
@entro@else db "entro else", 0
.code
start:
MOV ECX, @1
MOV _a@prueba_nombre_programa, ECX
MOV ECX, @2
MOV _b@prueba_nombre_programa, ECX
MOV ECX, _b@prueba_nombre_programa
CMP _a@prueba_nombre_programa, ECX
JNA L18
invoke MessageBox, NULL, addr @entro@if, addr @entro@if, MB_OK 
MOV ECX, _b@prueba_nombre_programa
MOV _a@prueba_nombre_programa, ECX
JMP L24
L18:
invoke MessageBox, NULL, addr @entro@else, addr @entro@else, MB_OK 
MOV ECX, _a@prueba_nombre_programa
MOV _b@prueba_nombre_programa, ECX
L24:
invoke ExitProcess, 0
end start
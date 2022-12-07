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
_var2@prueba_nombre_programa dq ?
@1 dd 1
@2@0 REAL4 2.0
@3@0 REAL4 3.0
@entro@if db "entro if", 0
@entro@else db "entro else", 0
@aux0 dq ?
@aux1 dd ? 
.code
start:
MOV ECX, @1
MOV _var1@prueba_nombre_programa, ECX
FLD _var1@prueba_nombre_programa
FLD @2@0
FILD _var1@prueba_nombre_programa
FADD
FSTP @aux0
FLD @aux0
FSTP _var2@prueba_nombre_programa
FLD _var2@prueba_nombre_programa
FCOM @3@0
FSTSW @aux2bytes
MOV AX, @aux2bytes
SAHF
MOV @aux1, 0FFh
JE aux1
MOV @aux1, 00h
aux1:
JNE L18
invoke MessageBox, NULL, addr @entro@if, addr @entro@if, MB_OK 
JMP L21
L18:
invoke MessageBox, NULL, addr @entro@else, addr @entro@else, MB_OK 
L21:
invoke ExitProcess, 0
end start
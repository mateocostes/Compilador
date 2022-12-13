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
_a@prueba_nombre_programa dq ?
_b@prueba_nombre_programa dd ? 
@1 dd 1
@aux0 dd ? 
.code
start:
MOV ECX, @1
MOV _b@prueba_nombre_programa, ECX
MOV ECX, _b@prueba_nombre_programa
ADD ECX, _b@prueba_nombre_programa
MOV @aux0, ECX
FLD @aux0
FILD @aux0
FSTP _a@prueba_nombre_programa
invoke ExitProcess, 0
end start
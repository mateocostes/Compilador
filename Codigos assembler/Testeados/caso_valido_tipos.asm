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
_var2@prueba_nombre_programa dq ?
@1 dd 1
@1@0 REAL4 1.0
@tipo@valido db "tipo valido", 0
@tipo@invalido db "tipo invalido", 0
@aux0 dd ? 
.code
start:
MOV ECX, @1
MOV _var1@prueba_nombre_programa, ECX
FLD _var1@prueba_nombre_programa
FILD _var1@prueba_nombre_programa
FSTP _var2@prueba_nombre_programa
FLD _var2@prueba_nombre_programa
FCOM @1@0
FSTSW @aux2bytes
MOV AX, @aux2bytes
SAHF
MOV @aux0, 0FFh
JE aux0
MOV @aux0, 00h
aux0:
JNE L16
invoke MessageBox, NULL, addr @tipo@valido, addr @tipo@valido, MB_OK 
JMP L19
L16:
invoke MessageBox, NULL, addr @tipo@invalido, addr @tipo@invalido, MB_OK 
L19:
invoke ExitProcess, 0
end start
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
_var2@prueba_nombre_programa dd ? 
@1 dd 1
@entro db "entro", 0
@despues@break db "despues break", 0
@5 dd 5
@fin db "fin", 0
@aux0 dd ? 
@aux1 dd ? 
.code
start:
MOV ECX, @1
MOV _var1@prueba_nombre_programa, ECX
L3:
invoke MessageBox, NULL, addr @entro, addr @entro, MB_OK 
MOV ECX, _var1@prueba_nombre_programa
ADD ECX, @1
MOV @aux0, ECX
MOV ECX, @aux0
MOV _var2@prueba_nombre_programa, ECX
JMP L27
invoke MessageBox, NULL, addr @despues@break, addr @despues@break, MB_OK 
MOV ECX, @5
CMP _var1@prueba_nombre_programa, ECX
JA L27
MOV ECX, _var1@prueba_nombre_programa
ADD ECX, @1
MOV @aux1, ECX
MOV ECX, @aux1
MOV _var1@prueba_nombre_programa, ECX
JMP L3
L27:
invoke MessageBox, NULL, addr @fin, addr @fin, MB_OK 
invoke ExitProcess, 0
end start
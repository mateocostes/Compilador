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
_i@prueba_nombre_programa dd ? 
_j@prueba_nombre_programa dd ? 
@loop@exterior db "loop exterior", 0
@loop@interior db "loop interior", 0
@10 dd 10
@1 dd 1
@sale@de@loop@inerior db "sale de loop inerior", 0
@5 dd 5
@fin db "fin", 0
@aux0 dd ? 
@aux1 dd ? 
.code
start:
L0:
invoke MessageBox, NULL, addr @loop@exterior, addr @loop@exterior, MB_OK 
L3:
invoke MessageBox, NULL, addr @loop@interior, addr @loop@interior, MB_OK 
JMP L20
MOV ECX, @10
CMP _i@prueba_nombre_programa, ECX
JA L20
MOV ECX, _i@prueba_nombre_programa
ADD ECX, @1
MOV @aux0, ECX
MOV ECX, @aux0
MOV _i@prueba_nombre_programa, ECX
JMP L3
L20:
invoke MessageBox, NULL, addr @sale@de@loop@inerior, addr @sale@de@loop@inerior, MB_OK 
JMP L37
MOV ECX, @5
CMP _j@prueba_nombre_programa, ECX
JA L37
MOV ECX, _j@prueba_nombre_programa
ADD ECX, @1
MOV @aux1, ECX
MOV ECX, @aux1
MOV _j@prueba_nombre_programa, ECX
JMP L0
L37:
invoke MessageBox, NULL, addr @fin, addr @fin, MB_OK 
invoke ExitProcess, 0
end start
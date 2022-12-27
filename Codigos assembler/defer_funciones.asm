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
_var1@prueba_nombre_programa dw ? 
_var2@prueba_nombre_programa dw ? 
@1 dw 1
@5 dw 5
@defer@do db "defer do", 0
@mayor db "mayor", 0
@aux0 dw ? 
.code
start:
MOV CX, @1
MOV _var1@prueba_nombre_programa, CX
MOV CX, @5
MOV _var2@prueba_nombre_programa, CX
L6:
invoke MessageBox, NULL, addr @mayor, addr @mayor, MB_OK 
MOV CX, _var2@prueba_nombre_programa
CMP _var1@prueba_nombre_programa, CX
JE L25
MOV CX, _var1@prueba_nombre_programa
ADD CX, @1
MOV @aux0, CX
MOV CX, @aux0
MOV _var1@prueba_nombre_programa, CX
JMP L6
L25:
invoke MessageBox, NULL, addr @defer@do, addr @defer@do, MB_OK 
invoke ExitProcess, 0
end start
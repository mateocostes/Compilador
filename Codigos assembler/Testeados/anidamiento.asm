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
_funcion1@prueba_nombre_programa dd 4
_var2@prueba_nombre_programa@funcion1 dd ? 
@1 dd 1
@6 dd 6
@es@menor@a@6 db "es menor a 6", 0
@entro@al@do db "entro al do", 0
@es@mayor@o@igual@a@6 db "es mayor o igual a 6", 0
@entro db "entro", 0
@0 dd 0
@8 dd 8
@es@igual@a@8 db "es igual a 8", 0
@es@distinto@a@8 db "es distinto a 8", 0
@10 dd 10
@4 dd 4
@es@igual@a@4 db "es igual a 4", 0
@es@distinto@a@4 db "es distinto a 4", 0
@aux0 dd ? 
@aux1 dd ? 
@aux2 dd ? 
@aux3 dd ? 
@aux4 dd ? 
.code
funcion1@prueba_nombre_programa:
MOV ECX, _var2@prueba_nombre_programa@funcion1
ADD ECX, @1
MOV @aux0, ECX
MOV ECX, @aux0
MOV _var2@prueba_nombre_programa@funcion1, ECX
MOV ECX, @6
CMP _var2@prueba_nombre_programa@funcion1, ECX
JAE L37
invoke MessageBox, NULL, addr @es@menor@a@6, addr @es@menor@a@6, MB_OK 
L14:
MOV ECX, _var2@prueba_nombre_programa@funcion1
ADD ECX, @1
MOV @aux1, ECX
MOV ECX, @aux1
MOV _var2@prueba_nombre_programa@funcion1, ECX
invoke MessageBox, NULL, addr @entro@al@do, addr @entro@al@do, MB_OK 
MOV ECX, @6
CMP _var2@prueba_nombre_programa@funcion1, ECX
JA L34
MOV ECX, _var2@prueba_nombre_programa@funcion1
ADD ECX, @1
MOV @aux2, ECX
MOV ECX, @aux2
MOV _var2@prueba_nombre_programa@funcion1, ECX
JMP L14
L34:
JMP L61
L37:
invoke MessageBox, NULL, addr @es@mayor@o@igual@a@6, addr @es@mayor@o@igual@a@6, MB_OK 
L40:
MOV ECX, _var2@prueba_nombre_programa@funcion1
SUB ECX, @1
CMP ECX, 00h
JGE aux3
invoke MessageBox, NULL, addr @ERROR_RESTA_NEGATIVA, addr @ERROR_RESTA_NEGATIVA, MB_OK
invoke ExitProcess, 0
aux3:
MOV @aux3, ECX
MOV ECX, @aux3
MOV _var2@prueba_nombre_programa@funcion1, ECX
invoke MessageBox, NULL, addr @entro, addr @entro, MB_OK 
MOV ECX, @6
CMP _var2@prueba_nombre_programa@funcion1, ECX
JNAE L60
MOV ECX, _var2@prueba_nombre_programa@funcion1
SUB ECX, @1
CMP ECX, 00h
JGE aux4
invoke MessageBox, NULL, addr @ERROR_RESTA_NEGATIVA, addr @ERROR_RESTA_NEGATIVA, MB_OK
invoke ExitProcess, 0
aux4:
MOV @aux4, ECX
MOV ECX, @aux4
MOV _var2@prueba_nombre_programa@funcion1, ECX
JMP L40
L60:
L61:
RET
start:
MOV ECX, @0
MOV _var2@prueba_nombre_programa@funcion1, ECX
CALL funcion1@prueba_nombre_programa
MOV ECX, _var2@prueba_nombre_programa@funcion1
MOV _var1@prueba_nombre_programa, ECX
MOV ECX, @8
CMP _var1@prueba_nombre_programa, ECX
JNE L78
invoke MessageBox, NULL, addr @es@igual@a@8, addr @es@igual@a@8, MB_OK 
JMP L81
L78:
invoke MessageBox, NULL, addr @es@distinto@a@8, addr @es@distinto@a@8, MB_OK 
L81:
MOV ECX, @10
MOV _var2@prueba_nombre_programa@funcion1, ECX
CALL funcion1@prueba_nombre_programa
MOV ECX, _var2@prueba_nombre_programa@funcion1
MOV _var2@prueba_nombre_programa, ECX
MOV ECX, @4
CMP _var2@prueba_nombre_programa, ECX
JNE L96
invoke MessageBox, NULL, addr @es@igual@a@4, addr @es@igual@a@4, MB_OK 
JMP L99
L96:
invoke MessageBox, NULL, addr @es@distinto@a@4, addr @es@distinto@a@4, MB_OK 
L99:
invoke ExitProcess, 0
end start
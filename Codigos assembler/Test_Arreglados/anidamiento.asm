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
@entro@al@do@if db "entro al do if", 0
@salio@del@do db "salio del do", 0
@es@mayor@o@igual@a@6 db "es mayor o igual a 6", 0
@entro@al@do@else db "entro al do else", 0
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
JAE L41
invoke MessageBox, NULL, addr @es@menor@a@6, addr @es@menor@a@6, MB_OK 
L14:
MOV ECX, _var2@prueba_nombre_programa@funcion1
ADD ECX, @1
MOV @aux1, ECX
MOV ECX, @aux1
MOV _var2@prueba_nombre_programa@funcion1, ECX
invoke MessageBox, NULL, addr @entro@al@do@if, addr @entro@al@do@if, MB_OK 
JMP L36
MOV ECX, @6
CMP _var2@prueba_nombre_programa@funcion1, ECX
JA L36
MOV ECX, _var2@prueba_nombre_programa@funcion1
ADD ECX, @1
MOV @aux2, ECX
MOV ECX, @aux2
MOV _var2@prueba_nombre_programa@funcion1, ECX
JMP L14
L36:
invoke MessageBox, NULL, addr @salio@del@do, addr @salio@del@do, MB_OK 
JMP L65
L41:
invoke MessageBox, NULL, addr @es@mayor@o@igual@a@6, addr @es@mayor@o@igual@a@6, MB_OK 
L44:
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
invoke MessageBox, NULL, addr @entro@al@do@else, addr @entro@al@do@else, MB_OK 
MOV ECX, @6
CMP _var2@prueba_nombre_programa@funcion1, ECX
JNAE L64
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
JMP L44
L64:
L65:
RET
start:
MOV ECX, @0
MOV _var2@prueba_nombre_programa@funcion1, ECX
CALL funcion1@prueba_nombre_programa
MOV ECX, _var2@prueba_nombre_programa@funcion1
MOV _var1@prueba_nombre_programa, ECX
MOV ECX, @8
CMP _var1@prueba_nombre_programa, ECX
JNE L82
invoke MessageBox, NULL, addr @es@igual@a@8, addr @es@igual@a@8, MB_OK 
JMP L85
L82:
invoke MessageBox, NULL, addr @es@distinto@a@8, addr @es@distinto@a@8, MB_OK 
L85:
MOV ECX, @10
MOV _var2@prueba_nombre_programa@funcion1, ECX
CALL funcion1@prueba_nombre_programa
MOV ECX, _var2@prueba_nombre_programa@funcion1
MOV _var2@prueba_nombre_programa, ECX
MOV ECX, @4
CMP _var2@prueba_nombre_programa, ECX
JNE L100
invoke MessageBox, NULL, addr @es@igual@a@4, addr @es@igual@a@4, MB_OK 
JMP L103
L100:
invoke MessageBox, NULL, addr @es@distinto@a@4, addr @es@distinto@a@4, MB_OK 
L103:
invoke ExitProcess, 0
end start
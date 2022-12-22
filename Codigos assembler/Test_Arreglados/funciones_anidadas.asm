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
_var3@prueba_nombre_programa dd ? 
_fun1@prueba_nombre_programa dd 5
_a@prueba_nombre_programa@fun1 dd ? 
_fun2@prueba_nombre_programa@fun1 dd 7
_b@prueba_nombre_programa@fun1@fun2 dd ? 
@fun2 db "fun2", 0
_fun3@prueba_nombre_programa@fun1@fun2 dd 10
_c@prueba_nombre_programa@fun1@fun2@fun3 dd ? 
@fun3 db "fun3", 0
@1 dd 1
@2 dd 2
@es@2 db "es 2", 0
@no@es@2 db "no es 2", 0
@fun1 db "fun1", 0
@4 dd 4
@es@4 db "es 4", 0
@no@es@4 db "no es 4", 0
@10 dd 10
@es@10 db "es 10", 0
@no@es@10 db "no es 10", 0
@fin db "fin", 0
@aux0 dd ? 
@aux2 dd ? 
@aux4 dd ? 
@aux5 dd ? 
.code
fun3@prueba_nombre_programa@fun1@fun2:
invoke MessageBox, NULL, addr @fun3, addr @fun3, MB_OK 
MOV ECX, _c@prueba_nombre_programa@fun1@fun2@fun3
ADD ECX, @1
MOV @aux0, ECX
RET
fun2@prueba_nombre_programa@fun1:
invoke MessageBox, NULL, addr @fun2, addr @fun2, MB_OK 
MOV EAX, _fun3@prueba_nombre_programa@fun1@fun2
MOV EBX, _fun2@prueba_nombre_programa@fun1
CMP EBX, EAX
JNE @aux1
invoke MessageBox, NULL, addr @ERROR_INVOCACION, addr @ERROR_INVOCACION, MB_OK
invoke ExitProcess, 0
@aux1:
MOV ECX, @1
MOV _c@prueba_nombre_programa@fun1@fun2@fun3, ECX
CALL fun3@prueba_nombre_programa@fun1@fun2
MOV ECX, @aux0
MOV _var3@prueba_nombre_programa, ECX
MOV ECX, @2
CMP _var3@prueba_nombre_programa, ECX
JNE L28
invoke MessageBox, NULL, addr @es@2, addr @es@2, MB_OK 
JMP L31
L28:
invoke MessageBox, NULL, addr @no@es@2, addr @no@es@2, MB_OK 
L31:
MOV EAX, _b@prueba_nombre_programa@fun1@fun2
MUL _var3@prueba_nombre_programa
MOV @aux2, EAX
RET
fun1@prueba_nombre_programa:
invoke MessageBox, NULL, addr @fun1, addr @fun1, MB_OK 
MOV EAX, _fun2@prueba_nombre_programa@fun1
MOV EBX, _fun1@prueba_nombre_programa
CMP EBX, EAX
JNE @aux3
invoke MessageBox, NULL, addr @ERROR_INVOCACION, addr @ERROR_INVOCACION, MB_OK
invoke ExitProcess, 0
@aux3:
MOV ECX, @2
MOV _b@prueba_nombre_programa@fun1@fun2, ECX
CALL fun2@prueba_nombre_programa@fun1
MOV ECX, @aux2
MOV _var2@prueba_nombre_programa, ECX
MOV ECX, @4
CMP _var2@prueba_nombre_programa, ECX
JNE L52
invoke MessageBox, NULL, addr @es@4, addr @es@4, MB_OK 
JMP L55
L52:
invoke MessageBox, NULL, addr @no@es@4, addr @no@es@4, MB_OK 
L55:
MOV ECX, _var2@prueba_nombre_programa
ADD ECX, _a@prueba_nombre_programa@fun1
MOV @aux4, ECX
RET
start:
MOV ECX, @1
MOV _a@prueba_nombre_programa@fun1, ECX
CALL fun1@prueba_nombre_programa
MOV EAX, @aux4
MUL @2
MOV @aux5, EAX
MOV ECX, @aux5
MOV _var1@prueba_nombre_programa, ECX
MOV ECX, @10
CMP _var1@prueba_nombre_programa, ECX
JNE L76
invoke MessageBox, NULL, addr @es@10, addr @es@10, MB_OK 
JMP L79
L76:
invoke MessageBox, NULL, addr @no@es@10, addr @no@es@10, MB_OK 
L79:
invoke MessageBox, NULL, addr @fin, addr @fin, MB_OK 
invoke ExitProcess, 0
end start
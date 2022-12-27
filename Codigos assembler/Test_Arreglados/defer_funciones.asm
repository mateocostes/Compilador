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
_funcion1@prueba_nombre_programa dw 3
_var2@prueba_nombre_programa@funcion1 dw ? 
@funcion@1 db "funcion 1", 0
_funcion2@prueba_nombre_programa@funcion1 dw 6
_var2@prueba_nombre_programa@funcion1@funcion2 dw ? 
@defer@funcion2 db "defer funcion2", 0
@funcion@2 db "funcion 2", 0
@1 dw 1
@defer@funcion@1 db "defer funcion 1", 0
@salio@funcion@2 db "salio funcion 2", 0
@defer@fin db "defer fin", 0
@fin db "fin", 0
.code
funcion2@prueba_nombre_programa@funcion1:
invoke MessageBox, NULL, addr @funcion@2, addr @funcion@2, MB_OK 
invoke MessageBox, NULL, addr @defer@funcion2, addr @defer@funcion2, MB_OK 
RET
funcion1@prueba_nombre_programa:
invoke MessageBox, NULL, addr @funcion@1, addr @funcion@1, MB_OK 
MOV AX, _funcion2@prueba_nombre_programa@funcion1
MOV BX, _funcion1@prueba_nombre_programa
CMP BX, AX
JNE @aux0
invoke MessageBox, NULL, addr @ERROR_INVOCACION, addr @ERROR_INVOCACION, MB_OK
invoke ExitProcess, 0
@aux0:
MOV CX, @1
MOV _var2@prueba_nombre_programa@funcion1@funcion2, CX
CALL funcion2@prueba_nombre_programa@funcion1
MOV CX, _var2@prueba_nombre_programa@funcion1@funcion2
MOV _var1@prueba_nombre_programa, CX
invoke MessageBox, NULL, addr @salio@funcion@2, addr @salio@funcion@2, MB_OK 
invoke MessageBox, NULL, addr @defer@funcion@1, addr @defer@funcion@1, MB_OK 
RET
start:
MOV CX, @1
MOV _var2@prueba_nombre_programa@funcion1, CX
CALL funcion1@prueba_nombre_programa
MOV CX, _var2@prueba_nombre_programa@funcion1
MOV _var1@prueba_nombre_programa, CX
invoke MessageBox, NULL, addr @fin, addr @fin, MB_OK 
invoke MessageBox, NULL, addr @defer@fin, addr @defer@fin, MB_OK 
invoke ExitProcess, 0
end start
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
@aux0 dd ? 
@aux1 dd ? 
.code
start:
MOV ECX, @1
MOV _var1@prueba_nombre_programa, ECX
MOV ECX, _var1@prueba_nombre_programa
SUB ECX, @1
CMP ECX, 00h
JGE aux0
invoke MessageBox, NULL, addr @ERROR_RESTA_NEGATIVA, addr @ERROR_RESTA_NEGATIVA, MB_OK
invoke ExitProcess, 0
aux0:
MOV @aux0, ECX
MOV ECX, @aux0
MOV _var2@prueba_nombre_programa, ECX
CMP _var2@prueba_nombre_programa, 00h
JNE aux1
invoke MessageBox, NULL, addr @ERROR_DIVISION_POR_CERO, addr @ERROR_DIVISION_POR_CERO, MB_OK
invoke ExitProcess, 0
aux1:
MOV EAX, _var1@prueba_nombre_programa
DIV _var2@prueba_nombre_programa
MOV @aux1, EAX
MOV ECX, @aux1
MOV _var1@prueba_nombre_programa, ECX
invoke ExitProcess, 0
end start
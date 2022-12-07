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
@ERROR_OVERFLOW_PRODUCTO db "ERROR: Overflow en operacion de producto", 0
@ERROR_INVOCACION db "ERROR: Invocacion de funcion a si misma no permitida", 0
imprime@1 db "imprime 1", 0
imprime@2 db "imprime 2", 0
imprime@3 db "imprime 3", 0
.code
start:
invoke MessageBox, NULL, addr imprime@1, addr imprime@1, MB_OK 
invoke MessageBox, NULL, addr imprime@3, addr imprime@3, MB_OK 
invoke MessageBox, NULL, addr imprime@2, addr imprime@2, MB_OK 
invoke ExitProcess, 0
end start
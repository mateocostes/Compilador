package GeneracionCodigo;

import java.util.*;

import AnalizadorLexico.*;

public class Assembler {
    public static StringBuilder codigo = new StringBuilder();

    private static final Stack<String> pila_tokens = new Stack<>();

    private static String ultimaComparacion = "";

    private static int auxiliarDisponible = 0;

    public static int posicionActualPolaca = 0;

    public static String ultimaFuncionLlamada = "";

    public static int pos_start;

    public static boolean tipo_tof64 = false;

    public static StringBuilder conv_exp = new StringBuilder();

    public static ArrayList<Object> codigoDefer = new ArrayList<Object>();
    public static ArrayList<Object> funciones = new ArrayList<Object>();

    private static String nombreAux2bytes = "@aux2bytes"; 

    private static boolean error_generacion_codigo = false;

    private static boolean existen_errores = false;

    private static final String ERROR_DIVISION_POR_CERO = "ERROR: Division por cero";   //strings de error constantes en el codigo
    private static final String ERROR_RESTA_NEGATIVA = "ERROR: Resultados negativos en restas de enteros sin signo";   
    private static final String ERROR_INVOCACION = "ERROR: Invocacion de funcion a si misma no permitida";
    
    public static void generarCodigo() {
        boolean agrego_start = false;
        RestructurarPolacaFunciones();
        //funcion principal que genera el codigo del programa, utilizando los tokes de la pocala y simbolos de la respectiva tabla
        for (int indice = 0; indice < Polaca.polaca.size(); indice++){
            if (Main.erroresLexico.isEmpty() && Main.erroresSintacticos.isEmpty() && Main.erroresSemanticos.isEmpty() && !error_generacion_codigo){

                if ((indice == pos_start)){
                    codigo.append("start:\n");
                    agrego_start = true;
                    ultimaFuncionLlamada = "";
                }

                String token = String.valueOf(Polaca.polaca.get(indice));
                switch (token) {
                    case "*":
                    case "+":
                    case "=:":  
                    case "-":
                    case "/":
                    case ">=":
                    case ">":   
                    case "<=":
                    case "<":
                    case "=!":
                    case "=":
                        generarOperador(token);
                        break;
                    case "#BI":
                        generarSalto("JMP", "#BI");
                        break;
                    case "#BF":
                        generarSalto(ultimaComparacion, "#BF");
                        break;
                    case "#BT":
                        generarSalto(negacion(ultimaComparacion), "#BT");
                        break;
                    case "#CALL":
                        generarLlamadoFuncion("#CALL");
                        break;
                    case "#DISCARD":
                        generarLlamadoFuncion("#DISCARD");
                        break;
                    case "#RET":
                        generarCodigoRetorno();
                        break;
                    case "#OUT":
                        String cadena = '@' + pila_tokens.pop().replace(' ', '@');
                        codigo.append("invoke MessageBox, NULL, addr ").append(cadena).append(", addr ").append(cadena).append(", MB_OK \n");
                        break;
                    case "#FUN":
                        generarCabeceraFuncion();
                        break;
                    case "#DEFER":
                        generarCodigoDefer(indice);
                        indice--;
                        break;
                    case "#EJECDEFER":
                        generarCodigoEjecucionDefer(indice);
                        indice--;
                        pos_start = pos_start -3; //Se actualiza la posicion del start sin las 3 marcas del defer
                        break;
                    case "#TOF64":
                        tipo_tof64 = true;
                        String var = pila_tokens.pop();
                        pila_tokens.push(var + "." + "#tof64");
                        break;
                    default:
                        if (token.startsWith(":")) {   //encontramos un label
                            codigo.append(token.substring(1)).append(":\n");
                        } 
                        else {
                            pila_tokens.push(token);
                        }
                        break;
                }
            }
            else{
                existen_errores = true;
            }
        }

        if (existen_errores){
            System.out.println("Se detectaron errores, se aborto la compilacion \n");
        }
        else{
            if (!agrego_start) //Se utiliza cuando no hay sentencias en el bloque principal
            codigo.append("start:\n");

            codigo.append("invoke ExitProcess, 0\n")
                .append("end start");

            generarCabecera();
        }
    }

    private static void generarCabecera() {
    //funcion encargada de la generacion de la cabecera del codigo
        StringBuilder cabecera = new StringBuilder();

        cabecera.append(".386\n")
            .append(".model flat, stdcall\n")
            .append("option casemap :none\n")
            .append("include \\masm32\\include\\windows.inc\n")
            .append("include \\masm32\\include\\kernel32.inc\n")
            .append("include \\masm32\\include\\user32.inc\n")
            .append("includelib \\masm32\\lib\\kernel32.lib\n")
            .append("includelib \\masm32\\lib\\user32.lib\n")
            .append(".data\n")
            .append(nombreAux2bytes).append(" dw ? \n")
            //agregamos las constantes de error
            .append("@ERROR_DIVISION_POR_CERO db \"" + ERROR_DIVISION_POR_CERO + "\", 0\n")
            .append("@ERROR_RESTA_NEGATIVA db \"" + ERROR_RESTA_NEGATIVA + "\", 0\n")
            .append("@ERROR_INVOCACION db \"" + ERROR_INVOCACION + "\", 0\n");

        generarCodigoDatos(cabecera);

        cabecera.append(".code\n");
        cabecera.append(codigo);
        codigo = cabecera;
    }

    private static void generarCodigoDatos(StringBuilder cabecera) {
        //funcion utilizada para generar el codigo necesario para todos los datos del programa, presentes en la tabla de simbolos
        for (int simbolo : TablaSimbolos.obtenerConjuntoPunteros()) {
            String uso = TablaSimbolos.obtenerAtributo(simbolo, "uso");
            String tipo_actual = TablaSimbolos.obtenerAtributo(simbolo, "tipo");
            String lexema_actual = TablaSimbolos.obtenerAtributo(simbolo, "lexema");
            if (tipo_actual==null) continue;
            switch (tipo_actual) {
                case Tipos.CADENA_TYPE:
                    cabecera.append('@').append(lexema_actual.replace(' ', '@')).append(" db \"").append(lexema_actual).append("\", 0\n");
                    break;
                
                case Tipos.UI16_TYPE:
                    if (uso.equals("constante")) {
                        String lexema = lexema_actual;
                        lexema_actual = "@" + lexema_actual;
                        cabecera.append(lexema_actual.replace(' ', '@')).append(" dw ").append(lexema).append("\n");
                    } 
                    else {
                        if (!lexema_actual.startsWith("@")) {
                            cabecera.append("_");
                        }
                        if (uso.equals("funcion"))
                            cabecera.append(lexema_actual.replace('.', '@')).append(" dw ").append(TablaSimbolos.obtenerClave(lexema_actual)).append("\n");
                        else
                            cabecera.append(lexema_actual.replace('.', '@')).append(" dw ? \n");
                    }   
                    break;
                
                case Tipos.F64_TYPE:        //en caso que el simbolo de tipo double y sea una constante
                    if (uso.equals("constante")) {
                        String lexema = lexema_actual;
                        if (lexema_actual.charAt(0) == '.')
                            lexema = "0" + lexema;
                        lexema_actual = "@" + lexema_actual.replace('.', '@').replace('-', '@').replace('+', '@');  //cambiamos el punto por una @ 
                        cabecera.append(lexema_actual.replace(' ', '@')).append(" dq ").append(lexema).append("\n");   //y agregamos el simbolo a la cabecera con REAL4
                    } else {
                        if (! lexema_actual.startsWith("@")) {
                            cabecera.append("_");
                        }
                        if (uso.equals("funcion"))
                            cabecera.append(lexema_actual.replace('.', '@')).append(" dq ").append(TablaSimbolos.obtenerClave(lexema_actual)).append("\n");
                        else
                        cabecera.append(lexema_actual.replace('.', '@')).append(" dq ?\n");
                    }   
                    break;
            }
        }
    }

    private static void generarCabeceraFuncion() {
        ultimaFuncionLlamada = pila_tokens.pop();
        codigo.append(ultimaFuncionLlamada.replace('.', '@')).append(":").append("\n");
    }

    public static void generarOperador(String operador) {
        String op2 = pila_tokens.pop();   
        String op1 = pila_tokens.pop();
        if (operador.equals("=:")) { //Si el operador es =: entonces los operandos estan al reves por como esta hecha la gramatica
            String aux = op1;
            op1 = op2;
            op2 = aux;
        }
        if (!tipo_tof64){
            String tipo = Tipos.getTipoOperacion(op1, op2, operador);
            switch (tipo) {
                case Tipos.UI16_TYPE:
                    generarOperacionEnteros(op1, op2, operador);
                    break;
                case Tipos.F64_TYPE:
                    generarOperacionFlotantes(op1, op2, operador);
                    break;
                default:
                    System.out.println("Error en la generacion de codigo: Incompatibilidad de tipos en la sentencia: " + eliminarAmbito(op1) + " " + operador + " " + eliminarAmbito(op2) + "\n");
                    error_generacion_codigo = true;
                    break;
            }
        }
        else{
            tipo_tof64 = false;
            if (((op1.contains("#tof64")) && (Tipos.getTipo(op2).equals(Tipos.UI16_TYPE))) || ((op2.contains("#tof64")) && (Tipos.getTipo(op1).equals(Tipos.UI16_TYPE)))){
                System.out.println("Error en la generacion de codigo: Incompatibilidad de tipos en la sentencia: " + eliminarAmbito(op1) + " " + operador + " " + eliminarAmbito(op2) + "\n");
                error_generacion_codigo = true;
            }
            else{
                if (op1.contains("#tof64")) {
                    int pos = op1.lastIndexOf(".");
                    op1 = op1.substring(0, pos);
                    conv_exp.append("FILD ").append(renombre(op1)).append("\n");
                }
                if (op2.contains("#tof64")) {
                    int pos = op2.lastIndexOf(".");
                    op2 = op2.substring(0, pos);
                    conv_exp.append("FILD ").append(renombre(op2)).append("\n");
                }
                generarOperacionFlotantes(op1, op2, operador);
            }
        }
    }

    private static void generarErrorDivCero(String aux){
        // genera el codigo necesario ante un error de division por cero
        codigo.append("JNE ").append(aux.substring(1)).append("\n");
        codigo.append("invoke MessageBox, NULL, addr @ERROR_DIVISION_POR_CERO, addr @ERROR_DIVISION_POR_CERO, MB_OK\n");
        codigo.append("invoke ExitProcess, 0\n");
        codigo.append(aux.substring(1)).append(":\n"); //declaro una label        
    }

    private static void generarErrorRestaNegativa(String aux){
        codigo.append("JGE ").append(aux.substring(1)).append("\n");    
        codigo.append("invoke MessageBox, NULL, addr @ERROR_RESTA_NEGATIVA, addr @ERROR_RESTA_NEGATIVA, MB_OK\n");
        codigo.append("invoke ExitProcess, 0\n");
        codigo.append(aux.substring(1)).append(":\n"); //declaro una label        
    }

    private static void generarErrorInvocacion(String funcion, String funcion_actual) {
        codigo.append("MOV AX, ").append('_').append(funcion.replace('.', '@')).append("\n");
        String label = "@aux" + auxiliarDisponible;
        ++auxiliarDisponible;
        codigo.append("MOV BX, ").append('_').append(funcion_actual.replace('.', '@')).append("\n");
        codigo.append("CMP BX, AX\n");
        codigo.append("JNE ").append(label).append("\n");
        codigo.append("invoke MessageBox, NULL, addr @ERROR_INVOCACION, addr @ERROR_INVOCACION, MB_OK\n");
        codigo.append("invoke ExitProcess, 0\n");
        codigo.append(label).append(":\n"); //declaro una label        
    }

    private static void generarOperacionEnteros(String op1, String op2, String operador) {
        op1 = renombre(op1);
        op2 = renombre(op2); 

        String aux;

        switch (operador) {
            case "+":
                codigo.append("MOV CX, ").append(op1).append("\n"); //muevo siempre al registro CX ya que al usar auxiliares nunca voy a gastar mas de 1 registro, ademas este registro no es usado por las divisiones
                codigo.append("ADD CX, ").append(op2).append("\n");
                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("MOV ").append(aux).append(", CX\n");
                pila_tokens.push(aux);
                break;
            case "-":
                codigo.append("MOV CX, ").append(op1).append("\n"); //muevo siempre al registro CX ya que al usar auxiliares nunca voy a gastar mas de 1 registro, ademas este registro no es usado por las divisiones
                codigo.append("SUB CX, ").append(op2).append("\n");
                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("CMP ").append("CX,").append(" 00h\n"); 
                generarErrorRestaNegativa(aux);
                codigo.append("MOV ").append(aux).append(", CX\n");
                pila_tokens.push(aux);
                break;
            case "*":
                codigo.append("MOV AX, ").append(op1).append("\n"); //muevo al registro AX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("MUL ").append(op2).append("\n");
                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("MOV ").append(aux).append(", AX\n");
                pila_tokens.push(aux);
                break;
            case "=:":
                codigo.append("MOV CX, ").append(op2).append("\n"); //muevo al registro AX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("MOV ").append(op1).append(", CX\n");
                break;
            case "/":   
                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("CMP ").append(op2).append(", 00h\n"); 
                generarErrorDivCero(aux);
                codigo.append("MOV AX, ").append(op1).append("\n"); //el dividendo debe estar en AX
                codigo.append("MOV DX, 0").append("\n");
                codigo.append("DIV ").append(op2).append("\n");
                codigo.append("MOV ").append(aux).append(", AX\n");
                pila_tokens.push(aux);
                break;
            
            case "=":
                codigo.append("MOV CX, ").append(op2).append("\n"); 
                codigo.append("CMP ").append(op1).append(", CX\n");
                ultimaComparacion = "JNE";
                break;
            case "=!":
                codigo.append("MOV CX, ").append(op2).append("\n"); 
                codigo.append("CMP ").append(op1).append(", CX\n");
                ultimaComparacion = "JE";
                break;
            case ">=":
                codigo.append("MOV CX, ").append(op2).append("\n"); //muevo al registro AX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("CMP ").append(op1).append(", CX\n");
                ultimaComparacion = "JB";
                break;
            
            case ">":
                codigo.append("MOV CX, ").append(op2).append("\n"); //muevo al registro AX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("CMP ").append(op1).append(", CX\n");
                ultimaComparacion = "JNA";
                break;
            
            case "<=":
                codigo.append("MOV CX, ").append(op2).append("\n"); //muevo al registro AX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("CMP ").append(op1).append(", CX\n");
                ultimaComparacion = "JA";
                break;
            
            case "<":
                codigo.append("MOV CX, ").append(op2).append("\n"); //muevo al registro AX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("CMP ").append(op1).append(", CX\n");
                ultimaComparacion = "JAE";
                break;
            
            default:
                codigo.append("ERROR, se entro a default en operacion de enteros").append("\n");
                break;
        }
    }

    //BORRAR AUXILIARES
    private static void generarOperacionFlotantes(String op1, String op2, String operador) { 
        op1 = renombre(op1);
        op2 = renombre(op2);

        String aux;

        switch (operador) {
            //nunca  va a llegar una operacion AND o OR entre doubles ya que al finalizar cada condicion guardo un UINT con el resultado de la condicion.
            case "+":
                codigo.append("FLD ").append(op2).append("\n"); //apilo primero el op2 ya que quiero que me quede como el segundo que agarro para las operaciones que no son conmutativas
                codigo.append("FLD ").append(op1).append("\n");

                if (conv_exp.length() > 0){
                    codigo.append(conv_exp);
                    conv_exp.delete(0, conv_exp.length());
                }

                codigo.append("FADD\n");
                aux = ocuparAuxiliar(Tipos.F64_TYPE);
                codigo.append("FSTP ").append(aux).append("\n");
                pila_tokens.push(aux);
                break;

            case "-":
                codigo.append("FLD ").append(op2).append("\n"); //apilo primero el op2 ya que quiero que me quede como el segundo que agarro para las operaciones que no son conmutativas
                codigo.append("FLD ").append(op1).append("\n");

                if (conv_exp.length() > 0){
                    codigo.append(conv_exp);
                    conv_exp.delete(0, conv_exp.length());
                }

                codigo.append("FSUB\n");
                aux = ocuparAuxiliar(Tipos.F64_TYPE);
                codigo.append("FSTP ").append(aux).append("\n");
                pila_tokens.push(aux);
                break;
            
            case "*":
                codigo.append("FLD ").append(op2).append("\n"); //apilo primero el op2 ya que quiero que me quede como el segundo que agarro para las operaciones que no son conmutativas
                codigo.append("FLD ").append(op1).append("\n");

                if (conv_exp.length() > 0){
                    codigo.append(conv_exp);
                    conv_exp.delete(0, conv_exp.length());
                }
                
                codigo.append("FMUL\n");
                aux = ocuparAuxiliar(Tipos.F64_TYPE);
                codigo.append("FSTP ").append(aux).append("\n");
                pila_tokens.push(aux);
                break;
            
            case "=:":
                codigo.append("FLD ").append(op2).append("\n");

                if (conv_exp.length() > 0){
                    codigo.append(conv_exp);
                    conv_exp.delete(0, conv_exp.length());
                }

                codigo.append("FSTP ").append(op1).append("\n");
                break;
            
            case "/":
                aux = ocuparAuxiliar(Tipos.F64_TYPE);
                codigo.append("FLD ").append(op2).append("\n"); //cargo el operando dos para luego compararlo con cero
                
                if (conv_exp.length() > 0){
                    codigo.append(conv_exp);
                    conv_exp.delete(0, conv_exp.length());
                }
                //guardar 00h en una variable auxiliar
                String _cero = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("MOV ").append(_cero).append(", 00h\n");
                codigo.append("FCOM " + _cero + "\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                generarErrorDivCero(aux);

                codigo.append("FLD ").append(op2).append("\n"); //apilo primero el op2 ya que quiero que me quede como el segundo que agarro para las operaciones que no son conmutativas
                codigo.append("FLD ").append(op1).append("\n");
                codigo.append("FDIV\n");
                codigo.append("FSTP ").append(aux).append("\n");
                pila_tokens.push(aux);
                break;
            
            case "=":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JE " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); //creo una label para que salte y se saltee la instruccion de poner aux en cero en caso de que sea verdadera
                pila_tokens.push(aux);
                ultimaComparacion = "JNE";
                break;
            case "=!":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JNE " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); //creo una label para que salte y se saltee la instruccion de poner aux en cero en caso de que sea verdadera
                pila_tokens.push(aux);
                ultimaComparacion = "JE";
                break;
            
            case ">=":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JAE " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); 
                pila_tokens.push(aux);
                ultimaComparacion = "JB";
                break;
            
            case ">":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n"); 
                codigo.append("JA " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); //creo una label para que salte y se saltee la instruccion de poner aux en cero en caso de que sea verdadera
                pila_tokens.push(aux);
                ultimaComparacion = "JNA";
                break;
            
            case "<=":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JBE " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); 
                pila_tokens.push(aux);
                ultimaComparacion = "JA";
                break;
            
            case "<":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(Tipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JB " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); 
                pila_tokens.push(aux);
                ultimaComparacion = "JAE";
                break;
            
            default:
                codigo.append("ERROR se entro a default al generar codigo para una operacion de flotantes\n");
                break;
        }
    }

    private static void generarSalto(String salto, String token) {
        String direccion = pila_tokens.pop();    
        codigo.append(salto).append(" L").append(direccion).append("\n");
        if (token.equals("#BF") || token.equals("#BT"))
            ultimaComparacion = "";
    }

    private static void generarLlamadoFuncion(String token) {
        String funcion = pila_tokens.pop();
        if (ultimaFuncionLlamada != "") //Esta en una declaracion de funcion
            generarErrorInvocacion(funcion, ultimaFuncionLlamada);
        int clave_funcion = TablaSimbolos.obtenerClave(funcion);
        int cant_parametros_funcion = Integer.parseInt(TablaSimbolos.obtenerAtributo(clave_funcion, "cantidad de parametros"));
        switch (cant_parametros_funcion){
            case 0:
                //La funcion no tiene parametros
                break;
            case 1: 
                String parametro_formal = TablaSimbolos.obtenerAtributo(clave_funcion, "parametro_1");
                pila_tokens.push(parametro_formal);
                generarOperador("=:");
                break;

            case 2:
                    String parametro_real_2 = pila_tokens.pop(); //Se ordenan de acuerdo al orden original
                    String parametro_real_1 = pila_tokens.pop();
                    String parametro_formal_1 = TablaSimbolos.obtenerAtributo(clave_funcion, "parametro_1");
                    String parametro_formal_2 = TablaSimbolos.obtenerAtributo(clave_funcion, "parametro_2");
                    pila_tokens.push(parametro_real_1);
                    pila_tokens.push(parametro_formal_1);
                    generarOperador("=:");
                    pila_tokens.push(parametro_real_2);
                    pila_tokens.push(parametro_formal_2);
                    generarOperador("=:");
                    break;
        }
        codigo.append("CALL ").append(funcion.replace('.', '@')).append("\n");
        if (token.equals("#CALL"))
            pila_tokens.push(TablaSimbolos.obtenerAtributo(clave_funcion, "retorno"));
            //En caso de ser token = #DISCARD no pusheo el retorno de la funcion
    }

    private static String renombre(String token) {
        char caracter = token.charAt(0);
        int puntToken = TablaSimbolos.obtenerClave(token);

        // Si es una constante, le cambio de nombre al cual fue declarada
        if (TablaSimbolos.obtenerAtributo(puntToken, "uso").equals("constante")) {
            return "@" + token.replace('.', '@').replace('-', '@').replace('+', '@');
        } else if (Character.isLowerCase(caracter) || Character.isUpperCase(caracter)) {
            return "_" + token.replace('.', '@').replace('-', '@').replace('+', '@');
        } else {
            return token;
        }
    }

    private static String negacion(String comparacion) {
        switch (comparacion) {
            case "JE": return "JNE";
            case "JNE": return "JE";
            case "JG": return "JLE";
            case "JLE": return "JG";
            case "JL": return "JGE";
            case "JGE": return "JL";
            case "JNA": return "JA";
            case "JA": return "JNA";
            case "JNAE": return "JAE";
            case "JAE": return "JNAE";
            default: return comparacion;
        }
    }

    private static String ocuparAuxiliar(String tipo) {
        String retorno = "@aux" + auxiliarDisponible;
        ++auxiliarDisponible;
        //agrego a la tabla de simbolos la auxiliar.
        TablaSimbolos.agregarSimbolo(retorno);
        TablaSimbolos.agregarAtributo(TablaSimbolos.obtenerClave(retorno), "tipo", tipo);
        return retorno;
    }

    private static void generarCodigoRetorno() {
        //generarOperador(":=");
        TablaSimbolos.agregarAtributo(TablaSimbolos.obtenerClave(ultimaFuncionLlamada), "retorno", pila_tokens.pop());
        codigo.append("RET\n");
    } 

    private static void generarCodigoDefer(int indice){ //Se crea una lista nueva para generar assembler con las funciones
        Polaca.polaca.remove(indice); //Se remueve la marca #DEFER
        while(!(Polaca.polaca.get(indice).equals("#FINDEFER"))){
            codigoDefer.add(Polaca.polaca.get(indice));
            Polaca.polaca.remove(indice);
        }
        codigoDefer.add("#FINDEFER");
        Polaca.polaca.remove(indice);
    }

    private static void generarCodigoEjecucionDefer(int indice){
        while(!(codigoDefer.get(0).equals("#FINDEFER"))){
            Polaca.polaca.add(indice++, codigoDefer.get(0));
            codigoDefer.remove(0);
        }
        codigoDefer.remove(0); //Se remueve #FINDEFER
        Polaca.polaca.remove(indice); //Se remueve #EJECDEFER
    }

    private static void RestructurarPolacaFunciones(){
        int indice = 0;
        Stack<Integer> pila_inicio_funciones = new Stack<>();
        while(indice < Polaca.polaca.size()){
            if (Polaca.polaca.get(indice).equals("#FUN")){
                int pos = indice;
                pos--;
                pila_inicio_funciones.push(pos);
            }

            if (Polaca.polaca.get(indice).equals("#RET")){
                int pos_inicio = pila_inicio_funciones.pop();
                int pos_fin = indice;
                pos_fin++;
                funciones.addAll(Polaca.polaca.subList(pos_inicio, pos_fin));
                int pos_indice = indice - Polaca.polaca.subList(pos_inicio, pos_fin).size();
                Polaca.polaca.subList(pos_inicio, pos_fin).clear();
                indice = pos_indice;
            }
            indice++;
        }
        Polaca.polaca.addAll(0, funciones);
        pos_start = funciones.size();
        funciones.clear();
    }

    public static String eliminarAmbito(String lexema){
        int clave = TablaSimbolos.obtenerClave(lexema);
        if (clave != TablaSimbolos.NO_ENCONTRADO){
            String uso = TablaSimbolos.obtenerAtributo(clave, "uso");
            if (!uso.equals("constante")){
                return lexema.substring(0, lexema.indexOf("."));
            }
        }
        return lexema;
    }

}
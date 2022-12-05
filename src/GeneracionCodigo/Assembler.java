package GeneracionCodigo;

import java.util.*;
import AnalizadorLexico.*;

public class Assembler {
    public static StringBuilder codigo = new StringBuilder();

    private static final Stack<String> pila_tokens = new Stack<>();
    private static String ultimaComparacion = "";

    private static int auxiliarDisponible = 0;

    public static int posicionActualPolaca = 0;

    public static String ultimaFuncionLlamada;

    public static ArrayList<Object> codigoDefer = new ArrayList<Object>();

    private static final String AUX_CONTRATO = "@contrato";
    private static String nombreAux2bytes = "@aux2bytes"; 

    private static final String ERROR_DIVISION_POR_CERO = "ERROR: Division por cero";   //strings de error constantes en el codigo
    private static final String ERROR_OVERFLOW_PRODUCTO = "ERROR: Overflow en operacion de producto";   
    private static final String ERROR_INVOCACION = "ERROR: Invocacion de funcion a si misma no permitida";
    
    public static void generarCodigo() {
        //funcion principal que genera el codigo del programa, utilizando los tokes de la pocala y simbolos de la respectiva tabla
        for (int indice = 0; indice < Polaca.polaca.size(); indice++){
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
                    generarSalto("JMP");
                    break;
                case "#BF":
                    generarSalto(ultimaComparacion);
                    break;
                case "#BT":
                    generarSalto(negacion(ultimaComparacion));
                    break;
                case "#CALL":
                    generarLlamadoFuncion();
                    break;
                case "#RET":
                    generarCodigoRetorno();
                    break;
                case "#OUT":
                    String cadena = pila_tokens.pop(); 
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
                    break;
                default:
                    if (token.startsWith(":")) {   //entramos un label
                        codigo.append(token.substring(1)).append(":\n");
                    } 
                    else {
                        pila_tokens.push(token);
                    }
                    break;
            }

            ++posicionActualPolaca;
            //Impresion por pantalla para debuggear el codigo
            //System.out.println("Se leyo el token: " + token + ", la pila actual es: " + pila_tokens);
        }

        codigo.append("invoke ExitProcess, 0\n")
              .append("end START");

        generarCabecera();
    }

    private static void generarCabecera() {
    //funcoin encargada de la generacion de la cabecera del codigo
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
            .append("@ERROR_OVERFLOW_PRODUCTO db \"" + ERROR_OVERFLOW_PRODUCTO + "\", 0\n")
            .append("@ERROR_INVOCACION db \"" + ERROR_INVOCACION + "\", 0\n");

        generarCodigoDatos(cabecera);

        cabecera.append(".code\n");
        cabecera.append(codigo);
        codigo = cabecera;
    }

    private static void generarCodigoDatos(StringBuilder cabecera) {
        //funcion utilizada para generar el codigo necesario para todos los datos del programa, presentes en la tabla de simbolos
        for (int simbolo : TablaSimbolos.obtenerConjuntoPunteros()) {
            //tomamos el atributo 'uso' del simbolo actual, desde la tabla de simbolos
            String uso = TablaSimbolos.obtenerAtributo(simbolo, "uso");

            if (uso!=null && uso.equals("funcion")) continue;

            String tipo_actual = TablaSimbolos.obtenerAtributo(simbolo, "tipo");
            String lexema_actual = TablaSimbolos.obtenerAtributo(simbolo, "lexema");
            
            if (tipo_actual!=null) continue;

            switch (tipo_actual) {
                /*case TablaTipos.STR_TYPE:
                    //tomo el valor de la tabla de simbolos
                    String valor_actual = TablaSimbolos.obtenerAtributo(simbolo, "valor");
                    cabecera.append(lexema_actual.substring(1)).append(" db \"").append(valor_actual).append("\", 0\n");
                    break;
                */
                case TablaTipos.UI16_TYPE:
                //case TablaTipos.FUNC_TYPE:
                    if (uso.equals("constante")) {
                        String lexema = lexema_actual;
                        lexema_actual = "@" + lexema_actual;
                        cabecera.append(lexema_actual).append(" dd ").append(lexema).append("\n");
                    } else {
                        if (!lexema_actual.startsWith("@")) {
                            cabecera.append("_");
                        }
                        
                        cabecera.append(lexema_actual).append(" dd ? \n");
                    }
                   
                    break;
                
                case TablaTipos.F64_TYPE:        //en caso que el simbolo de tipo double y sea una constante
                    if (uso.equals("constante")) {
                        String lexema = lexema_actual;

                        if (lexema_actual.charAt(0) == '.')
                            lexema = "0" + lexema;

                        lexema_actual = "@" + lexema_actual.replace('.', '@').replace('-', '@').replace('+', '@');  //cambiamos el punto por una @ 
                        cabecera.append(lexema_actual).append(" REAL4 ").append(lexema).append("\n");   //y agregamos el simbolo a la cabecera con REAL4
                    } else {
                        if (! lexema_actual.startsWith("@")) {
                            cabecera.append("_");
                        }
                        cabecera.append(lexema_actual).append(" dq ?\n");
                    }
                    
                    break;
            }
        }
    }

    private static void generarCabeceraFuncion() {
        codigo.append(pila_tokens.pop()).append(":").append("\n");
    }

    public static void generarOperador(String operador) {
        String op2 = pila_tokens.pop();   //el primero que saco es el segundo operando, ya que fue el ultimo que lei de la polaca y el ultimo que agregue a la pila
        String op1 = pila_tokens.pop();

        if (operador.equals("=:")) { //Si el operador es =: entonces los operandos estan al reves por como esta hecha la gramatica
            String aux = op1;
            op1 = op2;
            op2 = aux;
        }

        String tipo = TablaTipos.getTipoAbarcativo(op1, op2, operador);
        switch (tipo) {
            case TablaTipos.UI16_TYPE:
                generarOperacionEnteros(op1, op2, operador);
                break;
            case TablaTipos.F64_TYPE:
                generarOperacionFlotantes(op1, op2, operador);
                break;
            case TablaTipos.FUNC_TYPE:
                generarOperacionFuncion(op1, op2);
                break;
            default:
                System.out.println("Algo esta mal");
                TablaSimbolos.imprimirTabla();
        }
    }
 
    public static void generarOperacionFuncion(String op1, String op2) {
        int punt_op2 = TablaSimbolos.obtenerClave(op2);
        String uso = TablaSimbolos.obtenerAtributo(punt_op2, "uso");
        
        op1 = renombre(op1);

        //si el uso es una variable, renombramos el operando
        if (uso.equals("variable"))
            op2 = renombre(op2);

        codigo.append("MOV EAX, ").append(op2).append("\n");
        codigo.append("MOV ").append(op1).append(", EAX\n");
    }

    private static void generarErrorDivCero(String aux){
        // genera el codigo necesario ante un error de division por cero
        codigo.append("JNE ").append(aux.substring(1)).append("\n");
        codigo.append("invoke MessageBox, NULL, addr @ERROR_DIVISION_POR_CERO, addr @ERROR_DIVISION_POR_CERO, MB_OK\n");
        codigo.append("invoke ExitProcess, 0\n");
        codigo.append(aux.substring(1)).append(":\n"); //declaro una label        
    }

    private static void generarErrorOverflow(String aux){
        //genera el codigo necesario ante un error de overflow de una operacion de producto de enteros
        //utilizamos el flag de overflow para indicar que se ha producido un overflow
        
        codigo.append("JNO ").append(aux.substring(1)).append("\n");    
        codigo.append("invoke MessageBox, NULL, addr @ERROR_OVERFLOW_PRODUCTO, addr @ERROR_OVERFLOW_PRODUCTO, MB_OK\n");
        codigo.append("invoke ExitProcess, 0\n");
        codigo.append(aux.substring(1)).append(":\n"); //declaro una label        
    }

    private static void generarErrorInvocacion(String funcion, String funcion_actual) {
        //genera el codigo necesario ante un error de invocacion de una funcion:
        //El codigo Assembler debera controlar que una funcion no pueda invocarse a si misma. 
     
        int punt_funcion = TablaSimbolos.obtenerClaveID(funcion);
        String uso = TablaSimbolos.obtenerAtributo(punt_funcion, "uso"); 

        if (uso.equals("variable")) {
            funcion = renombre(funcion);  //renombramos la variable de funcion
            codigo.append("MOV EAX, [").append(funcion).append("]\n");
        } else 
            codigo.append("MOV EAX, ").append(funcion).append("\n");

        String label = "@aux" + auxiliarDisponible;
        ++auxiliarDisponible;

        codigo.append("MOV EBX, ").append(funcion_actual).append("\n");
        codigo.append("CMP EBX, EAX\n");
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
                codigo.append("MOV ECX, ").append(op1).append("\n"); //muevo siempre al registro ECX ya que al usar auxiliares nunca voy a gastar mas de 1 registro, ademas este registro no es usado por las divisiones
                codigo.append("ADD ECX, ").append(op2).append("\n");
                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                codigo.append("MOV ").append(aux).append(", ECX\n");
                pila_tokens.push(aux);
                break;
            case "-":
                codigo.append("MOV ECX, ").append(op1).append("\n"); //muevo siempre al registro ECX ya que al usar auxiliares nunca voy a gastar mas de 1 registro, ademas este registro no es usado por las divisiones
                codigo.append("SUB ECX, ").append(op2).append("\n");
                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                codigo.append("MOV ").append(aux).append(", ECX\n");
                pila_tokens.push(aux);
                break;
            case "*":
                codigo.append("MOV EAX, ").append(op1).append("\n"); //muevo al registro EAX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("MUL ").append(op2).append("\n");
                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                generarErrorOverflow(aux); //el aux solo se pasa para poner el nombre de la etiqueta de salto.
                codigo.append("MOV ").append(aux).append(", EAX\n");
                pila_tokens.push(aux);
                break;
            case "=:":
                codigo.append("MOV ECX, ").append(op2).append("\n"); //muevo al registro EAX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("MOV ").append(op1).append(", ECX\n");
                break;
            case "/":   
                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                codigo.append("CMP ").append(op2).append(", 00h\n"); 
                generarErrorDivCero(aux);
                codigo.append("MOV EAX, ").append(op1).append("\n"); //el dividendo debe estar en EAX
                codigo.append("DIV ").append(op2).append("\n");
                codigo.append("MOV ").append(aux).append(", EAX\n");
                pila_tokens.push(aux);
                break;
            
            case "=":
                codigo.append("MOV ECX, ").append(op2).append("\n"); 
                codigo.append("CMP ").append(op1).append(", ECX\n");
                ultimaComparacion = "JNE";
                break;
            case "=!":
                codigo.append("MOV ECX, ").append(op2).append("\n"); 
                codigo.append("CMP ").append(op1).append(", ECX\n");
                ultimaComparacion = "JE";
                break;
            case ">=":
                codigo.append("MOV ECX, ").append(op2).append("\n"); //muevo al registro EAX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("CMP ").append(op1).append(", ECX\n");
                ultimaComparacion = "JB";
                break;
            
            case ">":
                codigo.append("MOV ECX, ").append(op2).append("\n"); //muevo al registro EAX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("CMP ").append(op1).append(", ECX\n");
                ultimaComparacion = "JNA";
                break;
            
            case "<=":
                codigo.append("MOV ECX, ").append(op2).append("\n"); //muevo al registro EAX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("CMP ").append(op1).append(", ECX\n");
                ultimaComparacion = "JA";
                break;
            
            case "<":
                codigo.append("MOV ECX, ").append(op2).append("\n"); //muevo al registro EAX ya que esto es lo que dice la filmina, que siempre en las MULT tengo que usar este registro
                codigo.append("CMP ").append(op1).append(", ECX\n");
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

        //Si es UINT, la tengo que convertir a DOUBLE
        if (TablaTipos.getTipo(op1).equals(TablaTipos.UI16_TYPE)) {
            aux = ocuparAuxiliar(TablaTipos.F64_TYPE);
            codigo.append("FLD ").append(op1).append("\n");
            codigo.append("FSTP ").append(aux).append("\n");
            op1 = aux;
        }
        if (TablaTipos.getTipo(op2).equals(TablaTipos.UI16_TYPE)) {
            aux = ocuparAuxiliar(TablaTipos.F64_TYPE);
            codigo.append("FLD ").append(op2).append("\n");
            codigo.append("FSTP ").append(aux).append("\n");
            op1 = aux;
        }
        

        switch (operador) {
            //nunca  va a llegar una operacion AND o OR entre doubles ya que al finalizar cada condicion guardo un UINT con el resultado de la condicion.
            case "+":
                codigo.append("FLD ").append(op2).append("\n"); //apilo primero el op2 ya que quiero que me quede como el segundo que agarro para las operaciones que no son conmutativas
                codigo.append("FLD ").append(op1).append("\n");

                codigo.append("FADD\n");
                aux = ocuparAuxiliar(TablaTipos.F64_TYPE);
                codigo.append("FSTP ").append(aux).append("\n");
                pila_tokens.push(aux);
                break;

            case "-":
                codigo.append("FLD ").append(op2).append("\n"); //apilo primero el op2 ya que quiero que me quede como el segundo que agarro para las operaciones que no son conmutativas
                codigo.append("FLD ").append(op1).append("\n");

                codigo.append("FSUB\n");
                aux = ocuparAuxiliar(TablaTipos.F64_TYPE);
                codigo.append("FSTP ").append(aux).append("\n");
                pila_tokens.push(aux);
                break;
            
            case "*":
                codigo.append("FLD ").append(op2).append("\n"); //apilo primero el op2 ya que quiero que me quede como el segundo que agarro para las operaciones que no son conmutativas
                codigo.append("FLD ").append(op1).append("\n");
                
                codigo.append("FMUL\n");
                aux = ocuparAuxiliar(TablaTipos.F64_TYPE);
                codigo.append("FSTP ").append(aux).append("\n");
                pila_tokens.push(aux);
                break;
            
            case "=:":
                codigo.append("FLD ").append(op2).append("\n");
                codigo.append("FSTP ").append(op1).append("\n");
                break;
            
            case "/":
                aux = ocuparAuxiliar(TablaTipos.F64_TYPE);
                codigo.append("FLD ").append(op2).append("\n"); //cargo el operando dos para luego compararlo con cero
                
                //guardar 00h en una variable auxiliar
                String _cero = ocuparAuxiliar(TablaTipos.UI16_TYPE);
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

                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JE " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); //creo una label para que salte y se saltee la instruccion de poner aux en cero en caso de que sea verdadera
                pila_tokens.push(aux);
                break;
            case "=!":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JNE " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); //creo una label para que salte y se saltee la instruccion de poner aux en cero en caso de que sea verdadera
                pila_tokens.push(aux);
                break;
            
            case ">=":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JAE " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); 
                pila_tokens.push(aux);
                break;
            
            case ">":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n"); 
                codigo.append("JA " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); //creo una label para que salte y se saltee la instruccion de poner aux en cero en caso de que sea verdadera
                pila_tokens.push(aux);
                break;
            
            case "<=":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JBE " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); 
                pila_tokens.push(aux);
                break;
            
            case "<":
                codigo.append("FLD ").append(op1).append("\n"); 
                codigo.append("FCOM ").append(op2).append("\n");
                codigo.append("FSTSW ").append(nombreAux2bytes).append("\n");// cargo la palabra de estado en la memoria
                codigo.append("MOV AX, ").append(nombreAux2bytes).append("\n"); //copia el contenido en el registro AX
                codigo.append("SAHF").append("\n"); //Almacena en los 8 bits menos significativos del regisro de indicadores el valor del registro AH

                aux = ocuparAuxiliar(TablaTipos.UI16_TYPE);
                codigo.append("MOV " + aux + ", 0FFh\n");
                codigo.append("JB " + aux.substring(1) + "\n"); // si llega a ser verdadero salto y sigo con la ejecucion. En caso contrario tengo que poner el valor de aux en 0
                codigo.append("MOV " + aux + ", 00h\n"); 
                codigo.append(aux.substring(1) + ":\n"); 
                pila_tokens.push(aux);
                break;
            
            default:
                codigo.append("ERROR se entro a default al generar codigo para una operacion de flotantes\n");
                break;
        }
    }

    private static void generarSalto(String salto) {
        String direccion = pila_tokens.pop();    
        System.out.println("direccion: " + direccion);
        if (!salto.equals("JMP") && ultimaComparacion.equals("")) {
            String valor = pila_tokens.pop();
            System.out.println("valor: " + valor);
            int punt_valor = TablaSimbolos.obtenerClave(valor);
            String uso = TablaSimbolos.obtenerAtributo(punt_valor, "uso");
            
            if (uso.equals("variable"))
                valor = renombre(valor);

            codigo.append("MOV ECX, ").append(valor).append("\n");
            codigo.append("OR ECX, 0\n");
            codigo.append("JE L").append(direccion).append("\n");
        } else {
            codigo.append(salto).append(" L").append(direccion).append("\n");
        }

        ultimaComparacion = "";
    }

    private static void generarLlamadoFuncion() {
        String funcion = pila_tokens.pop();
        int clave_funcion = TablaSimbolos.obtenerClaveID(funcion);
        int cant_parametros_funcion = Integer.parseInt(TablaSimbolos.obtenerAtributo(clave_funcion, "cantidad de parametros"));
        switch (cant_parametros_funcion){
            case 0:
                //La funcion no tiene parametros
                break;
            case 1: 
                //String parametro_real = pila_tokens.pop();
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
                    generarOperador(":=");
                    pila_tokens.push(parametro_real_2);
                    pila_tokens.push(parametro_formal_2);
                    generarOperador(":=");
                    break;
        }
        codigo.append("CALL ").append(funcion).append("\n");
        pila_tokens.push(funcion); //pusheo el retorno de la funcion
    }

    private static String renombre(String token) {
        char caracter = token.charAt(0);
        int puntToken = TablaSimbolos.obtenerClave(token);

        // Si es una constante, le cambio de nombre al cual fue declarada
        if (TablaSimbolos.obtenerAtributo(puntToken, "uso").equals("constante")) {
            return "@" + token.replace('.', '@').replace('-', '@').replace('+', '@');
        } else if (Character.isLowerCase(caracter) || Character.isUpperCase(caracter)) {
            return "_" + token;
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
        System.out.println("Polaca1: " + Polaca.polaca);
        System.out.println("codigoDefer1: " + codigoDefer);
    }

    private static void generarCodigoEjecucionDefer(int indice){
        while(!(codigoDefer.get(0).equals("#FINDEFER"))){
            Polaca.polaca.add(indice++, codigoDefer.get(0));
            codigoDefer.remove(0);
        }
        codigoDefer.remove(0); //Se remueve #FINDEFER
        Polaca.polaca.remove(indice); //Se remueve #EJECDEFER
        System.out.println("Polaca2: " + Polaca.polaca);
        System.out.println("codigoDefer2: " + codigoDefer);
    }
}
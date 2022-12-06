package GeneracionCodigo;

import AnalizadorLexico.*;

public class TablaTipos {
    public static final int UI16 = 0;
    public static final int F64 = 1;
    public static final int FUNC = 2;

    public static final String F64_TYPE = "f64";
    public static final String UI16_TYPE = "ui16";
    public static final String FUNC_TYPE = "funcion";
    public static final String CADENA_TYPE = "cadena";
    public static final String ERROR_TYPE = "error";

    private static final String[][] tiposSumaResta = { { UI16_TYPE, ERROR_TYPE, ERROR_TYPE },
                                                       { ERROR_TYPE, F64_TYPE, ERROR_TYPE },
                                                       { ERROR_TYPE, ERROR_TYPE, ERROR_TYPE} };
    private static final String[][] tiposMultDiv = { { UI16_TYPE, ERROR_TYPE, ERROR_TYPE },
                                                     { ERROR_TYPE, F64_TYPE, ERROR_TYPE },
                                                     { ERROR_TYPE, ERROR_TYPE, ERROR_TYPE} };
    private static final String[][] tiposComparadores = { { UI16_TYPE, ERROR_TYPE, ERROR_TYPE }, 
                                                          { ERROR_TYPE, F64_TYPE, ERROR_TYPE },
                                                          { ERROR_TYPE, ERROR_TYPE, ERROR_TYPE } };
    private static final String[][] tiposAsig = { { UI16_TYPE, ERROR_TYPE, ERROR_TYPE }, 
                                                  { ERROR_TYPE, F64_TYPE, ERROR_TYPE },
                                                  { ERROR_TYPE, ERROR_TYPE, FUNC_TYPE } };

    public static String getTipoAbarcativo(String op1, String op2, String operador){
        // mirar en la tabla del operando que tipo queda entre esos 2 tipos
        String tipoOp1 = getTipo(op1);
        String tipoOp2 = getTipo(op2);

        String tipoFinal = tipoResultante(tipoOp1, tipoOp2, operador);

        if (tipoFinal.equals(ERROR_TYPE)) {
            Main.erroresSintacticos.add( Assembler.posicionActualPolaca, "No se puede realizar la operacion " + operador + " entre los tipos " + tipoOp1 + " y " + tipoOp2);
        }

        return tipoFinal;
    }

    public static String getTipo(String op) {
        int puntOp;
        char caracter = op.charAt(0); //Obtengo el primer caracter del operando para saber si es una variable o una constante
        if (Character.isDigit(caracter) || (caracter == '-')) { //Es una constante
            puntOp = TablaSimbolos.obtenerClave(op);
        }
        else{
            puntOp = TablaSimbolos.obtenerClave(op);
        }
        String tipo = TablaSimbolos.obtenerAtributo(puntOp, "tipo");

        if (tipo == FUNC_TYPE) {
            // x = una_funcino() + 8 --> devo devolver el tipo de reforno de la funcion
            String uso = TablaSimbolos.obtenerAtributo(puntOp, "uso");
            if(uso == FUNC_TYPE)
                return uso;             //REVISAR EL CASO QUE SE MUESTRA DEBAJO
        }
        // a = 2
        // b = func_suma <--- func_suma es una funcoin 
        // c = a + b    <-- incorrecto
        // c = a + b()  <-- correcto

        return tipo; 
    }

    private static String tipoResultante(String op1, String op2, String operador) {
        int fil = getNumeroTipo(op1);
        int col = getNumeroTipo(op2);

        switch (operador) {
            case ("+"):
            case ("-"):
                return tiposSumaResta[fil][col];
            case ("*"):
            case ("/"):
                return tiposMultDiv[fil][col];
            case ("=:"):
                return tiposAsig[fil][col];
            case ("<="):
            case ("<"):
            case (">="):
            case (">"):
            case ("=!"):
            case ("="):
                return tiposComparadores[fil][col];
            default:
                return ERROR_TYPE;
        }
    }

    private static int getNumeroTipo(String tipo) {
        if (tipo.equals(UI16_TYPE)) return UI16;
        else if (tipo.equals(F64_TYPE)) return F64;
        else return FUNC;
    }
}
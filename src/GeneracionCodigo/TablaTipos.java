package GeneracionCodigo;

import AnalizadorLexico.*;

public class TablaTipos {
    public static final int UINT = 0;
    public static final int DOUBLE = 1;
    public static final int FUNC = 2;

    public static final String DOUBLE_TYPE = "double";
    public static final String UINT_TYPE = "uint";
    public static final String FUNC_TYPE = "funcion";
    //public static final String STR_TYPE = "string";
    public static final String ERROR_TYPE = "error";

    private static final String[][] tiposSumaResta = { { UINT_TYPE, DOUBLE_TYPE, ERROR_TYPE },
                                                       { DOUBLE_TYPE, DOUBLE_TYPE, ERROR_TYPE },
                                                       { ERROR_TYPE, ERROR_TYPE, ERROR_TYPE} };
    private static final String[][] tiposMultDiv = { { UINT_TYPE, DOUBLE_TYPE, ERROR_TYPE },
                                                     { DOUBLE_TYPE, DOUBLE_TYPE, ERROR_TYPE },
                                                     { ERROR_TYPE, ERROR_TYPE, ERROR_TYPE} };
    private static final String[][] tiposComparadores = { { UINT_TYPE, DOUBLE_TYPE, ERROR_TYPE }, 
                                                          { DOUBLE_TYPE, DOUBLE_TYPE, ERROR_TYPE },
                                                          { ERROR_TYPE, ERROR_TYPE, ERROR_TYPE } };
    private static final String[][] tiposAsig = { { UINT_TYPE, ERROR_TYPE, ERROR_TYPE }, 
                                                  { DOUBLE_TYPE, DOUBLE_TYPE, ERROR_TYPE },
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
        int puntOp = TablaSimbolos.obtenerClave(op);

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
            case (":="):
                return tiposAsig[fil][col];
            case ("<="):
            case ("<"):
            case (">="):
            case (">"):
            case ("<>"):
            case ("=="):
            case ("||"):
            case ("&&"):
                return tiposComparadores[fil][col];
            default:
                return ERROR_TYPE;
        }
    }

    private static int getNumeroTipo(String tipo) {
        if (tipo.equals(UINT_TYPE)) return UINT;
        else if (tipo.equals(DOUBLE_TYPE)) return DOUBLE;
        else return FUNC;
    }
}
package GeneracionCodigo;

import AnalizadorLexico.*;

public class Tipos {
    public static final String F64_TYPE = "f64";
    public static final String UI16_TYPE = "ui16";
    public static final String CADENA_TYPE = "cadena";
    public static final String ERROR_TYPE = "error";

    public static String getTipoOperacion(String op1, String op2, String operador){
        // mirar en la tabla del operando que tipo queda entre esos 2 tipos
        //var2.prueba_nombre_programa.funcion1
        String tipoOp1 = getTipo(op1);
        String tipoOp2 = getTipo(op2);

        if (tipoOp1.equals(tipoOp2))
            return tipoOp1;
        else {
            Main.erroresSintacticos.add( Assembler.posicionActualPolaca, "No se puede realizar la operacion " + operador + " entre los tipos " + tipoOp1 + " y " + tipoOp2);
            return ERROR_TYPE;
        }
    }

    public static String getTipo(String op) {
        int puntOp = TablaSimbolos.obtenerClave(op);
        int posicion = op.lastIndexOf('.');
        while ((puntOp == TablaSimbolos.NO_ENCONTRADO) && (posicion != -1)){
            op = op.substring(0, posicion);
            puntOp = TablaSimbolos.obtenerClave(op);
            posicion = op.lastIndexOf('.');
        }
        String tipo = TablaSimbolos.obtenerAtributo(puntOp, "tipo");
        return tipo; 
    }
}
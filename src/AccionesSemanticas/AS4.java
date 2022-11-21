package AccionesSemanticas;
import AnalizadorLexico.*;
import Parser.Parser;
import java.math.BigDecimal;

public class AS4 extends AccionSemantica{
	
	@Override
    public Token ejecutar() {
        String simbolo = buffer;
        Parser.agregoCteDbl = false;
        if ((AnalizadorLexico.caracter != AnalizadorLexico.NUEVALINEA) || (AnalizadorLexico.caracter != AnalizadorLexico.BLANCO) || ((AnalizadorLexico.caracter != AnalizadorLexico.TAB)))
        	AnalizadorLexico.cursor--;
        String exp = simbolo.replace("D", "E"); //Para leer exponencial
        double valor = Double.parseDouble(exp);
        if (!dentroRango(valor)) {
        	Main.erroresLexico.add("Error Lexico linea: " + AnalizadorLexico.linea + ". Error en el rango del numero double, el numero fue truncado a un rango aceptado.");
            if (valor < AnalizadorLexico.MINDOUBLEPOS) {
                simbolo = Double.toString(AnalizadorLexico.MINDOUBLEPOS);
            } else {
            		simbolo = Double.toString(AnalizadorLexico.MAXDOUBLEPOS);
            }
            simbolo = simbolo.replace("E", "D"); //Se vuelve a poner el simbolo original
        }
        System.out.println("simbolo2: " + simbolo);
        if (TablaSimbolos.obtenerClave(simbolo) == TablaSimbolos.NO_ENCONTRADO) {
            TablaSimbolos.agregarSimbolo(simbolo);
            int clave = TablaSimbolos.obtenerClave(simbolo);
            TablaSimbolos.agregarAtributo(clave, "tipo", AnalizadorLexico.CTE_DBL_TYPE);
            Parser.agregoCteDbl = true;
        }
        Main.tokensLexico.add(AnalizadorLexico.CTE_DBL_TYPE);
        return new Token(AnalizadorLexico.CTE_DBL, simbolo);
    }
	
    private static boolean dentroRango(double valor) {
        return valor == 0D || (AnalizadorLexico.MINDOUBLEPOS <= valor && valor <= AnalizadorLexico.MAXDOUBLEPOS);
    }
}

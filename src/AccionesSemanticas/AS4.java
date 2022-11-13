package AccionesSemanticas;
import AnalizadorLexico.*;

public class AS4 extends AccionSemantica{
	
	@Override
    public Token ejecutar() {
        String simbolo = buffer;
        if ((AnalizadorLexico.caracter != AnalizadorLexico.NUEVALINEA) || (AnalizadorLexico.caracter != AnalizadorLexico.BLANCO) || ((AnalizadorLexico.caracter != AnalizadorLexico.TAB)))
        	AnalizadorLexico.cursor--;
        String exp = simbolo.replace("D", "e"); //PARA LEER EXPONENCIAL
        double valor = Double.parseDouble(exp);
        if (!dentroRango(valor)) {
        	Main.erroresLexico.add("Error Lexico linea: " + AnalizadorLexico.linea + ". Error en el rango del número double.");
            if (valor < AnalizadorLexico.MINDOUBLEPOS) {
                simbolo = Double.toString(AnalizadorLexico.MINDOUBLEPOS);
            } else {
            		simbolo = Double.toString(AnalizadorLexico.MAXDOUBLEPOS);
            }
        }
        if (TablaSimbolos.obtenerClave(simbolo) == TablaSimbolos.NO_ENCONTRADO) {
            TablaSimbolos.agregarSimbolo(simbolo);
            int clave = TablaSimbolos.obtenerClave(simbolo);
            TablaSimbolos.agregarAtributo(clave, "tipo", String.valueOf(AnalizadorLexico.CTE_DBL));
        }
        Main.tokensLexico.add(AnalizadorLexico.CTE_DBL);
        return new Token(AnalizadorLexico.CTE_DBL, simbolo);
    }
	
    private static boolean dentroRango(double valor) {
        return valor == 0D || (AnalizadorLexico.MINDOUBLEPOS <= valor && valor <= AnalizadorLexico.MAXDOUBLEPOS);
    }
}

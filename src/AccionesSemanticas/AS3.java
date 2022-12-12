package AccionesSemanticas;
import AnalizadorLexico.*;

public class AS3 extends AccionSemantica{

	@Override
    public Token ejecutar() {
        String simbolo = buffer;
        long valor = Long.parseLong(simbolo);
        if ((AnalizadorLexico.caracter != AnalizadorLexico.NUEVALINEA) || (AnalizadorLexico.caracter != AnalizadorLexico.BLANCO) || ((AnalizadorLexico.caracter != AnalizadorLexico.TAB)))
        	AnalizadorLexico.cursor--;
        if (valor > AnalizadorLexico.MAXINT) {
        	Main.warnings.add("Error Lexico linea: " + AnalizadorLexico.linea + ". Error en el rango del numero entero, el numero fue truncado a un rango aceptado.");
            simbolo = String.valueOf(AnalizadorLexico.MAXINT);//Se trunca hasta el valor mayor
            }
        if (TablaSimbolos.obtenerClave(simbolo) == TablaSimbolos.NO_ENCONTRADO) {
            TablaSimbolos.agregarSimbolo(simbolo);
            int clave = TablaSimbolos.obtenerClave(simbolo);
            //TablaSimbolos.agregarAtributo(clave, "tipo", String.valueOf("ui16"));
            TablaSimbolos.agregarAtributo(clave, "tipo", AnalizadorLexico.CTE_INT_TYPE);
        }
        Main.tokensLexico.add(AnalizadorLexico.CTE_INT_TYPE);
        return new Token(AnalizadorLexico.CTE_INT, simbolo);
    }
}



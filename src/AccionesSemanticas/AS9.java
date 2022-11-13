package AccionesSemanticas;
import AnalizadorLexico.AnalizadorLexico;
import AnalizadorLexico.Main;
import AnalizadorLexico.TablaSimbolos;
import AnalizadorLexico.Token;

public class AS9 extends AccionSemantica{
	
    @Override
    public Token ejecutar() {
    	String simbolo = buffer;
    	Main.tokensLexico.add(AnalizadorLexico.CADENA);
        if (TablaSimbolos.obtenerClave(simbolo) == TablaSimbolos.NO_ENCONTRADO)
            TablaSimbolos.agregarSimbolo(simbolo);
        return new Token(AnalizadorLexico.CADENA, simbolo);
    }
}

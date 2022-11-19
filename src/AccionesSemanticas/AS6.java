package AccionesSemanticas;
import AnalizadorLexico.*;

public class AS6 extends AccionSemantica{

    @Override
    public Token ejecutar() {
    	if ((AnalizadorLexico.caracter != AnalizadorLexico.NUEVALINEA) || (AnalizadorLexico.caracter != AnalizadorLexico.BLANCO) || ((AnalizadorLexico.caracter != AnalizadorLexico.TAB)))
    		AnalizadorLexico.cursor--;
    	char caracter = buffer.charAt(0);
		Main.tokensLexico.add(String.valueOf(caracter));
		return new Token((int) caracter);
    }
}

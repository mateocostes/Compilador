package AccionesSemanticas;
import AnalizadorLexico.*;

public class AS8 extends AccionSemantica{
	
    @Override
    public Token ejecutar() {
    	buffer = buffer + AnalizadorLexico.caracter;
		Main.tokensLexico.add(buffer);
		return new Token(TablaPalabrasReservadas.obtenerId(buffer)); 
    }
}

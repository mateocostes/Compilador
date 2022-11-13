package AccionesSemanticas;
import AnalizadorLexico.*;

public class AS5 extends AccionSemantica{

	@Override
	public Token ejecutar() {
		char caracter = AnalizadorLexico.caracter;
		Main.tokensLexico.add((int)caracter);
		return new Token((int) caracter);
	}
}

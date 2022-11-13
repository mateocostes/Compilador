package AccionesSemanticas;
import AnalizadorLexico.*;

public class AS1 extends AccionSemantica{

	@Override
	public Token ejecutar() {
		buffer = buffer + AnalizadorLexico.caracter;
		return null;
	}
}

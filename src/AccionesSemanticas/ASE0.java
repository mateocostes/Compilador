package AccionesSemanticas;
import AnalizadorLexico.*;

public class ASE0 extends AccionSemantica{
	
    @Override
    public Token ejecutar() {
    	Main.erroresLexico.add("Error Lexico linea: " + AnalizadorLexico.linea + ". No se puede comenzar con " + AnalizadorLexico.caracter + ".");
    	return null;
    }
}
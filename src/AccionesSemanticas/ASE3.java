package AccionesSemanticas;
import AnalizadorLexico.*;

public class ASE3 extends AccionSemantica{
	
    @Override
    public Token ejecutar() {
    	Main.erroresLexico.add("Error Lexico linea: " + AnalizadorLexico.linea + ". Error en la construccion de la parte exponencial del double.");
    	return null;
    }
}
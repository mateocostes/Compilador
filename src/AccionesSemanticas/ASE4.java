package AccionesSemanticas;
import AnalizadorLexico.*;

public class ASE4 extends AccionSemantica{
	
    @Override
    public Token ejecutar() {
    	Main.erroresLexico.add("Error Lexico linea: " + AnalizadorLexico.linea + ". Error en la construccion de la cadena, la misma no se cierra.");
    	return null;
    }
}
package AccionesSemanticas;
import AnalizadorLexico.*;

public class ASE2 extends AccionSemantica{
	
    @Override
    public Token ejecutar() {
    	Main.erroresLexico.add("Error Lexico linea: " + AnalizadorLexico.linea + ". Error en la construcción del double, se esperaba un digito.");
    	return null;
    }
}

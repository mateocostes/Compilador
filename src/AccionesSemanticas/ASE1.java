package AccionesSemanticas;
import AnalizadorLexico.*;

public class ASE1 extends AccionSemantica{
	
    @Override
    public Token ejecutar() {
    	if (!((AnalizadorLexico.caracter == '$') && (AnalizadorLexico.linea == ManejadorArchivo.cantidadLineas)))
    		Main.erroresLexico.add("Error Lexico linea: " + AnalizadorLexico.linea + ". S�mbolo no reconocido por la gram�tica.");
    	return null;
    }
}

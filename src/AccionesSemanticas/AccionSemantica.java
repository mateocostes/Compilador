package AccionesSemanticas;
import AnalizadorLexico.*;

public abstract class AccionSemantica {
	public static String buffer = "";
	public abstract Token ejecutar();
}

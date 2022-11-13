package AccionesSemanticas;
import AnalizadorLexico.Token;

public class AS7 extends AccionSemantica{

    @Override
    public Token ejecutar() {
        buffer = ""; // Reinicia el token
        return null;
    }
}

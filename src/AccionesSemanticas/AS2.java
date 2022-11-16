package AccionesSemanticas;
import AnalizadorLexico.*;
import Parser.Parser;

public class AS2 extends AccionSemantica{

    @Override
    public Token ejecutar() {
        int identificador;
        String simbolo = buffer;
        if ((AnalizadorLexico.caracter != AnalizadorLexico.NUEVALINEA) || (AnalizadorLexico.caracter != AnalizadorLexico.BLANCO) || ((AnalizadorLexico.caracter != AnalizadorLexico.TAB)))
        	AnalizadorLexico.cursor--; //Se vuelve el cursor para atras para no perder el caracter
        int id_palabra_reservada = TablaPalabrasReservadas.obtenerId(simbolo);
        if (id_palabra_reservada != -1) { //Es una palabra reservada
            identificador = id_palabra_reservada;
            Main.tokensLexico.add(identificador);
            return new Token(identificador);
        } 
        else {
            if (buffer.length() > AnalizadorLexico.LONGITUDIDENTIFICADOR) {
                simbolo = buffer.substring(0, AnalizadorLexico.LONGITUDIDENTIFICADOR-1);
                Main.erroresLexico.add("Error Lexico linea: " + AnalizadorLexico.linea + ". Error en la longitud del identificador.");
                
            }
            identificador = AnalizadorLexico.IDENTIFICADOR;
        }
        if ((TablaSimbolos.obtenerClaveSinAmbito(simbolo) == TablaSimbolos.NO_ENCONTRADO)) //Se agregan todos los identificadores que se encuentren distintos
            TablaSimbolos.agregarSimbolo(simbolo);
        Main.tokensLexico.add(identificador);
        return new Token(identificador,simbolo);
    }
}

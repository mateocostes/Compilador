package AnalizadorLexico;

import java.util.HashMap;
import java.util.Map;
import Parser.Parser;

public class TablaSimbolos {
	public static final int NO_ENCONTRADO = -1;
    public static final String LEXEMA = "lexema";
    public static final String NO_ENCONTRADO_S = "No encontrado";
    private static final Map<Integer, Map<String, String>> simbolos = new HashMap<>();
                           
    private static int identificador_siguiente = 1;

    public static void agregarSimbolo(String simbolo) {
        Map<String, String> atributos = new HashMap<>();
        atributos.put(LEXEMA, simbolo);
        simbolos.put(identificador_siguiente, atributos);
        ++identificador_siguiente;
    }

    public static int obtenerClaveID(String lexema) {
        for (Map.Entry<Integer, Map<String, String>> entrada: simbolos.entrySet()) {
            String lexema_actual = entrada.getValue().get(LEXEMA);
            int posicion =  lexema_actual.indexOf("."); // obtengo la clave solo con el nombre de la variable, sin el ambito
            if (posicion != -1) {
                lexema_actual = lexema_actual.substring(0, posicion);
            }
            if (lexema_actual.equals(lexema)) {
                return entrada.getKey();
            }
        }
        return NO_ENCONTRADO; //No encontrado
    }

    public static int obtenerClave(String lexema) {
        for (Map.Entry<Integer, Map<String, String>> entrada: simbolos.entrySet()) {
            String lexema_actual = entrada.getValue().get(LEXEMA);
            if (lexema_actual.equals(lexema)) {
                return entrada.getKey();
            }
        }
        return NO_ENCONTRADO; //No encontrado
    }
    
    public static void actulizarSimbolo(int clave, String lexema_nuevo) {
        if (simbolos.containsKey(clave)) {
            Map<String, String> atributos = simbolos.get(clave);
            if (atributos.containsKey(LEXEMA)) {
                atributos.remove(LEXEMA);
                atributos.put(LEXEMA, lexema_nuevo);
            }
        }
    }
    
    public static void agregarAtributo(int clave, String atributo, String valor) {
        if (simbolos.containsKey(clave)) {
            Map<String, String> atributos = simbolos.get(clave);
            atributos.put(atributo, valor);
        }  
    }

    public static String obtenerAtributo(int clave, String atributo) {
        if (simbolos.containsKey(clave)) {
            Map<String, String> atributos = simbolos.get(clave);

            if (atributos.containsKey(atributo)) {
                return atributos.get(atributo);
            }
        }
        return NO_ENCONTRADO_S;
    }

    public static boolean perteneceAmbito(String lexema, String ambito_actual){
        return false;
    }
    
    public static void imprimirTabla() {
        System.out.println("\nTablaSimbolos:");

        for (Map.Entry<Integer, Map<String, String>> entrada: simbolos.entrySet()) {
            Map<String, String> atributos = entrada.getValue();
            System.out.print(entrada.getKey() + ": ");

            for (Map.Entry<String, String> atributo: atributos.entrySet()) {
                System.out.print("(" + atributo.getKey() + ": " + atributo.getValue() + ") ");
            }

            System.out.println();
        }
    }

    public static boolean verificarAmbito(String lexema){
        int posicion =  lexema.lastIndexOf(".");
        while (posicion != -1){
            System.out.println("lexema: " + lexema);
            for (Map.Entry<Integer, Map<String, String>> entrada: simbolos.entrySet()) {
                String lexema_actual = entrada.getValue().get(LEXEMA);
                if (lexema_actual.equals(lexema)) //Pertenecen al mismo ambito
                    return true;
            } 
            lexema = lexema.substring(0, posicion);
            if (!lexema.contains(".")) // Chequeo que tenga al menos un ambito
                return false;
            posicion =  lexema.lastIndexOf(".");
        }
        return false;
    }

}
package AnalizadorLexico;
import java.util.HashMap;
import java.util.Map;

public class TablaPalabrasReservadas {
	private static final Map<String, Integer> simbolos = new HashMap<>();
	
	public TablaPalabrasReservadas() {
		setTabla();
	}
	private static void setTabla() {
		simbolos.put("if", 261);
		simbolos.put("then", 262);
		simbolos.put("else", 263);
		simbolos.put("end_if", 264);
		simbolos.put("return", 265);
		simbolos.put("out", 266);
		simbolos.put("fun", 267);
		simbolos.put("ui16", 268);
		simbolos.put("=:", 269);
		simbolos.put(">=", 270);
		simbolos.put("<=", 271);
		simbolos.put("=!", 272);
		simbolos.put("break", 273);
		simbolos.put("f64", 274);
		simbolos.put("until", 275);
		simbolos.put("discard", 276);
		simbolos.put("do", 277);
		simbolos.put("defer", 278);
		simbolos.put("tof64", 279);
	}
	
	public static int obtenerId(String key){
		return simbolos.getOrDefault(key,-1);
	}
}

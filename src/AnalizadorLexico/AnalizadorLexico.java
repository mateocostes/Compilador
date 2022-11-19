package AnalizadorLexico;

public class AnalizadorLexico {
	public static StringBuilder codigoFuente;
	public static char caracter;
	public static int cursor;
	public static int linea;
	
	//TOKENS
    public static final char TAB = '\t';
    public static final char BLANCO = ' ';
    public static final char NUEVALINEA = '\n';
    public static final char COMILLASIMPLE = '\'';
    public static final int LONGITUDIDENTIFICADOR = 25;
    public static final long MININT = 0;
    public static final long MAXINT = (long) Math.pow(2, 16) - 1;
    public static final double MINDOUBLEPOS = 2.2250738585072014e-308;
    public static final double MAXDOUBLEPOS = 1.7976931348623157e+308;
    public static final double MINDOUBLENEG = -1.7976931348623157e+308;
    public static final double MAXDOUBLENEG = -2.2250738585072014e-308;
	public static final char DIGITO = '0';
	public static final char MINUSCULA = 'a';
	public static final char MAYUSCULA = 'A';
    public static final int IDENTIFICADOR = 257;
    public static final int CTE_INT = 258;
    public static final int CTE_DBL = 259;
    public static final int CADENA = 260;
	public static final String IDENTIFICADOR_TYPE = "id";
	public static final String CTE_INT_TYPE = "ui16";
	public static final String CTE_DBL_TYPE = "f64";
	public static final String CADENA_TYPE = "cadena";
    
    public MatricesTransicion matricesTransicion = new MatricesTransicion();
    public TablaPalabrasReservadas tablaPalabrasReservadas = new TablaPalabrasReservadas();
    public TablaSimbolos tablaSimbolos = new TablaSimbolos();
    
	public AnalizadorLexico(StringBuilder codigoFuente) {
		linea = 1;
		cursor = 0;
		AnalizadorLexico.codigoFuente = codigoFuente;
	}
 
	private char tipoCaracter(char caracter) {
	    if (Character.isDigit(caracter)) {
	        return DIGITO;
	    } else if (Character.isLowerCase(caracter)) {
	        return MINUSCULA;
	    } else if (caracter != 'D' && Character.isUpperCase(caracter)) {
	        return MAYUSCULA;
	    } else {
	        return caracter;
	    }
	}
	
	public int getValorSimbolo(char caracter){
	    switch (tipoCaracter(caracter)) {
	        case BLANCO:
	        	return 0;
	        case TAB:
	        	return 1;
	        case NUEVALINEA:
	        	return 2;	            
	        case MAYUSCULA:
	        	return 3;	            
	        case MINUSCULA:
	        	return 4;
	        case DIGITO:
	        	return 5;            
	        case '_':
	        	return 6;	            
	        case '+':
	        	return 7;	            
	        case '*':
	        	return 8;	            
	        case '/':
	        	return 9;	            
	        case '(':
	        	return 10;            
	        case ')':
	        	return 11;	            
	        case '{':
	        	return 12;            
	        case '}':
	        	return 13;            
	        case ',':
	        	return 14;            
	        case ';':
	        	return 15;	            
	        case '<':
	        	return 16;	            
	        case '>':
	        	return 17;	            
	        case '=':
	        	return 18;	            
	        case '!':
	        	return 19;	            
	        case COMILLASIMPLE:
	        	return 20;	            
	        case '.':
	        	return 21;	            
	        case 'D':
	        	return 22;	            
	        case '-':
	        	return 23;	            
	        case ':':
	        	return 24;	            
	        default:
	        	return 25;
	    }
	}
	
	public Token getToken() {
		Token token = null;
		int columna = -1;
	    int estado = 0;
		while (caracter != '$') {
			caracter = codigoFuente.charAt(cursor);
			cursor++;
			columna = this.getValorSimbolo(caracter);	
			token = matricesTransicion.getAccion(estado, columna).ejecutar();
			estado = matricesTransicion.getEstado(estado, columna);
			if ((estado == -1) && (token != null)) {//-1 es Estado final
				AccionesSemanticas.AccionSemantica.buffer = ""; //Se reinicia el buffer
                return token;	
			}
            if (estado == -1) { //Estado final
            	AccionesSemanticas.AccionSemantica.buffer = "";
                estado = 0;
            }
            if (estado == -2) { //Error
            	AccionesSemanticas.AccionSemantica.buffer = "";
            	return new Token(0);
            }
			if (caracter == '\n')
				linea++;
		}
		return new Token(0);
	}
}

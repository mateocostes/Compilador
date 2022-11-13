package AnalizadorLexico;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import GeneracionCodigo.*;
import Parser.Parser;

public class Main {
	public static ManejadorArchivo manejadorArchivo = new ManejadorArchivo();
	public static List<Integer> tokensLexico = new ArrayList<>();
	public static List<String> erroresLexico = new ArrayList<>();
	public static List<String> estructurasSintacticas = new ArrayList<>();
	public static List<String> erroresSintacticos = new ArrayList<>();
	public static Polaca polaca = new Polaca();
	
	public static void main(String[] args) throws Exception{
		/*//Ventana para seleccionar archivo
		JFileChooser ventana = new JFileChooser();
		ventana.setDialogTitle("Elije el codigo fuente");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivo de Texto .txt","txt");
		ventana.setAcceptAllFileFilterUsed(false);
		ventana.setFileFilter(filter);
		int retorno = ventana.showDialog(null, "Abrir codigo fuente");
		File file = null;
		if (retorno == JFileChooser.APPROVE_OPTION)
			file = ventana.getSelectedFile();
		//Fin ventana para seleccionar archivo
		
		if (file != null) {
			String direccion = file.getPath();*/
			String direccion = "";
			//CAMBIAR DIRECCION
			//Direccion Mateo
			direccion = "C:\\Users\\Mateo\\Desktop\\Test.txt";
	    	StringBuilder Codigo = manejadorArchivo.getCodigo(direccion);
			AnalizadorLexico analizadorLexico = new AnalizadorLexico(Codigo);
	        Parser p = new Parser(analizadorLexico);
	        p.run();
			System.out.println("tokensLexico");
	        for (int i=0; i<tokensLexico.size();i++)
	        	System.out.print(tokensLexico.get(i) + " ");
	        
	        System.out.println();
			System.out.println("erroresLexico");
	        for (int i=0; i<erroresLexico.size();i++) 
	        	System.out.println(erroresLexico.get(i));
	        
	        System.out.println();
			System.out.println("estructurasSintacticas");
	        for (int i=0; i<estructurasSintacticas.size();i++) 
	        	System.out.println(estructurasSintacticas.get(i));
	        
	        System.out.println();
			System.out.println("erroresSintacticos");
	        for (int i=0; i<erroresSintacticos.size();i++) 
	        	System.out.println(erroresSintacticos.get(i));
	        
	        analizadorLexico.tablaSimbolos.imprimirTabla();
	        polaca.imprimirLista();
		//}       
	}
 }

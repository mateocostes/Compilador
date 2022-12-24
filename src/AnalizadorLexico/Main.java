package AnalizadorLexico;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import GeneracionCodigo.*;
import Parser.Parser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Main {
	public static ManejadorArchivo manejadorArchivo = new ManejadorArchivo();
	public static List<String> tokensLexico = new ArrayList<>();
	public static List<String> erroresLexico = new ArrayList<>();
	public static List<String> estructurasSintacticas = new ArrayList<>();
	public static List<String> erroresSintacticos = new ArrayList<>();
	public static List<String> warnings = new ArrayList<>();
	public static Polaca polaca = new Polaca();
	public static Assembler assembler = new Assembler();
	
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
			//Direccion Mateo PC
			direccion = "C:\\Users\\Mateo\\Desktop\\Test.txt";
			//Direccion Mateo Notebook
			//direccion = "C:\\Users\\Ceres Tolvas\\Desktop\\Test.txt";
			//Direccion Franco
			//direccion = "C:\\Users\\Franco\\Desktop\\Nuevacarpeta\\repoCompi\\test.txt";
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
			
			System.out.println();
			System.out.println("warnings");
			for (int i=0; i<warnings.size();i++) 
				System.out.println(warnings.get(i));
	        
	        polaca.imprimirLista();

			System.out.println();
			Assembler.generarCodigo();
			analizadorLexico.tablaSimbolos.imprimirTabla();
			//System.out.println("Assembler: " + Assembler.codigo);

			//Se genera el archivo .asm
			try {
				String ruta = "";
				//Ruta mateo notebook
				//ruta = "C:\\Users\\Ceres Tolvas\\Desktop\\Compilador\\prueba.asm";
				ruta = "C:\\Users\\Mateo\\Desktop\\Compilador\\Codigos assembler\\anidamiento.asm";
				//ruta = "C:\\Users\\Franco\\Desktop\\Nuevacarpeta\\repoCompi\\prueba\\error_division_cero.asm";
				StringBuilder contenido = Assembler.codigo;
				File file = new File(ruta);
				// Si el archivo no existe es creado
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(contenido.toString());
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		//}       
	}
 }


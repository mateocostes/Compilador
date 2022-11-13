package AnalizadorLexico;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ManejadorArchivo{
	public static int cantidadLineas; //Se utiliza para obtener la linea final
	
	public ManejadorArchivo() {
		cantidadLineas = 0;
	}
	
	public StringBuilder getCodigo(String ruta) {
		BufferedReader codigo;
		StringBuilder buffer = new StringBuilder();
		try {
			codigo = new BufferedReader(new FileReader(ruta));

			String readLine = codigo.readLine();

			while (readLine != null) {
				cantidadLineas++;
				buffer.append(readLine + "\n");
				readLine = codigo.readLine();
			}
			buffer.deleteCharAt(buffer.length() - 1);
			buffer.append("\n$");
			cantidadLineas++;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}
}


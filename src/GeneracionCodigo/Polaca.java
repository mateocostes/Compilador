package GeneracionCodigo;

import java.util.ArrayList;
import java.util.Stack;

public class Polaca {
	public static ArrayList<Object> polaca;
	public static Stack<Integer> pila = new Stack<Integer>();
	
	public Polaca() {
		polaca = new ArrayList<Object>();
	}
	
	public void addElementPolaca(Object element) {
		polaca.add(element);
	}
	
	public static void apilar(int indice) {
		pila.push(indice);
	}

	public static int desapilar() {
		return pila.pop();
	}
	
	public int getSize() {
		return this.polaca.size();
	}
	
	public void replaceElementIndex(int elem, int index) {
		//System.out.println("index: " + index);
		this.polaca.add(index, elem); //Lo agrega detras de la posicion por parametro
		this.polaca.remove(index + 1); //Borro el siguiente que el vacio
	}
	
	public static void imprimirLista() {
		System.out.println("\nPolaca:");
		for (int i = 0; i < polaca.size(); i++)
			System.out.println(i + " " + polaca.get(i));
	}
	
}

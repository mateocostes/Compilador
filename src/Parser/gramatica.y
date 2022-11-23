%{
package Parser;
import AnalizadorLexico.*;
import java.util.ArrayList;
%}

//declaracion de tokens a recibir del Analizador Lexico
%token ID CTE_INT CTE_DBL CADENA IF THEN ELSE END_IF RETURN OUT FUN UI16 ASSIGN MAYOR_IGUAL MENOR_IGUAL DISTINTO BREAK  F64 UNTIL DISCARD DO DEFER TOF64
%left '+' '-'
%left '*' '/'

%start programa
%%

programa            :   ID {String nombre_programa = $1.sval;
							int clave = this.analizadorLexico.tablaSimbolos.obtenerClave(nombre_programa); //se obtiene la clave
							if(clave != this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO) // si esta declarada
								this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "uso", "nombre de programa"); // se agrega el uso a la tabla de simbolos
								this.ambito = nombre_programa;}
						conjunto_sentencias
                    |   error_programa
                    ;
					
conjunto_sentencias	: 	'{' sentencias '}' 	{if (this.existeDefer){
												this.existeDefer = false;
												Main.polaca.replaceElementIndex(Main.polaca.getSize() + 2, Main.polaca.desapilar());
												Main.polaca.addElementPolaca(Main.polaca.desapilar()); // Apilo el comienzo del defer
												Main.polaca.addElementPolaca("JBD");}} // JUMP BEGIN DEFER CONSULTAR
					| 	error_conjunto_sentencias
					;
					
sentencias			: 	declarativa sentencias
					|	ejecutable sentencias
					|	declarativa
					|	ejecutable
					;
				
ejecutables         :   ejecutables ejecutable
					|	ejecutable
                    ;

declarativa        	:	funcion ';'
					|   tipo lista_de_variables ';' { Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto una declaracion de variables");
													String tipoVar = $1.sval;
													lista_de_variables = (ArrayList<String>)$2.obj;
													for(String lexema : lista_de_variables) // por cada variable declarada
														incorporarInformacionSemantica(lexema, tipoVar, "variable", ambito);
													lista_de_variables.clear();}
													
                    |   error_declarativa
                    ;

tipo                :   UI16 {$$ = new ParserVal("ui16"); Main.estructurasSintacticas.add("[Lexico: linea " + this.analizadorLexico.linea + "]. Se leyo el tipo 'UI16'");}
                    |   F64   {$$ = new ParserVal("f64"); Main.estructurasSintacticas.add("[Lexico: linea " + this.analizadorLexico.linea + "]. Se leyo el tipo 'F64'");}  
                    ;
					
lista_de_variables  :   ID {Main.estructurasSintacticas.add("[Lexico: linea " + this.analizadorLexico.linea + "]. Se leyo el identificador -> " + $1.sval);
							lista_de_variables.add($1.sval);
                            $$ = new ParserVal(lista_de_variables);} // retorna la lista de variables
      		        |   lista_de_variables ',' ID {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se leyo el identificador (dentro de una lista de variables) -> " +  $3.sval);
					                            lista_de_variables = (ArrayList<String>)$1.obj;
												lista_de_variables.add($3.sval);
												$$ = new ParserVal(lista_de_variables);} // retorna la lista de variables
                    |   error_lista_de_variables
                    ;
				
funcion         	:	FUN ID {this.nombre_funcion = $2.sval;}
						funcion_parametros
                    |   error_funcion
                    ; 

funcion_parametros	:	'(' lista_parametros ')' ':' tipo '{'  	{Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto una declaracion de una funcion");
																String nombreFunc = this.nombre_funcion;
																String tipoFunc = $2.sval;
																incorporarInformacionSemantica(nombreFunc, tipoFunc, "nombre de funcion", ambito);
																int clave = this.analizadorLexico.tablaSimbolos.obtenerClave(nombreFunc + "." + ambito); //se obtiene la clave
																if(clave != this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO) // si esta declarada
																	this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "cantidad de parametros", Integer.toString(this.cantidad_parametros)); // se agrega la cantidad de parametros a la tabla de simbolos
																this.cantidad_parametros = 0;
																this.ambito = this.ambito + "." + nombreFunc;}
						cuerpo_funcion
					|	error_funcion_parametros
					
lista_parametros	: 	parametros ',' parametro //PARAMETROS SE UTILIZA PARA ESTABLECER EL MAXIMO DE PARAMETROS PERMITIDOS EN 2;
					|	parametro
					|	//sin 
					|	error_lista_parametros
					;
					
parametros			: 	parametro
					;
					
parametro			:	tipo ID  {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se leyo el parametro -> " + $2.sval);
								String nombreFunc = this.nombre_funcion;
								String tipoParam = $1.sval;
								String nombreParam = $2.sval;
								String ambito_actual = ambito + "." + nombreFunc;
								this.cantidad_parametros++;
								incorporarInformacionSemantica(nombreParam, tipoParam, "nombre de parametro", ambito_actual);}
					|	error_parametro
					;
			
cuerpo_funcion      :   sentencias retorno '}' {this.ambito = this.ambito.substring(0,ambito.lastIndexOf("."));
												if (this.existeDefer){
													this.existeDefer = false;
													Main.polaca.replaceElementIndex(Main.polaca.getSize() + 2, Main.polaca.desapilar());
													Main.polaca.addElementPolaca(Main.polaca.desapilar()); // Apilo el comienzo del defer
													Main.polaca.addElementPolaca("JBD");}} //JUMP BEGIN DEFER CONSULTAR
                    |   retorno '}' {this.ambito = this.ambito.substring(0,ambito.lastIndexOf(".")); 
									Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Warning: funcion vacia");
									if (this.existeDefer){
										this.existeDefer = false;
										Main.polaca.replaceElementIndex(Main.polaca.getSize() + 2, Main.polaca.desapilar());
										Main.polaca.addElementPolaca(Main.polaca.desapilar()); // Apilo el comienzo del defer
										Main.polaca.addElementPolaca("JBD");}} //JUMP BEGIN DEFER CONSULTAR
                    |   error_cuerpo_funcion
                    ;    

retorno             :   RETURN ejecucion_retorno ';' {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un retorno de funcion");}
                    |   error_retorno
					;
                    
ejecucion_retorno   :   condicion
                    |   '(' expresion ')'
					|	error_retorno_expresion
                    ;
			
condicion           :  '(' expresion comparador expresion ')' 
							{Main.polaca.addElementPolaca($3.sval);} //Guarda el comparador utilizando $$ en comparador
                    |	error_condicion
					;
								
expresion		    :   termino
					|	expresion '+' termino {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. se realizo una suma");
								Main.polaca.addElementPolaca("+");}
                    |   expresion '-' termino {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. se realizo una resta");
								Main.polaca.addElementPolaca("-");}
                    |	error_expresion
					;

termino             :   termino '*' factor {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. se realizo una multiplicacion");
							Main.polaca.addElementPolaca("*");}
                    |   termino '/' factor {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. se realizo una division");
							Main.polaca.addElementPolaca("/");}
                    |   factor
                    |	error_termino
					;
       
factor       		:   CTE_INT  	{Main.estructurasSintacticas.add("[Lexico: linea " + this.analizadorLexico.linea + "]. se leyo la constante entera: " + $1.sval);
									String cte = $1.sval;
									Main.polaca.addElementPolaca(cte); 
									int clave = this.analizadorLexico.tablaSimbolos.obtenerClave(cte);
									this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "uso", "constante");}
					|	CTE_DBL	 	{Main.estructurasSintacticas.add("[Lexico: linea " + this.analizadorLexico.linea + "]. se leyo la constante doble: " + $1.sval);
									String cte = $1.sval;
									Main.polaca.addElementPolaca(cte);
									int clave = this.analizadorLexico.tablaSimbolos.obtenerClave(cte);
									this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "uso", "constante");}
					|	'-' CTE_INT {actualizarRango();} {$$ = new ParserVal("-"+$2.sval); Main.estructurasSintacticas.add("[Lexico: linea " + this.analizadorLexico.linea + "]. se leyo la constante entera: " + $$.sval);
									String cte = $$.sval;
									Main.polaca.addElementPolaca(cte);
									int clave = this.analizadorLexico.tablaSimbolos.obtenerClave(cte);
									this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "uso", "constante");}
					|	'-' CTE_DBL {actualizarRango();} {$$ = new ParserVal("-"+$2.sval); Main.estructurasSintacticas.add("[Lexico: linea " + this.analizadorLexico.linea + "]. se leyo la constante doble: " + $$.sval);
									String cte = $$.sval;
									Main.polaca.addElementPolaca(cte);
									int clave = this.analizadorLexico.tablaSimbolos.obtenerClave(cte);
									this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "uso", "constante");}
					|   ID          {Main.estructurasSintacticas.add("[Lexico: linea " + this.analizadorLexico.linea + "]. se leyo el identificador:  " + $1.sval);
									String id = $1.sval;
									Main.polaca.addElementPolaca(id);
									if (this.analizadorLexico.tablaSimbolos.obtenerClaveAmbito(id + "." + this.ambito) == this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO)
										Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, la variable " + id + ", no fue declarada en ese ambito");}
									
					| 	invocacion
					|	TOF64 '(' expresion ')'
					|	error_factor
					;
					
invocacion			: 	ID '(' lista_parametros_reales ')'	{String id = $1.sval;
															Main.polaca.addElementPolaca(id);
															Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. se realizo una invocacion a funcion");
															int clave = this.analizadorLexico.tablaSimbolos.obtenerClaveAmbito(id + "." + this.ambito); //se obtiene la clave
															if (clave == this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO){
																Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, la variable " + id + ", no fue declarada en ese ambito");
															}
															else{
																if (Integer.parseInt(this.analizadorLexico.tablaSimbolos.obtenerAtributo(clave, "cantidad de parametros")) != this.cantidad_parametros_reales)
																	Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Warning sintactico : El numero de parametros de la funcion " + id + ", no coincide con su declaracion");
															}
															this.cantidad_parametros_reales = 0;}
					|	error_invocacion
					;

lista_parametros_reales    	:  	parametros_reales ',' parametro_real
							|	parametro_real
							| 	//Sin parametros
							|   error_lista_parametros_reales
							;
					
parametros_reales			: 	parametro_real
							;
							
parametro_real				:	ID  {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se leyo el parametro -> " + $1.sval);
									String id = $1.sval;
									Main.polaca.addElementPolaca(id);
									this.cantidad_parametros_reales++;
									if (this.analizadorLexico.tablaSimbolos.obtenerClaveAmbito(id + "." + this.ambito) == this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO)
										Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, la variable " + id + ", no fue declarada en ese ambito");}
							|	CTE_INT {Main.polaca.addElementPolaca($1.sval);}
							|	CTE_DBL {Main.polaca.addElementPolaca($1.sval);}
							;

comparador          :   MENOR_IGUAL {$$ = new ParserVal("<=");}
                    |   MAYOR_IGUAL {$$ = new ParserVal(">=");}
                    |   '=' {$$ = new ParserVal("=");}
                    |   '<' {$$ = new ParserVal("<");}
                    |   '>' {$$ = new ParserVal(">");}
                    |   DISTINTO {$$ = new ParserVal("=!");}
                    ;
					
ejecutable			: 	ejecutable_comun
					|	ejecutable_defer
					;
					
ejecutable_comun	: 	invocacion_discard
					|	asignacion
					|	seleccion
					|	mensaje_pantalla
					|	expresion_dountil
					;
					
ejecutable_defer	: 	DEFER 	{this.existeDefer = true;
								Main.polaca.apilar(Main.polaca.getSize() + 2); //Se apila para marcar el inicio del defer
								Main.polaca.apilar(Main.polaca.getSize()); //Se apila para continuar la ejecutacion y luego ir al defer
								Main.polaca.addElementPolaca(""); 
								Main.polaca.addElementPolaca("JED");} //JUMP END DEFER CONSULTAR
						ejecutable_comun 	{Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto una sentencia ejecutable con defer");
											Main.polaca.replaceElementIndex(Main.polaca.getSize() + 2, Main.polaca.desapilar());
											Main.polaca.apilar(Main.polaca.getSize());
											Main.polaca.addElementPolaca(""); 
											Main.polaca.addElementPolaca("JS");} // JUMP SCOPE CONSULTAR
					;	
				
asignacion			:	ID ASSIGN expresion ';' {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto una asignacion");
												String id = $1.sval;
												Main.polaca.addElementPolaca(id);
												Main.polaca.addElementPolaca("=:");
												if (this.analizadorLexico.tablaSimbolos.obtenerClaveAmbito(id + "." + this.ambito) == this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO)
													Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, la variable " + id + ", no fue declarada en ese ambito");} 
					|	error_asignacion
					;
					
seleccion			:	IF condicion {Main.polaca.apilar(Main.polaca.getSize()); 
														Main.polaca.addElementPolaca(""); 
														Main.polaca.addElementPolaca("BF");}
						cuerpo_seleccion
					|	error_seleccion 
					;
					
cuerpo_seleccion	: 	THEN '{' bloque_de_sent_ejecutables '}' END_IF ';' {Main.polaca.replaceElementIndex(Main.polaca.getSize(), Main.polaca.desapilar());
																			Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un IF");}
					| 	THEN '{' bloque_de_sent_ejecutables '}' {Main.polaca.replaceElementIndex(Main.polaca.getSize() + 2, Main.polaca.desapilar());
																Main.polaca.apilar(Main.polaca.getSize());
																Main.polaca.addElementPolaca("");
																Main.polaca.addElementPolaca("BI");}
						cuerpo_else
					|	error_cuerpo_seleccion
					;

cuerpo_else	:	ELSE '{' bloque_de_sent_ejecutables '}' END_IF ';' {Main.polaca.replaceElementIndex(Main.polaca.getSize(), Main.polaca.desapilar());
																	Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un IF-ELSE");}
			|	error_cuerpo_else
			;
						
bloque_de_sent_ejecutables	:  	ejecutables
							;
							
			
mensaje_pantalla	:	OUT '(' CADENA ')'	';' {String cadena = $3.sval;
												Main.polaca.addElementPolaca(cadena);
												Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un mensaje por pantalla");
												int clave = this.analizadorLexico.tablaSimbolos.obtenerClave(cadena); //se obtiene la clave
												if(clave != this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO){ // si esta declarada
													this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "uso", "cadena");}} // se agrega el uso a la tabla de simbolos}
					|	error_mensaje_pantalla
					;
					
invocacion_discard	: 	DISCARD ID {String id = $2.sval;
									if (this.analizadorLexico.tablaSimbolos.obtenerClaveAmbito(id + "." + this.ambito) == this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO)
										Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, la variable " + id + ", no fue declarada en ese ambito");}
						parametros_discard
					|	error_invocacion_discard
					;
					
parametros_discard	:	'(' lista_parametros_reales ')' ';' {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto una invocacion a un DISCARD");}
					|	error_parametros_discard
					;
					
expresion_dountil	: 	DO {Main.polaca.apilar(Main.polaca.getSize());} cuerpo_dountil
					|	etiqueta ':' DO {Main.polaca.apilar(Main.polaca.getSize());
										String nombre_etiqueta = $1.sval;
										incorporarInformacionSemantica(nombre_etiqueta, "", "etiqueta", this.ambito);}
						cuerpo_dountil_etiqueta
					|	error_dountil
					;
					
etiqueta			: 	ID
					;

cuerpo_dountil		: 	'{' bloque_de_sentencias_ejecutables '}' UNTIL condicion {Main.polaca.apilar(Main.polaca.getSize());
																				Main.polaca.addElementPolaca("");
																				Main.polaca.addElementPolaca("BI");}
						cuerpo_asignacion_do_until 	{Main.polaca.replaceElementIndex(Main.polaca.getSize() + 2, Main.polaca.desapilar());
												if (Main.polaca.existeBreak()){ //Hay un Break
													Main.polaca.replaceElementIndex(Main.polaca.getSize() + 2, Main.polaca.desapilar());}
												Main.polaca.addElementPolaca(Main.polaca.desapilar());
												Main.polaca.addElementPolaca("BF");
												Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un do-until");}
					|	error_cuerpo_dountil
					;

cuerpo_dountil_etiqueta	:	'{' bloque_de_sentencias_ejecutables_etiqueta '}' UNTIL condicion {Main.polaca.apilar(Main.polaca.getSize());
																								Main.polaca.addElementPolaca("");
																								Main.polaca.addElementPolaca("BI");}
							cuerpo_asignacion_do_until {Main.polaca.replaceElementIndex(Main.polaca.getSize() + 2, Main.polaca.desapilar());
												if (Main.polaca.existeBreak()){ //Hay un Break
													Main.polaca.replaceElementIndex(Main.polaca.getSize() + 2, Main.polaca.desapilar());}
												Main.polaca.addElementPolaca(Main.polaca.desapilar());
												Main.polaca.addElementPolaca("BF");
												Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un do-until con etiqueta");}
						|	error_cuerpo_dountil_etiqueta
						;


cuerpo_asignacion_do_until 	:	':' '(' asignacion_do_until ')' ';'
							|	error_cuerpo_asignacion_do_until
							;
					
asignacion_do_until			:	ID ASSIGN expresion {Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto una asignacion en la sentencia do-until");
												String id = $1.sval;
												Main.polaca.addElementPolaca(id);
												Main.polaca.addElementPolaca("=:");
												if (this.analizadorLexico.tablaSimbolos.obtenerClaveAmbito(id + "." + this.ambito) == this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO)
													Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, la variable " + id + ", no fue declarada en ese ambito");} 
							|	error_asignacion_do_until
							;
					
bloque_de_sentencias_ejecutables 	:	ejecutables BREAK ';' {Main.polaca.contieneBreak();
															Main.polaca.apilar(Main.polaca.getSize());
															Main.polaca.addElementPolaca("");
															Main.polaca.addElementPolaca("BI");
															Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un break");}
									|	ejecutables
									|	BREAK ';' {Main.polaca.contieneBreak();
												Main.polaca.apilar(Main.polaca.getSize());
												Main.polaca.addElementPolaca("");
												Main.polaca.addElementPolaca("BI");
												Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un break");}
									|	error_bloque_sent_ejecutables
									;

									
bloque_de_sentencias_ejecutables_etiqueta	:	ejecutables BREAK ':' etiqueta ';' {Main.polaca.contieneBreak();
																					Main.polaca.apilar(Main.polaca.getSize());
																					Main.polaca.addElementPolaca("");
																					Main.polaca.addElementPolaca("BI");
																					String nombre_etiqueta = $4.sval;
																					if (!(this.analizadorLexico.tablaSimbolos.existeEtiqueta(nombre_etiqueta + "." + this.ambito)))
																						Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, la etiqueta " + nombre_etiqueta + " no se corresponde con la etiqueta del do-until");
																					Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un break con etiqueta");}
																						
											|	BREAK ':' etiqueta ';' {Main.polaca.contieneBreak();
																		Main.polaca.apilar(Main.polaca.getSize());
																		Main.polaca.addElementPolaca("");
																		Main.polaca.addElementPolaca("BI");
																		String nombre_etiqueta = $3.sval;
																		if (!(this.analizadorLexico.tablaSimbolos.existeEtiqueta(nombre_etiqueta + "." + this.ambito)))
																			Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, la etiqueta " + nombre_etiqueta + " no se corresponde con la etiqueta del do-until");
																		Main.estructurasSintacticas.add("[Parser: linea " + this.analizadorLexico.linea + "]. Se detecto un break con etiqueta");}
											|	error_bloque_de_sentencias_ejecutables_etiqueta
											;
									

//ERRORES				
error_programa      :   ID {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el bloque de programa junto con sus llaves");}
					|   conjunto_sentencias {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el nombre del programa");}
                    ;					
			  
error_conjunto_sentencias 	:	error sentencias '}' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, se detecto un bloque sin llave de apertura");}
							|	'{'	{Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el bloque de sentencia/s y la llave de cierre");}
							|	'}'	{Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el bloque de sentencia/s y la llave de apertura");}
							|	'{' '}' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta/n sentencia/s dentro de las '{' '}'");}	 
							|	error sentencias {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, faltan las llaves de apertura y cierre");}
							|	'{' sentencias {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, se detecto un bloque sin llave de cierre");}
							;
							
						  
error_declarativa	:	tipo lista_de_variables {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ; para terminar la declaracion");}
					|	lista_de_variables ';'  {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el tipo de las variables");}
					|	error tipo ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta/n la/s variable/s");}
					|	funcion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta ; al terminar la declaracion de la funcion");}
					;


error_lista_de_variables	:	error ',' ID {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta un identificador antes de la ','");}
							|	lista_de_variables ',' error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta un identificador luego de la ','");}
							;
							
				
error_funcion       :   error ID {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la palabra reservada fun al principio de la declaracion de la funcion");}
                    |   FUN error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el nombre de la funcion");}
                    ;
				
error_funcion_parametros 	: 	 lista_parametros ')' ':' tipo '{' cuerpo_funcion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de apertura para los parametros");} 
							|	'(' lista_parametros ':' tipo '{' cuerpo_funcion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de cierre para los parametros");}
							|	'(' lista_parametros ')' tipo '{' cuerpo_funcion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ':' luego de los parametros");}
							|	'(' lista_parametros ')' ':' '{' cuerpo_funcion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el tipo de retorno de la funcion");}
							|	'(' lista_parametros ')' ':' tipo cuerpo_funcion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave de apertura del cuerpo de la funcion");}
							;
			
error_lista_parametros	:	parametros ',' parametro ',' error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion de los parametros, no se puede tener mas de dos parametros");}
						|	',' parametro {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion de los parametros, falta un parametro antes de la ','");}
						|	parametros ',' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion de los parametros, falta un parametro luego de la ','");}
						|	parametros parametro {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion de los parametros, falta la ',' separando los parametros");}
						;
						
error_parametro	:	error ID {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion del parametro, falta el tipo del parametro");}
				|	tipo error{Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion del parametro, falta el identificador del parametro");}
				;

error_cuerpo_funcion 	: 	retorno error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en el cuerpo de la funcion, falta la llave de cierre");}
						|	error '}' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en el cuerpo de la funcion, falta el retorno");}
						;


error_retorno       :   RETURN ejecucion_retorno {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en el retorno de la funcion, falta el ';'");}
                    |   RETURN ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en el retorno de la funcion, falta la sentencia de retorno");}
                    ;
										

error_retorno_expresion	:	expresion ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de apertura de la expresion de retorno");}
						|	'(' expresion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de cierre de la expresion de retorno");}
						|	'(' ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, no se puede retornar vacio en la expresion de retorno");}
						|	expresion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de apertura y cierre de la expresion de retorno");}
						;
						

error_condicion	:	expresion comparador expresion ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de apertura de la condicion");}
				|	'(' comparador expresion ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la primera expresion en la condicion");}
				|	'(' error expresion error ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el comparador en la condicion");}
				|	'(' expresion comparador ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la segunda expresion en la condicion");}
				|	'(' expresion comparador expresion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de cierre de la condicion");}
				|	'(' error ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, no se permite la condicion vacia");}
				|	expresion comparador expresion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, faltan los parentesis de la condicion");}
				;

error_expresion	:	expresion '+' error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "].Error sintactico, falta el segundo termino de la suma");}	
				|	expresion '-' error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "].Error sintactico, falta el segundo termino de la resta");}
				|	error '+' termino {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "].Error sintactico, falta el primer termino de la suma");}
				;
						
error_termino	:	'*' factor {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el primer factor de la multiplicacion");}
				|	termino '*' error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el segundo factor de la multiplicacion");}
				|	'/' factor {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el primer factor de la division");}
				|	termino '/' error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el segundo factor de la division");}
				;
				
error_factor	:	TOF64 error expresion')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de apertura de la expresion en la conversion tof64");}
				|	TOF64 '(' expresion error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de cierre de la expresion en la conversion tof64");}
				|	TOF64 '(' ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, faltan los parentesis de la expresion en la conversion tof64");}

error_invocacion	:	ID '(' lista_parametros_reales error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de cierre en los parametros de la funcion invocada");}
					;

error_lista_parametros_reales	:	parametros_reales ',' parametro_real ',' error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion de los parametros en la invocacion de la funcion: No se puede tener mas de dos parametros reales");}
								|	',' parametro_real {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion de los parametros en la invocacion de la funcion: Falta un parametro antes de la ','");}
								|	parametros_reales ',' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion de los parametros en la invocacion de la funcion: Falta un parametro luego de la ','");}
								|	parametros_reales parametro_real {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico en la declaracion de los parametros en la invocacion de la funcion: Falta la ',' separando los parametros");}
								;
								
error_asignacion	:	ASSIGN expresion ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el identificador de la variable a asignar");}
					|	ID expresion ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el signo de asignacion");}
					|	ID ASSIGN ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la expresion a asignar");}
					|	ID ASSIGN expresion error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ';' en la sentencia de asignacion");}
					|	ID error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, solo se define el identificador de la asignacion");}
					;
				
error_seleccion	:	IF cuerpo_seleccion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la condicion en la sentencia de seleccion");}
				|	IF condicion error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el cuerpo de la seleccion");}
				;
					
error_cuerpo_seleccion	:	'{' bloque_de_sent_ejecutables '}' cuerpo_else {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el then en la sentencia de seleccion");}
						|	THEN bloque_de_sent_ejecutables '}' cuerpo_else {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave de apertura antes del bloque de sentencias de la seleccion");}
						|	THEN '{' '}' cuerpo_else {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el bloque de sentencias en la sentencia de seleccion");}
						|	THEN '{' bloque_de_sent_ejecutables cuerpo_else {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave de cierre del bloque de sentencias en la sentencia de seleccion");}
						|	'{' bloque_de_sent_ejecutables '}' END_IF ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el then en la sentencia de seleccion");}
						|	THEN bloque_de_sent_ejecutables '}' END_IF ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave de apertura antes del bloque de sentencias de la seleccion");}
						|	THEN '{' '}' END_IF ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el bloque de sentencias en la sentencia de seleccion");}
						|	THEN '{' bloque_de_sent_ejecutables END_IF ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave de cierre del bloque de sentencias en la sentencia de seleccion");}
						|	THEN '{' bloque_de_sent_ejecutables '}' ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el end_if de la seleccion");}
						|	THEN '{' bloque_de_sent_ejecutables '}' END_IF {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ';' luego de la sentencia de seleccion");}
						|	THEN bloque_de_sent_ejecutables cuerpo_else {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, faltan las llaves de apertura y cierre en la sentencia de seleccion");}
						|	THEN bloque_de_sent_ejecutables END_IF ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, faltan las llaves de apertura y cierre en la sentencia de seleccion");}
						;

error_cuerpo_else	:	THEN '{' bloque_de_sent_ejecutables '}' '{' bloque_de_sent_ejecutables '}' END_IF ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el else luego del primer bloque de sentencias de la seleccion");}
					|	THEN '{' bloque_de_sent_ejecutables '}' ELSE bloque_de_sent_ejecutables '}' END_IF ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave de apertura antes del bloque de sentencias luego del else");}
					|	THEN '{' bloque_de_sent_ejecutables '}' ELSE '{' '}' END_IF ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el bloque de sentencias luego del else");}
					|	THEN '{' bloque_de_sent_ejecutables '}' ELSE '{' bloque_de_sent_ejecutables END_IF ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave de cierre del bloque de sentencias luego del else");}
					|	THEN '{' bloque_de_sent_ejecutables '}' ELSE '{' bloque_de_sent_ejecutables '}' ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el end_if de la seleccion");}
					|	THEN '{' bloque_de_sent_ejecutables '}' ELSE '{' bloque_de_sent_ejecutables '}' END_IF {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ';' luego de la sentencia de seleccion");}
					;
					
error_mensaje_pantalla	:	'(' CADENA ')' ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el out en la sentencia de mensaje por pantalla");}
						|	OUT CADENA ')' ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de apertura en la sentencia de mensaje por pantalla");}
						|	OUT '(' CADENA ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de cierre en la sentencia de mensaje por pantalla");}
						|	OUT '(' CADENA ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ';' luego de la sentencia de mensaje por pantalla");}
						|	OUT	'(' ')' ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la cadena en el mensaje por pantalla");}
						|	CADENA {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el out y los parentesis en la sentencia de mensaje por pantalla");}
						|	CADENA ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el out, los parentesis y el punto y coma de cierre en la sentencia de mensaje por pantalla");}
						;

error_invocacion_discard	:	invocacion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el discard antes de la invocacion a la funcion");}
							|	DISCARD error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el nombre de la funcion discard");}
							;
							
error_parametros_discard	:	lista_parametros_reales ')' ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de apertura de los parametros de la funcion discard");}
							|	'(' error ')' ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la lista de parametros reales de la funcion discard");}
							|	'(' lista_parametros_reales ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de cierre de los parametros de la funcion discard");}
							|	'(' lista_parametros_reales ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ';' al final de la funcion discard");}
							;

error_dountil	:	error '{' bloque_de_sentencias_ejecutables '}' UNTIL condicion ':' asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el do en la sentencia do_until");}
				|	etiqueta DO '{' bloque_de_sentencias_ejecutables_etiqueta '}' UNTIL condicion ':' cuerpo_asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ':' luego de la etiqueta en la sentencia do_until");}
				|	':' DO 	{Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la etiqueta antes de los ':' en la sentencia do_until");}
				;


error_cuerpo_dountil	:	bloque_de_sentencias_ejecutables '}' UNTIL condicion ':' cuerpo_asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave '{' de apertura del bloque de sentencias ejecutables en la sentencia do_until");}
						|	'{' '}' UNTIL condicion ':' cuerpo_asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el bloque de sentencias ejecutables en la sentencia do_until");}
						|	'{' bloque_de_sentencias_ejecutables UNTIL condicion ':' cuerpo_asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave '}' de cierre del bloque de sentencias ejecutables en la sentencia do_until");}
						|	'{' bloque_de_sentencias_ejecutables '}' condicion ':' cuerpo_asignacion_do_until ';'  {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el until luego del bloque de sentencias en la sentencia do_until");}
						|	'{' bloque_de_sentencias_ejecutables '}' UNTIL ':' cuerpo_asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la condicion luego del until en la sentencia do_until");}
						;
						
error_cuerpo_dountil_etiqueta	:	bloque_de_sentencias_ejecutables_etiqueta '}' UNTIL condicion ':' cuerpo_asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave '{' de apertura del bloque de sentencias ejecutables en la sentencia do_until con etiqueta");}
								|	'{' '}' UNTIL condicion ':' cuerpo_asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el bloque de sentencias ejecutables en la sentencia do_until");}
								|	'{' bloque_de_sentencias_ejecutables_etiqueta UNTIL condicion ':' cuerpo_asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la llave '}' de cierre del bloque de sentencias ejecutables en la sentencia do_until con etiqueta");}
								|	'{' bloque_de_sentencias_ejecutables_etiqueta '}' condicion ':' cuerpo_asignacion_do_until ';'  {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el until luego del bloque de sentencias en la sentencia do_until con etiqueta");}
								|	'{' bloque_de_sentencias_ejecutables_etiqueta '}' UNTIL ':' cuerpo_asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la condicion luego del until en la sentencia do_until con etiqueta");}
								;
				
error_cuerpo_asignacion_do_until	:	asignacion_do_until ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ':' luego de la condicion en la sentencia do_until");}
									|	':' asignacion_do_until ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de apertura en la asignacion del do_until");}
									|	':' '(' ')' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, error en la asignacion del do_until");}
									|	':' '(' asignacion_do_until error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el parentesis de cierre en la asignacion del do_until");}
									;
									
error_asignacion_do_until	:	ASSIGN expresion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el identificador de la variable a asignar en la sentencia do-until");}
							|	ID expresion {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el signo de asignacion en la sentencia do-until");}
							|	ID ASSIGN error {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la expresion a asignar en la sentencia do-until");}
							;

error_bloque_sent_ejecutables	:	';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta al menos una sentencia ejecutable dentro del bloque de sentencias");}
								|	ejecutables error ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el break luego de la sentencia ejecutable");}
								|	ejecutables BREAK {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ';' luego del break");}
								|	BREAK {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ';' luego del break");}
								;

error_bloque_de_sentencias_ejecutables_etiqueta	:	ejecutables ':' etiqueta ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el break luego de la sentencia");}
												|	ejecutables BREAK etiqueta ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ':' luego del break");}
												|	ejecutables BREAK ':' ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la etiqueta luego del ':'");}
												|	ejecutables BREAK ':' etiqueta {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ';' luego de la etiqueta");}
												|	':' etiqueta ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el break antes del ':'");}
												|	BREAK etiqueta ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ':' luego del break");}
												|	BREAK ':' ';' {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta la etiqueta luego del ':'");}
												|	BREAK ':' etiqueta {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el ';' luego de la etiqueta");}
												|	ejecutables {Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico, falta el break con etiqueta en el cuerpo del do-until con etiqueta");}
												;
												

%% 
private AnalizadorLexico analizadorLexico;
private ArrayList<String> lista_de_variables;
public static int cantidad_parametros = 0;
public static int cantidad_parametros_reales = 0;
public static String nombre_funcion;
public static String ambito;
public static boolean existeDefer = false;
public static boolean agregoCteDbl = false;

public Parser(AnalizadorLexico analizadorLexico)
{
	this.analizadorLexico = analizadorLexico;
	this.lista_de_variables = new ArrayList<String>();
}

public int yylex(){
	Token token = this.analizadorLexico.getToken();
	if(token != null ){
		int val =token.getId();
		yylval = new ParserVal(token.getLexema());
		return val;
	}
   return 0;
}

public void yyerror(String s){
    Main.erroresSintacticos.add("[Parser]. " + s);
}


public void actualizarRango() {
	String lexema = yylval.sval;
	int clave = this.analizadorLexico.tablaSimbolos.obtenerClave(lexema);
	String tipo = this.analizadorLexico.tablaSimbolos.obtenerAtributo(clave, "tipo");
	if (tipo.equals(this.analizadorLexico.CTE_INT_TYPE)){ //Pasar valor desde analizador lexico
	  int nro = 1; //SOLO SE PERMITEN NUMEROS POSITIVOS
	  analizadorLexico.tablaSimbolos.actulizarSimbolo(clave, String.valueOf(nro));
	  Main.estructurasSintacticas.add("[Parser: linea " + analizadorLexico.linea + "]. Se actualiza la constante i16 al valor: " + nro);
	  Main.erroresSintacticos.add("[Parser: linea " + analizadorLexico.linea + "]. Error sintactico: constante i16 fuera de rango");
	}
	else if (tipo.equals(this.analizadorLexico.CTE_DBL_TYPE)) {
		String flotante = "-" + lexema;
		if (this.agregoCteDbl){
			analizadorLexico.tablaSimbolos.actulizarSimbolo(clave, flotante);
		}
		else {
			if (this.analizadorLexico.tablaSimbolos.obtenerClave(flotante) == this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO){
				this.analizadorLexico.tablaSimbolos.agregarSimbolo(flotante);
				clave = this.analizadorLexico.tablaSimbolos.obtenerClave(flotante);
				this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "tipo", this.analizadorLexico.CTE_DBL_TYPE);
			}
			Parser.agregoCteDbl = false;
		}
	}
}

public void incorporarInformacionSemantica(String nombreLexema, String tipoLexema, String usoLexema, String ambitoLexema){
	int clave = this.analizadorLexico.tablaSimbolos.obtenerClave(nombreLexema); //se obtiene la clave
	if(clave != this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO){ // si esta declarada
		if (usoLexema != "etiqueta")
			this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "tipo", tipoLexema); // se agrega el tipo a la tabla de simbolos
		this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "uso", usoLexema); // se agrega el uso a la tabla de simbolos
		this.analizadorLexico.tablaSimbolos.actulizarSimbolo(clave, nombreLexema + "." + ambitoLexema);	// se actualiza el nombre de la variable en la tabla de simbolos
	}
	else{
		clave = this.analizadorLexico.tablaSimbolos.obtenerClave(nombreLexema + "." + ambitoLexema); //se obtiene la clave
		if(clave == this.analizadorLexico.tablaSimbolos.NO_ENCONTRADO){ // si no esta declarada
			this.analizadorLexico.tablaSimbolos.agregarSimbolo(nombreLexema + "." + ambitoLexema);	// se actualiza el nombre de la variable en la tabla de simbolos
			clave = this.analizadorLexico.tablaSimbolos.obtenerClave(nombreLexema + "." + ambitoLexema); //se obtiene la clave
			if (usoLexema != "etiqueta")
				this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "tipo", tipoLexema); // se agrega el tipo a la tabla de simbolos
			this.analizadorLexico.tablaSimbolos.agregarAtributo(clave, "uso", usoLexema); // se agrega el uso a la tabla de simbolos
		}
		else
			Main.erroresSintacticos.add("[Parser: linea " + this.analizadorLexico.linea + "]. Error sintactico " + nombreLexema + ", ya fue declarada en ese ambito.");
	}
}
					
					

	
	
					
					
					

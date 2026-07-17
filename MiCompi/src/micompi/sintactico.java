
package micompi;

import java.util.LinkedList;
import micompi.NodoAST.*;

class sintactico {
    
    private nodo p;
    private boolean errorEncontrado = false;
    
    public sintactico(nodo cabeza) {
        this.p = cabeza;
    }
    
    // Tabla de mensajes de error sintáctico indexados por código.
    String errores[][] = {
        //   0                          1        
        /*0*/{"SÍMBOLO NO RECONOCIDO",     "500"},     
        /*1*/{"SE ESPERA UN DÍGITO",           "501"},
        /*2*/{"SE ESPERA CIERRE DE CADENA",  "502"},
        /*3*/{"SE ESPERA UN \\",             "503"},
        /*4*/{"EOF INESPERADO",              "504"},
        /*5*/{"Se espera Func",              "505"},
        /*6*/{"Se esperaba un '}'",          "506"},
        /*7*/{"Se esperaba un '{'",          "507"},
        /*8*/{"Se esperaba un ')' ",         "508"},
        /*9*/{"Se esperaba un '('",          "509"},
        /*10*/{"Se esperaba un main",         "510"},
        /*11*/{"Identificador o literal esperado", "511"},
        /*12*/{"Se esperaba un Scanln",         "512"},
        /*13*/{"Se esperaba un '.'",          "513"},
        /*14*/{"Se esperaba un tipo de dato", "514"},
        /*15*/{"Se esperaba un Println",      "515"},
        /*16*/{"Operador relacional esperado","516"},
        /*17*/{"Se esperaba un =",          "517"},      
        /*18*/{"Se esperaba una expresión después de operador", "518"},
        /*19*/{"Se esperaba una expresión después de '!'", "519"},
        /*20*/{"Condición esperada para 'if' o 'for'", "520"},
        /*21*/{"Se esperaba un factor", "540"},
    };
    
    private void error(int codigo) {
        if (!errorEncontrado) {
            for (String[] errore : errores) {
                if (codigo == Integer.parseInt(errore[1])) {
                    int renglonError = (p != null) ? p.renglon : 0;
                    System.out.println("ERROR SINTÁCTICO: " + errore[0] + " (código " + codigo + ") cerca del renglón " + renglonError);
                    break;
                }
            }
            errorEncontrado = true;
        }
    }
    
    // Función auxiliar para avanzar al siguiente token
    private void advance() {
        if (p != null) {
            p = p.sig;
        }
    }
    
    /**
     * Punto de entrada del análisis sintáctico.
     * @return El nodo raíz del AST (NodoPrograma), o null si hay un error.
     */
    public NodoPrograma analizar() {
        LinkedList<NodoAST> statements = new LinkedList<>();
        
        if (p != null) {
            if (p.token == 202) { //func
                advance();
                if (p != null && p.token == 225) { //main
                    advance();
                    if (p != null && p.token == 121) { //(
                        advance();
                        if (p != null && p.token == 122) { //)
                            advance();
                            if (p != null && p.token == 123) { //{
                                advance();
                                
                                // Parseamos las declaraciones y las añadimos a la lista
                                statements.addAll(declaracion_var());
                                
                                // Parseamos las instrucciones y las añadimos a la lista
                                statements.addAll(Instrucciones());
                                
                                if (p != null && p.token == 124) { //}
                                    advance();
                                    if (p == null && !errorEncontrado) { // Final del archivo
                                        System.out.println("Analisis sintactico completado correctamente.");
                                        return new NodoPrograma(statements, 1);
                                    } else if (p != null && !errorEncontrado) {
                                        System.out.println("Analisis sintactico completado, pero hay tokens extra después del '}'.");
                                        return new NodoPrograma(statements, 1);
                                    }
                                } else { error(506); } // se esperaba un }
                            } else { error(507); } //Se esperaba un {
                        } else { error(508); } // Se esperaba un )
                    } else { error(509); } //Se esperaba un (
                } else { error(510); } //Se esperaba un main
            } else { error(505); } // Se esperaba func
        } else {
            System.out.println("Archivo de código vacío, nada que analizar.");
        }
        
        return null; // Devuelve null si se encuentra un error en el parseo.
    }

    /**
     * Parsea las declaraciones de variables (var) al inicio del bloque.
     */
    private LinkedList<NodoAST> declaracion_var() {
        LinkedList<NodoAST> declaraciones = new LinkedList<>();
        while (p != null && p.token == 220 && !errorEncontrado) { // var
            int linea = p.renglon;
            advance();
            if (p != null && p.token == 100) { // identificador
                String identificador = p.lexema;
                advance();
                if (p != null && (p.token >= 228 && p.token <= 231)) { // int, float64, string, bool
                    String tipo = obtenerTipoDesdeToken(p.token);
                    declaraciones.add(new NodoDeclaracion(identificador, tipo, linea));
                    advance();
                } else {
                    error(514); // Se esperaba un tipo de dato
                    while(p != null && p.token != 124 && p.token != 127) { advance(); } // Recuperación
                }
            } else {
                error(511); // Se esperaba un identificador
                while(p != null && p.token != 124 && p.token != 127) { advance(); } // Recuperación
            }
        }
        return declaraciones;
    }
    
    /**
     * Parsea la secuencia de instrucciones hasta encontrar el '}' de cierre.
     */
    private LinkedList<NodoAST> Instrucciones() {
        LinkedList<NodoAST> instrucciones = new LinkedList<>();
        while (p != null && p.token != 124 && !errorEncontrado) { // mientras no sea '}'
            NodoAST instruccion = null;
            if (p.token == 100) { // Asignación
                instruccion = Asignacion();         
            } else if (p.token == 222) { // fmt (Lectura o Escritura)
                 if (p.sig != null && p.sig.token == 130 && p.sig.sig != null) {
                     if (p.sig.sig.token == 223) { // fmt.Scanln
                         instruccion = Lectura();
                     } else if (p.sig.sig.token == 224) { // fmt.Println
                         instruccion = Escritura();
                     } else {
                         error(500); // Símbolo no reconocido después de 'fmt.'
                     }
                 }
            } else if (p.token == 214) { // if
                instruccion = if_else();
            } else if (p.token == 218) { // for
                instruccion = While();
            } else {
                // Si no es una instrucción válida, recuperamos el error
                error(500); // Instrucción no válida
                while(p != null && p.token != 124 && p.token != 127) { advance(); }
            }
            
            if (instruccion != null) {
                instrucciones.add(instruccion);
            }
        }
        return instrucciones;
    }

    /**
     * Parsea una asignación: identificador = expresión.
     */
    private NodoAsignacion Asignacion() {
        if (p != null && p.token == 100) { // identificador
            String identificador = p.lexema;
            int linea = p.renglon;
            advance();
            if (p != null && p.token == 111) { // =
                advance();
                NodoAST expresion = Expresion();
                if (expresion != null) {
                    return new NodoAsignacion(identificador, expresion, linea);
                }
            } else {
                error(517); // Se esperaba un =
            }
        } else {
            error(511); // Se esperaba un identificador
        }
        return null; // En caso de error
    }
    
    // --- Jerarquía de expresiones, de menor a mayor precedencia ---

    /**
     * Esta es la función principal para parsear cualquier expresión.
     * Maneja los operadores lógicos (&&, ||), la menor precedencia.
     */
    private NodoAST Expresion() {
        if (p == null || errorEncontrado) return null;
        
        NodoAST izquierda = Relacional(); // Llama al siguiente nivel de precedencia

        // Loop para los operadores lógicos && (118) y || (119)
        while (p != null && (p.token == 118 || p.token == 119)) { 
            String operador = p.lexema;
            int linea = p.renglon;
            advance(); // Consumimos el operador lógico
            NodoAST derecha = Relacional();
            if (derecha == null) {
                error(518); // Se esperaba una expresión después del operador lógico.
                return null;
            }
            izquierda = new NodoOperacion(operador, izquierda, derecha, linea);
        }
        return izquierda;
    }

    /**
     * Maneja los operadores relacionales (==, !=, <, >, <=, >=).
     */
    private NodoAST Relacional() {
        if (p == null || errorEncontrado) return null;
        
        NodoAST izq = Aritmetica(); // Llama al siguiente nivel de precedencia
        
        // Loop para los operadores relacionales (112:==, 113:!=, 116:<, 117:>, 114:<=, 115:>=)
        while (p != null && (p.token == 112 || p.token == 113 || p.token == 116 || p.token == 117 || p.token == 114 || p.token == 115)) { 
            String operador = p.lexema;
            int linea = p.renglon;
            advance(); // Consumimos el operador relacional
            NodoAST der = Aritmetica();
            if (der == null) {
                error(516); // Operador relacional esperado (o una expresión después)
                return null;
            }
            izq = new NodoOperacion(operador, izq, der, linea);
        }
        return izq;
    }

    /**
     * Maneja los operadores aritméticos (+, -, *, /, %).
     */
    private NodoAST Aritmetica() {
    NodoAST izq = Termino();
    while (p != null && (p.token == 104 || p.token == 105)) { // +, -
        String operador = p.lexema;
        int linea = p.renglon;
        advance();
        NodoAST der = Termino();
        if (der == null) {
            error(518); // falta operand
            return null;
        }
        izq = new NodoOperacion(operador, izq, der, linea);
    }
    return izq;
}
    
    private NodoAST Termino() {
    NodoAST izq = Valor();
    while (p != null && (p.token == 106 || p.token == 107 || p.token == 108)) { // *, /, %
        String operador = p.lexema;
        int linea = p.renglon;
        advance();
        NodoAST der = Valor();
        if (der == null) {
            error(518);
            return null;
        }
        izq = new NodoOperacion(operador, izq, der, linea);
    }
    return izq;
}

    /**
     * Maneja los elementos más básicos de una expresión:
     * - Identificadores
     * - Literales (int, float, string, bool)
     * - Paréntesis `()`
     * - El operador unario `!` (NOT)
     */
    private NodoAST Valor() {
        if (p == null || errorEncontrado) {
            error(540); // Se esperaba un factor
            return null;
        }
        
        int linea = p.renglon;

        // Manejar el operador unario '!' (120)
        if (p.token == 120) { // '!'
            advance(); // Consumimos el '!'
            NodoAST operando = Valor(); // El operando también es un Valor
            if (operando == null) {
                error(519); // "Se esperaba una expresión después de '!'".
                return null;
            }
            return new NodoOperacionUnaria("!", operando, linea);
        }
        
        // Manejar paréntesis (121:(), 122:))
        if (p.token == 121) { // '('
            advance(); // Consumimos '('
            NodoAST expresionDentro = Expresion(); // Llamamos a la regla de mayor precedencia
            
            if (p != null && p.token == 122) { // ')'
                advance(); // Consumimos ')'
                return expresionDentro;
            } else {
                error(508); // Se esperaba ')'
                return null;
            }
        } 
        // Manejar identificadores y literales
        else if (p.token == 100 || (p.token >= 101 && p.token <= 103) || (p.token >= 226 && p.token <= 227)) {
            NodoAST nodoFactor;
            if (p.token == 100) {
                nodoFactor = new NodoIdentificador(p.lexema, linea);
            } else { // Es un literal
                String tipoLiteral = "";
                if (p.token == 101) tipoLiteral = "int";
                else if (p.token == 102) tipoLiteral = "float64";
                else if (p.token == 103) tipoLiteral = "string";
                else tipoLiteral = "bool"; // 226, 227
                nodoFactor = new NodoLiteral(p.lexema, tipoLiteral, linea);
            }
            advance();
            return nodoFactor;
        } else {
            error(511); // Identificador, literal o '(' esperado
            return null;
        }
    }
    
    // --- Métodos de instrucción ---

    /**
     * Parsea fmt.Scanln(identificador).
     */
    private NodoScanln Lectura() {
        if(p != null && p.token == 222 && !errorEncontrado) { // 222 = fmt
            int linea = p.renglon;
            advance(); // Consumimos 'fmt'
            if (p != null && p.token == 130) { // 130 = .
                advance(); // Consumimos '.'
                if (p != null && p.token == 223) { // 223 = Scanln
                    advance(); // Consumimos 'Scanln'
                    if (p != null && p.token == 121) { // 121 = (
                        advance(); // Consumimos '('
                        if (p != null && p.token == 100) { // 100 = identificador
                            String identificador = p.lexema;
                            advance(); // Consumimos el identificador
                            if (p != null && p.token == 122) { // 122 = )
                                advance(); // Consumimos ')'
                                return new NodoScanln(identificador, linea);
                            } else { error(508); } //se esperaba un )
                        } else { error(511); }// se esperaba un identificador
                    } else { error(509); } // se esperaba un (
                } else { error(512); } // Se esperaba un Scanln
            } else { error(513); } // Se esperaba un .
        }
        return null; // En caso de error
    }

    /**
     * Parsea fmt.Println(expr, expr, ...).
     */
    private NodoPrintln Escritura() {
        if (p != null && p.token == 222 && !errorEncontrado) { // fmt
            int linea = p.renglon;
            advance(); // Consumimos 'fmt'
            if (p != null && p.token == 130) { // .
                advance(); // Consumimos '.'
                if (p != null && p.token == 224) { // Println
                    advance(); // Consumimos 'Println'
                    if (p != null && p.token == 121) { // (
                        advance(); // Consumimos '('
                        
                        LinkedList<NodoAST> expresiones = new LinkedList<>();
                        
                        // Parseamos la primera expresión, usando la jerarquía de expresiones
                        NodoAST expr = Expresion();
                        if (expr != null) {
                            expresiones.add(expr);
                        }

                        // Acepta múltiples expresiones separadas por coma (127)
                        while (p != null && p.token == 127) {
                            advance(); // Consumimos ','
                            expr = Expresion();
                            if (expr != null) {
                                expresiones.add(expr);
                            }
                        }

                        if (p != null && p.token == 122) { // )
                            advance();
                            return new NodoPrintln(expresiones, linea);
                        } else {
                            error(508); // Se esperaba ')'
                        }
                    } else {
                        error(509); // Se esperaba '('
                    }
                } else {
                    error(515); // Se esperaba 'Println'
                }
            } else {
                error(513); // Se esperaba '.'
            }
        }
        return null;
    }

    /**
     * Parsea if condición { ... } [else { ... }].
     */
    private NodoIfElse if_else() {
        if (p != null && p.token == 214 && !errorEncontrado) { // if
            int linea = p.renglon;
            advance(); // Consumimos 'if'. p ahora apunta a 'activo' (100)
            
            // La condición debe ser una Expresión completa (lógica, relacional, etc.)
            NodoAST condicion = Expresion(); // <-- Consumimos la condición
            
            if (condicion == null) {
                error(520); // Condición esperada
                return null;
            }
            
            // El puntero p debe estar en el token '{' (123)
            if (p != null && p.token == 123) { // {
                advance(); // Consumimos '{'
                LinkedList<NodoAST> bloqueIf = Instrucciones();
                if (p != null && p.token == 124) { // }
                    advance(); // Consumimos '}'
                    LinkedList<NodoAST> bloqueElse = null;
                    if (p != null && p.token == 210) { // else
                        advance(); // Consumimos 'else'
                        if (p != null && p.token == 123) { // {
                            advance();
                            bloqueElse = Instrucciones();
                            if (p != null && p.token == 124) { // }
                                advance();
                            } else { error(506); } // se esperaba un }
                        } else { error(507); } // Se esperaba un {
                    }
                    return new NodoIfElse(condicion, bloqueIf, bloqueElse, linea);
                } else { error(506); } // se esperaba un }
            } else { error(507); } // Se esperaba un {
        }
        return null;
    }

    /**
     * Parsea for condición { ... } (usado como while).
     */
    private NodoFor While() {
        if (p != null && p.token == 218 && !errorEncontrado) { // for 
            int linea = p.renglon;
            advance();
            // La condición debe ser una Expresión completa
            NodoAST condicion = Expresion();
            if (condicion == null) {
                error(520); // Condición esperada
                return null;
            }
            
            if (p != null && p.token == 123) { // {
                advance();
                LinkedList<NodoAST> cuerpo = Instrucciones();
                if (p != null && p.token == 124) { // }
                    advance();
                    return new NodoFor(condicion, cuerpo, linea);
                } else { error(506); } // se esperaba un }
            } else { error(507); } // Se esperaba un {
        }
        return null;
    }

    private String obtenerTipoDesdeToken(int token) {
        return switch (token) {
            case 228 -> "int";
            case 229 -> "float64";
            case 230 -> "string";
            case 231 -> "bool";
            default -> "desconocido";
        };
    }
}
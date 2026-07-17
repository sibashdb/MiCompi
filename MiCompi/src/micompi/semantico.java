package micompi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import micompi.NodoAST.*;

/**
 * Analizador Semántico que recorre el AST para realizar comprobaciones de tipos
 * y otras validaciones semánticas.
 */
public class semantico {

    // Tabla de símbolos: HashMap<nombre_variable, tipo_de_dato>
    private Map<String, String> tablaSimbolos;

    // Bandera para controlar si se ha encontrado algún error
    private boolean errorEncontrado = false;

    // Lista para acumular los errores semánticos encontrados
    private LinkedList<String> erroresSemanticos;

    // Constructor
    public semantico() {
        this.tablaSimbolos = new HashMap<>();
        this.erroresSemanticos = new LinkedList<>();
    }
    
    public Map<String, String> getTablaSimbolos() {
        return this.tablaSimbolos;
    }

    /**
     * Método principal para iniciar el análisis semántico a partir de la raíz
     * del AST.
     *
     * @param astRoot El nodo raíz del Árbol de Sintaxis Abstracta
     * (NodoPrograma).
     */
    public void analizar(NodoAST astRoot) {
        System.out.println("Iniciando análisis semántico..."); 

        // Verificamos si la raíz es un NodoPrograma (como debería ser)
        if (astRoot instanceof NodoPrograma) {
            // Recorremos el árbol visitando cada sentencia
            visitPrograma((NodoPrograma) astRoot);
        } else {
            System.err.println("ERROR SEMÁNTICO CRÍTICO: La raíz del AST no es un NodoPrograma.");
            errorEncontrado = true;
        }

        // Mostramos el resumen de los errores
        if (errorEncontrado) {
            System.out.println("\n--- Resumen de Errores Semánticos ---");
            for (String error : erroresSemanticos) {
                System.err.println(error);
            }
            System.out.println("Análisis semántico terminado con errores.");
        } else {
            System.out.println("Análisis semántico completado correctamente. No se encontraron errores.");
        }
    }

    /**
     * Método auxiliar para registrar un error.
     *
     * @param mensaje El mensaje de error.
     * @param linea El número de línea donde ocurrió el error.
     */
    private void registrarError(String mensaje, int linea) {
        this.errorEncontrado = true;
        this.erroresSemanticos.add("ERROR SEMÁNTICO (Línea " + linea + "): " + mensaje);
    }

    /**
     * --- MÉTODOS DE VISITA PARA CADA TIPO DE NODO --- Implementa la lógica de
     * comprobación para cada tipo de nodo del AST.
     */
    /**
     * Visita el nodo raíz del programa y sus sentencias.
     */
    private void visitPrograma(NodoPrograma nodo) {
        // Itera sobre la lista de sentencias y visita cada una
        for (NodoAST sentencia : nodo.sentencias) {
            // Un 'visitor' genérico que redirige al método específico
            visit(sentencia);
        }
    }

    /**
     * Visita un nodo genérico y llama al método de visita correcto. Este es el
     * núcleo del patrón "Visitor".
     */
    private void visit(NodoAST nodo) {
        if (nodo instanceof NodoDeclaracion) {
            visitDeclaracion((NodoDeclaracion) nodo);
        } else if (nodo instanceof NodoAsignacion) {
            visitAsignacion((NodoAsignacion) nodo);
        } else if (nodo instanceof NodoOperacion) {
            visitOperacion((NodoOperacion) nodo);
        } else if (nodo instanceof NodoOperacionUnaria) {
            visitOperacionUnaria((NodoOperacionUnaria) nodo);
        } else if (nodo instanceof NodoLiteral) {
            visitLiteral((NodoLiteral) nodo);
        } else if (nodo instanceof NodoIdentificador) {
            // No hacemos nada, solo retornamos su tipo.
            // La lógica de verificación se hará en los nodos padre (ej. Asignacion).
        } else if (nodo instanceof NodoPrintln) {
            visitPrintln((NodoPrintln) nodo);
        } else if (nodo instanceof NodoScanln) {
            visitScanln((NodoScanln) nodo);
        } else if (nodo instanceof NodoIfElse) {
            visitIfElse((NodoIfElse) nodo);
        } else if (nodo instanceof NodoFor) {
            visitFor((NodoFor) nodo);
        }
    }

    /**
     * Visita un nodo de declaración de variable. Aquí se agrega la variable a
     * la tabla de símbolos.
     */
    private void visitDeclaracion(NodoDeclaracion nodo) {
        System.out.println("  > Visitando Declaracion: 'var " + nodo.nombre + " " + nodo.tipo + "'");
        // 1. Verificar si la variable ya fue declarada en el alcance actual
        if (tablaSimbolos.containsKey(nodo.nombre)) {
            registrarError("La variable '" + nodo.nombre + "' ya ha sido declarada.", nodo.linea);
        } else {
            // 2. Si no existe, la agregamos a la tabla de símbolos
            tablaSimbolos.put(nodo.nombre, nodo.tipo);
        }
    }

    /**
     * Visita un nodo de asignación. Aquí se verifica que el tipo de la
     * expresión coincida con el tipo de la variable.
     */
    private void visitAsignacion(NodoAsignacion nodo) {
        System.out.println("  > Visitando Asignacion: '" + nodo.identificador + " = ...'");

        // 1. Visitar la expresión del lado derecho para obtener su tipo
        String tipoExpresion = obtenerTipoDeExpresion(nodo.expresion);

        // 2. Obtener el tipo de la variable del lado izquierdo desde la tabla de símbolos
        if (!tablaSimbolos.containsKey(nodo.identificador)) {
            registrarError("La variable '" + nodo.identificador + "' no ha sido declarada.", nodo.linea);
            return; // No podemos continuar con la verificación de tipos
        }
        String tipoVariable = tablaSimbolos.get(nodo.identificador);

        // 3. Verificar la compatibilidad de tipos
        if (!tiposSonCompatibles(tipoVariable, tipoExpresion)) {
            registrarError("Asignación de tipo incompatible. No se puede asignar '" + tipoExpresion + "' a la variable de tipo '" + tipoVariable + "'.", nodo.linea);
        }
    }

    /**
     * Visita un nodo de operación binaria (+, -, *, /, <, >, etc.). Aquí se
     * verifica la compatibilidad de tipos entre operandos.
     */
    private void visitOperacion(NodoOperacion nodo) {
        System.out.println("  > Visitando Operacion: '" + nodo.operador + "'");

        // 1. Obtener los tipos de los operandos recursivamente
        String tipoIzquierdo = obtenerTipoDeExpresion(nodo.izquierdo);
        String tipoDerecho = obtenerTipoDeExpresion(nodo.derecho);

        // 2. Verificar la compatibilidad de los tipos para el operador
        if (!esTipoValidoParaOperacion(nodo.operador, tipoIzquierdo, tipoDerecho)) {
            registrarError("Operación '" + nodo.operador + "' no definida para los tipos '" + tipoIzquierdo + "' y '" + tipoDerecho + "'.", nodo.linea);
        }
    }

    /**
     * Visita un nodo de operación unaria.
     */
    private void visitOperacionUnaria(NodoOperacionUnaria nodo) {
        System.out.println("  > Visitando Operacion Unaria: '" + nodo.operador + "'");
        String tipoOperando = obtenerTipoDeExpresion(nodo.operando);
        if (nodo.operador.equals("!") && !tipoOperando.equals("bool")) {
            registrarError("El operador unario '!' solo puede aplicarse a expresiones de tipo 'bool', no a '" + tipoOperando + "'.", nodo.linea);
        }
    }

    /**
     * Visita un nodo de literal. No hace nada, pero sirve como base.
     */
    private void visitLiteral(NodoLiteral nodo) {
        // No hay nada que verificar en un literal, solo obtenemos su tipo.
    }

    /**
     * Visita un nodo de impresión. Verifica que los argumentos sean de un tipo
     * imprimible.
     */
    private void visitPrintln(NodoPrintln nodo) {
        System.out.println("  > Visitando Println...");
        for (NodoAST expr : nodo.expresiones) {
            String tipo = obtenerTipoDeExpresion(expr);
            if (tipo.equals("desconocido")) {
                registrarError("No se puede imprimir una expresión de tipo 'desconocido'.", nodo.linea);
            }
            // Puedes añadir más validaciones, como tipos que no se puedan imprimir si es necesario.
        }
    }

    /**
     * Visita un nodo de lectura. Verifica que el identificador existe y es de
     * un tipo compatible para lectura.
     */
    private void visitScanln(NodoScanln nodo) {
        System.out.println("  > Visitando Scanln para variable '" + nodo.identificador + "'");
        if (!tablaSimbolos.containsKey(nodo.identificador)) {
            registrarError("La variable '" + nodo.identificador + "' no ha sido declarada para la lectura.", nodo.linea);
        }
    }

    /**
     * Visita un nodo 'if'. Verifica que la condición sea de tipo 'bool'.
     */
    private void visitIfElse(NodoIfElse nodo) {
        System.out.println("  > Visitando sentencia 'if'...");
        // 1. Verificamos la condición
        String tipoCondicion = obtenerTipoDeExpresion(nodo.condicion);
        if (!tipoCondicion.equals("bool")) {
            registrarError("La condición de la sentencia 'if' debe ser de tipo 'bool', pero se encontró '" + tipoCondicion + "'.", nodo.linea);
        }

        // 2. Visitamos los bloques de código recursivamente
        for (NodoAST sentencia : nodo.bloqueIf) {
            visit(sentencia);
        }
        if (nodo.bloqueElse != null) {
            for (NodoAST sentencia : nodo.bloqueElse) {
                visit(sentencia);
            }
        }
    }

    /**
     * Visita un nodo 'for' (while). Verifica que la condición sea de tipo
     * 'bool'.
     */
    private void visitFor(NodoFor nodo) {
        System.out.println("  > Visitando bucle 'for'...");
        // 1. Verificamos la condición
        String tipoCondicion = obtenerTipoDeExpresion(nodo.condicion);
        if (!tipoCondicion.equals("bool")) {
            registrarError("La condición del bucle 'for' debe ser de tipo 'bool', pero se encontró '" + tipoCondicion + "'.", nodo.linea);
        }

        // 2. Visitamos el cuerpo del bucle recursivamente
        for (NodoAST sentencia : nodo.cuerpo) {
            visit(sentencia);
        }
    }

    /**
     * --- MÉTODOS AUXILIARES --- Estos métodos nos ayudan a obtener el tipo de
     * una expresión y a verificar la compatibilidad.
     */
    /**
     * Método recursivo para obtener el tipo de dato de un subárbol de
     * expresión.
     *
     * @param nodo El nodo raíz de la expresión.
     * @return El tipo de dato resultante de la expresión.
     */
    private String obtenerTipoDeExpresion(NodoAST nodo) {
        if (nodo == null) {
            return "desconocido";
        }

        if (nodo instanceof NodoLiteral) {
            return ((NodoLiteral) nodo).tipo;
        } else if (nodo instanceof NodoIdentificador) {
            String nombre = ((NodoIdentificador) nodo).nombre;
            // 1. Buscamos el tipo en la tabla de símbolos
            if (tablaSimbolos.containsKey(nombre)) {
                return tablaSimbolos.get(nombre);
            } else {
                // 2. Si no se encuentra, registramos un error y devolvemos "desconocido"
                registrarError("Uso de la variable no declarada: '" + nombre + "'.", nodo.linea);
                return "desconocido";
            }
        } else if (nodo instanceof NodoOperacion) {
            NodoOperacion opNode = (NodoOperacion) nodo;
            String tipoIzquierdo = obtenerTipoDeExpresion(opNode.izquierdo);
            String tipoDerecho = obtenerTipoDeExpresion(opNode.derecho);

            // Lógica para determinar el tipo resultante de la operación
            return inferirTipoOperacion(opNode.operador, tipoIzquierdo, tipoDerecho);

        } else if (nodo instanceof NodoOperacionUnaria) {
            NodoOperacionUnaria unOpNode = (NodoOperacionUnaria) nodo;
            String tipoOperando = obtenerTipoDeExpresion(unOpNode.operando);
            // El operador '!' siempre resulta en 'bool' si el operando es 'bool'
            if (unOpNode.operador.equals("!") && tipoOperando.equals("bool")) {
                return "bool";
            } else {
                return "desconocido";
            }
        }

        return "desconocido"; // Si el nodo no es una expresión (ej. una sentencia)
    }

    /**
     * Infiere el tipo resultante de una operación binaria.
     *
     * @param operador El operador (+, -, *, /, <, etc.). @
     * param tipo1 El tipo del primer operando.
     * @param tipo2 El tipo del segundo operando.
     * @return El tipo resultante de la operación.
     */
    private String inferirTipoOperacion(String operador, String tipo1, String tipo2) {
        // Operadores relacionales (>, <, >=, <=, ==, !=)
        if (operador.matches("==|!=|<=|>=|<|>")) {
            // La comparación siempre devuelve un booleano
            if ((tipo1.equals("int") || tipo1.equals("float64")) && (tipo2.equals("int") || tipo2.equals("float64"))) {
                return "bool";
            } else if (tipo1.equals("bool") && tipo2.equals("bool")) {
                return "bool";
            } else if (tipo1.equals("string") && tipo2.equals("string") && (operador.equals("==") || operador.equals("!="))) {
                return "bool";
            }
            return "desconocido"; // Tipos incompatibles para comparación
        }

        // Operadores aritméticos (+, -, *, /)
        if (operador.matches("\\+|-|\\*|/")) {
            // Conversión implícita de int a float64
            if (tipo1.equals("float64") || tipo2.equals("float64")) {
                if ((tipo1.equals("int") || tipo1.equals("float64")) && (tipo2.equals("int") || tipo2.equals("float64"))) {
                    return "float64";
                }
            } else if (tipo1.equals("int") && tipo2.equals("int")) {
                return "int";
            } else if (operador.equals("+") && tipo1.equals("string") && tipo2.equals("string")) {
                return "string"; // Concatenación de cadenas
            }
            return "desconocido"; // Operación no definida para estos tipos
        }

        // Operadores lógicos (&&, ||)
        if (operador.equals("&&") || operador.equals("||")) {
            if (tipo1.equals("bool") && tipo2.equals("bool")) {
                return "bool";
            }
            return "desconocido"; // Lógica solo con booleanos
        }

        return "desconocido"; // Operador no reconocido o tipos inválidos
    }

    /**
     * Verifica si una operación es válida para los tipos de sus operandos.
     * En Go los operandos deben ser del mismo tipo para operaciones binarias,
     * sin coerción implícita.
     *
     * @param operador El operador a verificar (ej. "+", ">", "&&").
     * @param tipo1 El tipo del operando izquierdo.
     * @param tipo2 El tipo del operando derecho.
     * @return true si la operación es válida, false en caso contrario.
     */
    private boolean esTipoValidoParaOperacion(String operador, String tipo1, String tipo2) {
        // Si alguno de los tipos es desconocido, no podemos validar.
        if (tipo1.equals("desconocido") || tipo2.equals("desconocido")) {
            return false;
        }

        // A diferencia de otros lenguajes, en Go los tipos deben coincidir exactamente
        // para la mayoría de las operaciones binarias, sin coerción implícita.
        // --- Operadores Aritméticos (+, -, *, /) ---
        if (operador.matches("\\+|-|\\*|/")) {
            // La operación es válida si los tipos son iguales y numéricos (int o float64)
            if (tipo1.equals(tipo2)) {
                return tipo1.equals("int") || tipo1.equals("float64");
            }
            // Caso especial de concatenación de cadenas
            if (operador.equals("+") && tipo1.equals("string") && tipo2.equals("string")) {
                return true;
            }
            return false; // Tipos numéricos diferentes no son compatibles.
        }

        // --- Operadores Relacionales (==, !=, <, >, <=, >=) ---
        if (operador.matches("==|!=|<=|>=|<|>")) {
            // Los tipos de los operandos deben ser idénticos para la comparación
            if (tipo1.equals(tipo2)) {
                // Se pueden comparar números y booleanos
                return tipo1.equals("int") || tipo1.equals("float64") || tipo1.equals("bool");
                // Para strings solo se permite == y !=, pero ya lo manejas con el if de arriba.
            }
            return false; // Tipos diferentes no son comparables.
        }

        // --- Operadores Lógicos (&&, ||) ---
        if (operador.equals("&&") || operador.equals("||")) {
            // Operaciones lógicas solo con booleanos
            return tipo1.equals("bool") && tipo2.equals("bool");
        }

        // Si el operador no es reconocido
        return false;
    }

    /**
     * Verifica si un tipo se puede asignar a otro en Go. En Go, la asignación
     * requiere que los tipos sean idénticos, no hay coerción implícita.
     */
    private boolean tiposSonCompatibles(String tipoDestino, String tipoOrigen) {
        // Si los tipos son desconocidos, no podemos validar.
        if (tipoDestino == null || tipoOrigen == null || tipoDestino.equals("desconocido") || tipoOrigen.equals("desconocido")) {
            return false;
        }

        // La asignación solo es válida si los tipos son exactamente iguales.
        // Go no permite la coerción implícita.
        return tipoDestino.equals(tipoOrigen);
    }

    public boolean huboError() {
        return errorEncontrado;
    }
}

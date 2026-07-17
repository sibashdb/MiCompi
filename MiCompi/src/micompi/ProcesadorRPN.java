package micompi;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ProcesadorRPN {

    private final List<String> codigoRPN;
    private final List<Cuadruplo> cuadruplos;
    private int contadorTemporales;

    public ProcesadorRPN(List<String> codigoRPN) {
        this.codigoRPN = codigoRPN;
        this.cuadruplos = new ArrayList<>();
        this.contadorTemporales = 0;
    }

    private String nuevoTemp() {
        return "t" + contadorTemporales++;
    }

    public List<Cuadruplo> procesar() {
        System.out.println("\n--- INICIANDO PROCESAMIENTO DE RPN A CUÁDRUPLOS ---");
        Stack<String> pila = new Stack<>();

        for (String token : codigoRPN) {
            // Si es un operador, procesa. Si no, es un operando y se apila.
            if (esOperador(token)) {
                // El operador de asignación es un caso especial
                if (token.equals("=")) {
                    String arg1 = pila.pop();
                    String resultado = pila.pop();
                    cuadruplos.add(new Cuadruplo("=", arg1, null, resultado));
                } 
                // Operadores de salto
                else if (token.equals("if_false_goto") || token.equals("goto")) {
                    String etiqueta = pila.pop();
                    String arg1 = (token.equals("if_false_goto")) ? pila.pop() : null;
                    String op = (token.equals("if_false_goto")) ? "IF_FALSE" : "GOTO";
                    cuadruplos.add(new Cuadruplo(op, arg1, null, etiqueta));
                }
                // Operador de impresión
                else if (token.equals("PRINT")) {
                     String arg1 = pila.pop();
                     cuadruplos.add(new Cuadruplo("PRINT", arg1, null, null));
                }
                // Operadores binarios (+, -, *, /, <, >, etc.)
                else {
                    String arg2 = pila.pop();
                    String arg1 = pila.pop();
                    String temp = nuevoTemp();
                    cuadruplos.add(new Cuadruplo(token, arg1, arg2, temp));
                    pila.push(temp); // El resultado se apila para futuras operaciones
                }
            }
            // Si es una etiqueta (ej. "L0:"), se traduce a un cuádruplo LABEL
            else if (token.endsWith(":")) {
                cuadruplos.add(new Cuadruplo("LABEL", null, null, token.replace(":", "")));
            }
            else {
                // Es un operando (variable, número o etiqueta destino), se apila.
                pila.push(token);
            }
        }
        System.out.println("Procesamiento de RPN completado.");
        return cuadruplos;
    }

    private boolean esOperador(String token) {
        return token.matches("[+\\-*/=]|==|!=|<=|>=|<|>|&&|\\|\\||!|if_false_goto|goto|PRINT");
    }
}

package micompi;

import java.io.File;
import java.util.List;
import java.util.Map;
import micompi.NodoAST.NodoPrograma;

public class MiCompi {
    public static void main(String[] args) {
        // Ruta del archivo fuente: se puede pasar como argumento; si no,
        // se usa la ruta por defecto construida de forma portable (Windows/Linux/Mac).
        String rutaCodigo = (args.length > 0)
                ? args[0]
                : "src" + File.separator + "micompi" + File.separator + "codigo.txt";

        // 1. Léxico
        lexico lexico = new lexico(rutaCodigo);
        lexico.analizar();
        if (lexico.errorEncontrado) {
            System.out.println("Error léxico.");
            return;
        }

        // 2. Sintáctico
        sintactico sintactico = new sintactico(lexico.obtenerCabeza());
        NodoPrograma ast = sintactico.analizar();
        if (ast == null) {
            System.out.println("Error sintáctico.");
            return;
        }

        // 3. Semántico
        semantico semantico = new semantico();
        semantico.analizar(ast);

        if (!semantico.huboError()) {
            // 4. Código intermedio en RPN
            Map<String, String> tablaSimbolos = semantico.getTablaSimbolos();
            GeneradorRPN generadorRPN = new GeneradorRPN(tablaSimbolos);
            generadorRPN.generar(ast);
            List<String> codigoRPN = generadorRPN.getCodigoRPN();

            System.out.println("\n--- CÓDIGO INTERMEDIO (RPN) ---");
            System.out.println(String.join(" ", codigoRPN));
            System.out.println("-----------------------------------------");

            // 5. RPN a cuádruplos
            ProcesadorRPN procesador = new ProcesadorRPN(codigoRPN);
            List<Cuadruplo> codigoIntermedio = procesador.procesar();

            // 6. Optimización de cuádruplos
            Optimizador optimizador = new Optimizador(codigoIntermedio);
            List<Cuadruplo> codigoOptimizado = optimizador.optimizar();

            // 7. Generación de código objeto
            GeneradorCodigoObjeto generadorFinal = new GeneradorCodigoObjeto(codigoOptimizado);
            List<String> codigoObjeto = generadorFinal.generar();
            
            System.out.println("\n--- CÓDIGO INTERMEDIO (Sin Optimizar) ---");
            for (int i = 0; i < codigoIntermedio.size(); i++) {
                System.out.println(i + ": " + codigoIntermedio.get(i));
            }
            System.out.println("-----------------------------------------");

            System.out.println("\n--- CÓDIGO INTERMEDIO (OPTIMIZADO) ---");
            for (int i = 0; i < codigoOptimizado.size(); i++) {
                System.out.println(i + ": " + codigoOptimizado.get(i));
            }
            
            System.out.println("\n--- CÓDIGO OBJETO (Ensamblador Simulado) ---");
            for (String linea : codigoObjeto) {
                System.out.println(linea);
            }
            
            System.out.println("--------------------------------------");
        } else {
            System.out.println("Análisis semántico terminó con errores.");
        }
    }
}


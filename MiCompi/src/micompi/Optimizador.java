package micompi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Optimizador {

    private final List<Cuadruplo> codigoOriginal;

    public Optimizador(List<Cuadruplo> codigoOriginal) {
        this.codigoOriginal = codigoOriginal;
    }

    public List<Cuadruplo> optimizar() {
        System.out.println("\n--- INICIANDO FASE DE OPTIMIZACIÓN DE CÓDIGO ---");

         List<Cuadruplo> codigoOptimizado = codigoOriginal;

         // Aplicamos las optimizaciones en pasadas sucesivas.
        System.out.println("  > Aplicando Pase 1: Plegado y Propagación de Constantes...");
        codigoOptimizado = Folding(codigoOptimizado);
        
        
        System.out.println("  > Aplicando Pase 2: Propagación de Copias y Código Muerto...");
        codigoOptimizado = pasePropagacionDeCopias(codigoOptimizado);

        System.out.println("Optimización completada.");
        return codigoOptimizado;
    }

    // --- PASE 1: PLEGADO Y PROPAGACIÓN DE CONSTANTES ---
    private List<Cuadruplo> Folding(List<Cuadruplo> codigoEntrada) {
    List<Cuadruplo> codigoSalida = new ArrayList<>();
    Map<String, String> constantesConocidas = new HashMap<>();

    for (Cuadruplo c : codigoEntrada) {
        String arg1 = constantesConocidas.getOrDefault(c.arg1, c.arg1);
        String arg2 = constantesConocidas.getOrDefault(c.arg2, c.arg2);
        String op = c.operador;
        String res = c.resultado;

        // Al llegar a una etiqueta no podemos garantizar los valores anteriores
        // (puede saltarse desde cualquier punto), así que invalidamos las constantes conocidas.
        if (Objects.equals(op, "LABEL")) {
            constantesConocidas.clear();
            codigoSalida.add(c);
            continue;
        }

        if (esOperacionEvaluable(op) && esNumero(arg1) && esNumero(arg2)) {
            String resultadoString = evaluar(op, arg1, arg2);
            if (res != null) {
                constantesConocidas.put(res, resultadoString);
            }
            codigoSalida.add(new Cuadruplo("=", resultadoString, null, res));
        }
        else if (Objects.equals(op, "=")) {
            if (esNumero(arg1)) {
                constantesConocidas.put(res, arg1);
            } else {
                constantesConocidas.remove(res);
            }
            codigoSalida.add(new Cuadruplo("=", arg1, null, res));
        }
        else {
            if (res != null) {
                constantesConocidas.remove(res);
            }
            codigoSalida.add(new Cuadruplo(op, arg1, arg2, res));
        }
    }
    return codigoSalida;
}
    
    
    /*
     PASE 2: PROPAGACIÓN DE COPIAS Y ELIMINACIÓN DE CÓDIGO MUERTO
     Reemplaza el uso de temporales por su valor directo y elimina las asignaciones
     a temporales que ya no se usan.
     */
    private List<Cuadruplo> pasePropagacionDeCopias(List<Cuadruplo> codigoEntrada) {
        List<Cuadruplo> codigoSalida = new ArrayList<>();
        Map<String, String> copiasConocidas = new HashMap<>();

        for (int i = 0; i < codigoEntrada.size(); i++) {
            Cuadruplo c = codigoEntrada.get(i);
            String arg1 = copiasConocidas.getOrDefault(c.arg1, c.arg1);
            String arg2 = copiasConocidas.getOrDefault(c.arg2, c.arg2);

            Cuadruplo nuevoCuadruplo = new Cuadruplo(c.operador, arg1, arg2, c.resultado);

            // Si la instrucción es una asignación simple (ej. x = t0)
            if (Objects.equals(nuevoCuadruplo.operador, "=") && nuevoCuadruplo.arg1.startsWith("t") && esTemporal(nuevoCuadruplo.arg1)) {
                // Verificamos si este temporal se vuelve a usar.
                boolean esUsadoDespues = false;
                for (int j = i + 1; j < codigoEntrada.size(); j++) {
                    Cuadruplo futuro = codigoEntrada.get(j);
                    if (Objects.equals(futuro.arg1, nuevoCuadruplo.arg1) || Objects.equals(futuro.arg2, nuevoCuadruplo.arg1)) {
                        esUsadoDespues = true;
                        break;
                    }
                    // Si se reasigna, la cadena de uso se rompe.
                    if (Objects.equals(futuro.resultado, nuevoCuadruplo.arg1)) {
                        break;
                    }
                }
                
                // Si el temporal no se vuelve a usar, podemos eliminar la asignación
                if (!esUsadoDespues) {
                    // No hacemos nada, efectivamente eliminando el cuádruplo.
                } else {
                    codigoSalida.add(nuevoCuadruplo);
                }

            } else if (Objects.equals(nuevoCuadruplo.operador, "=") && nuevoCuadruplo.resultado.startsWith("t") && esTemporal(nuevoCuadruplo.resultado)) {
                 // Guardamos la copia: t0 = 5
                 copiasConocidas.put(nuevoCuadruplo.resultado, nuevoCuadruplo.arg1);
                 codigoSalida.add(nuevoCuadruplo); // Mantenemos la asignación por ahora
            }
            else {
                codigoSalida.add(nuevoCuadruplo);
            }
        }
        
        // Una segunda pasada de limpieza para eliminar las asignaciones a temporales que quedaron muertas
        List<Cuadruplo> codigoFinal = new ArrayList<>();
        for (Cuadruplo c : codigoSalida) {
            boolean esAsignacionMuerta = false;
            if (Objects.equals(c.operador, "=") && c.resultado.startsWith("t")) {
                esAsignacionMuerta = true;
                for (Cuadruplo c2 : codigoSalida) {
                    if (Objects.equals(c.resultado, c2.arg1) || Objects.equals(c.resultado, c2.arg2)) {
                        esAsignacionMuerta = false;
                        break;
                    }
                }
            }
            if (!esAsignacionMuerta) {
                codigoFinal.add(c);
            }
        }
        
        return codigoFinal;
    }
    
    // --- Métodos de Ayuda ---
    private String evaluar(String op, String arg1, String arg2) {
        double val1 = Double.parseDouble(arg1);
        double val2 = Double.parseDouble(arg2);

        if (op.matches("[+\\-*/]")) {
            double resultado = 0;
            switch (op) {
                case "+": resultado = val1 + val2; break;
                case "-": resultado = val1 - val2; break;
                case "*": resultado = val1 * val2; break;
                case "/": resultado = (val2 != 0) ? val1 / val2 : 0; break;
            }
            return (resultado % 1 == 0) ? String.valueOf((int)resultado) : String.valueOf(resultado);
        }
        if (op.matches(">|<|>=|<=|==|!=")) {
            boolean resultado = false;
            switch (op) {
                case ">": resultado = val1 > val2; break;
                case "<": resultado = val1 < val2; break;
                case ">=": resultado = val1 >= val2; break;
                case "<=": resultado = val1 <= val2; break;
                case "==": resultado = val1 == val2; break;
                case "!=": resultado = val1 != val2; break;
            }
            return String.valueOf(resultado);
        }
        return "error";
    }

    private boolean esNumero(String s) {
        if (s == null) return false;
        return s.matches("-?\\d+(\\.\\d+)?");
    }


    private boolean esOperacionEvaluable(String op) {
        return op != null && op.matches("[+\\-*/]|>|<|>=|<=|==|!=");
    }
    
 
    
    private boolean esTemporal(String s) {
        if (s == null) return false;
        return s.matches("t\\d+");
    }
    
    public static List<Cuadruplo> aplicarPropagacion(List<Cuadruplo> cuadruplos) {
    Map<String, String> asignaciones = new HashMap<>();
    List<Cuadruplo> resultado = new ArrayList<>();

    for (Cuadruplo c : cuadruplos) {
        String op = c.operador;

        // Si es asignación simple (=, t4, , x)
        if ("=".equals(op) && c.arg1 != null && (c.arg2 == null || c.arg2.isBlank())) {
            asignaciones.put(c.resultado, c.arg1); // x → t4
            resultado.add(c); // aún agregamos el cuádruplo original
        }
        else {
            // Reemplazar arg1 si es una variable propagable
            String nuevoArg1 = asignaciones.getOrDefault(c.arg1, c.arg1);
            String nuevoArg2 = asignaciones.getOrDefault(c.arg2, c.arg2);

            resultado.add(new Cuadruplo(op, nuevoArg1, nuevoArg2, c.resultado));

            // Si esta operación asigna a algo, invalida su valor previo
            if (c.resultado != null) {
                asignaciones.remove(c.resultado);
            }
        }
    }

    return resultado;
}
   

}
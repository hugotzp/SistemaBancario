/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Bitacora;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Primera prueba
 * @author Wilson Xicará
 */
public class Transaccion {
    private Connection conexion;
    private final String SEPARADOR, CARPETA_PRINCIPAL, rutaBitacora;
    private RandomAccessFile bitacora;
    /** Una constante para indicar que la transacción se finaliza con éxito, CONFIRMANDO los cambios realizados. */
    public static final int COMPROMETIDA = 0;
    /** Una constante para indicar que la transacción se finaliza con algún ERROR. */
    public static final int FALLIDA = 3;
    /** Una constante para indicar que la transacción se finaliza con éxito, DESCARTANDO los cambios realizados. */
    public static final int ABORTADA = 4;
    private static final int ACTIVA = 1;
    private static final int PARCIALMENTE_COMPROMETIDA = 2;
    
    
    
    private int longitudBloque, cantidadInstrucciones;
    private long punteroLongitudBloque, punteroEstado, punteroCantidadInstrucciones;
    private boolean iniciada;
    
    /**
     * Inicializa el objeto que implementará la bitácora. Además, intenta hacer un ROLLBACK a la Base de Datos en caso de
     * que hay registro de alguna Transacción que haya finalizado como FALLIDA.
     * @param conexion Conexión activa con la Base de Datos.
     */
    public Transaccion(Connection conexion) {
        this.conexion = conexion;
        // Inicio de la lectura del archivo bitácora
        SEPARADOR = System.getProperty("file.separator");
        CARPETA_PRINCIPAL = System.getProperty("user.home") + SEPARADOR + "BITACORA";
        rutaBitacora = CARPETA_PRINCIPAL + SEPARADOR + "bitacora.log";
        
        File archivoBitacora = new File(rutaBitacora);
        RandomAccessFile archivo;
        if (!archivoBitacora.exists()) { // Si el archivo no existe, se crea uno vacío
            try {
                archivo = new RandomAccessFile(rutaBitacora, "rw");
                String cadenaInicial = "SBlog"; // Firma del archivo
                cadenaInicial+= (char)0 + (char)0 + (char)0;  // Byte reservado más cantidad actual de transacciones guardadas
                archivo.writeBytes(cadenaInicial);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // En caso de que exista el archivo, verifico si tiene almacenado el historial de una transacción anterior.
            // Dicha transacción tuvo un estado fallido, por lo que es necesario hacer un ROLLBACK
            try {
                archivo = new RandomAccessFile(archivoBitacora, "rw");
                archivo.skipBytes(6);   // Salto hasta los 2 bytes con la cantidad de transacciones guardadas
                int contador = Short.toUnsignedInt(archivo.readShort());
                boolean[] borrarBloques = null;
                int[] longBloques = null;
                if (contador > 0) {
                    borrarBloques = new boolean[contador];
                    longBloques = new int[contador];
                }
                
                int auxContador;
                long punteroBloqueT;
                for (int cont=0; cont<contador; cont++) {
                    punteroBloqueT = archivo.getFilePointer();  // Guardo el inicio del cont-ésimo bloque
                    longBloques[cont] = Short.toUnsignedInt(archivo.readShort());  // Obtengo la longitud del cont-ésimo bloque (información de la transacción)
                    archivo.readLine(); // Leo la línea correspondiente a la fecha de la transacción
                    if (Byte.toUnsignedInt(archivo.readByte()) == FALLIDA) {
                        try {
                            conexion.rollback();    // Si la Transacción terminó como fallida, hago un ROLLBACK
                            borrarBloques[cont] = true; // Indico que se borrará el bloque
                        } catch (SQLException ex) {
                            borrarBloques[cont] = false;    // Si ocurre un error, no se borrará el bloque (se tratará de atender en el inicio de la siguiente Transacción)
                            Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    archivo.seek(punteroBloqueT);   // Regreso al inicio del cont-ésimo bloque
                    archivo.skipBytes(longBloques[cont]);  // Salto hasta el siguiente bloque
                }
                // Inicio del borrado de todos los bloques atendidos con un ROLLBACK
                auxContador = contador;
                archivo.seek(8);    // Me muevo hasta el inicio del primer bloque almacenado
                for(int cont=0; cont<contador; cont++) {
                    if (borrarBloques[cont]) {
                        punteroBloqueT = archivo.getFilePointer();
                        archivo.skipBytes(longBloques[cont]);   // Salto hasta el inicio del siguiente bloque
                        byte[] bloque = new byte[(cont==contador-1) ? 1 : (int)(archivo.length()-archivo.getFilePointer())];
                        if (contador != contador-1) {   // Si se elimina un bloque que no es el último
                            archivo.read(bloque);
                            archivo.seek(punteroBloqueT);   // Regreso al inicio del bloque a borrar
                            archivo.write(bloque);  // Escribo todo lo que sigue
                            archivo.setLength(archivo.length() - longBloques[cont] - 2);    // Acorto el tamaño del archivo
                            archivo.seek(punteroBloqueT);   // Regreso al inicio del bloque borrado
                        } else {    // Si se elimina el último bloque
                            archivo.seek(punteroBloqueT);
                            archivo.setLength(archivo.length() - longBloques[cont] - 2);    // Acorto el tamaño del archivo
                        }
                        auxContador--;
                    }
                }
                archivo.seek(6);    // Regreso a donde está la cantidad anterior de Transacciones guardadas
                archivo.writeShort(auxContador);
                archivo.close();
                // HASTA AQUÍ SE GARANTIZA LA ATENCIÓN Y ELIMINACIÓN DE LAS TRANSACCIONES FALLIDAS
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }   // Hasta aquí se garantiza la existencia del archivo
    }
    /**
     * Prepara en el archivo el bloque de información que se almacenará para la Transacción actual.
     */
    public void iniciar() {
        try {
            bitacora = new RandomAccessFile(rutaBitacora, "rw");
            // Llevo el puntero hasta el final del archivo que es en donde se escribirá el registro de la transacción iniciada
            bitacora.seek(bitacora.length() - 1);
            punteroLongitudBloque = bitacora.getFilePointer();
            bitacora.writeShort(0); // Inicialmente la longitud del bloque es 0
            // Obtengo la fecha y hora que identificará a la transacción actual
            ResultSet cConsulta = conexion.createStatement().executeQuery("SELECT NOW()");
            cConsulta.next();
            String aux = cConsulta.getString(1)+'\n';   // Concatenación de la fecha y hora, con un salto de linea al final
            aux+= (char)ACTIVA;  // Concatenación del indicador de que la transacción está activa
            bitacora.writeBytes(aux);   // Escritura de la fecha y hora y del estado de la transacción actual
            punteroCantidadInstrucciones = bitacora.getFilePointer();
            bitacora.writeShort(0); // Inicialmente hay 0 instrucciones SQL ejecutadas
            cantidadInstrucciones = 0;
            longitudBloque = aux.length() + 2;  // Longitudes de fecha y hora, estado y cantidad de instrucciones
            // Escritura de la longitud del bloque de información de la transacción actual
            bitacora.seek(punteroLongitudBloque);
            bitacora.writeShort(longitudBloque);
            // Actualización de la cantidad de Transacciones almacenadas en el archivo
            bitacora.seek(6);
            int cantidad = Short.toUnsignedInt(bitacora.readShort()) + 1;
            bitacora.seek(6);
            bitacora.writeShort(cantidad);
            // Dejo el puntero al final del archivo
            bitacora.seek(bitacora.length() - 1);
            // Indicador de que la transacción ha sido iniciada
            iniciada = true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Escribe en la bitácora la sentencia SQL ejecutada. No interesa si la sentencia es correcta o no, ya que en caso de
     * ocurrir un error se finaliza la conexión y se llama al procedimiento correspondiente.
     * @param sentenciaSQL String con la sentencia SQL enviada a la Base de Datos.
     */
    public void almacenarSentenciaSQL(String sentenciaSQL) {
        // Aquí sólo se guarda la instrucción SQL y se actualiza el contador de cantidad de instrucciones en el archivo
        try {
            bitacora.writeBytes(sentenciaSQL + '\n');
            longitudBloque+= sentenciaSQL.length() + 1;
            // Actualización de la longitud del bloque
            bitacora.seek(punteroLongitudBloque);
            bitacora.writeShort(longitudBloque);
            // Actualización de la cantidad de instrucciones
            bitacora.seek(punteroCantidadInstrucciones);
            bitacora.writeShort(++cantidadInstrucciones);
            // Dejo el puntero al final del archivo
            bitacora.seek(bitacora.length() - 1);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Finaliza el almacenamiento de la información de la Transacción finalizada. Una vez finalizada la Transacción es
     * necesario llamar al método iniciar() para poder utilizar nuevamente el objeto.
     * @param estadoFinal Una de las siguientes constantes de Transaccion: Transaccion.COMPROMETIDA, Transaccion.ABORTADA
     * o Transaccion.FALLIDA que indica el estado con el que se finalizó la Transacción.
     */
    public void finalizar(int estadoFinal) {
        // Aquí se actualiza el estado de la transacción y se elimina el bloque (si no es FALLIDA)
        try {
            // Primero indico que está parcialmente comprometida
            bitacora.seek(punteroEstado);
            bitacora.writeByte(PARCIALMENTE_COMPROMETIDA);
            
            // Verificación del estado final que tuvo la Transacción
            switch (estadoFinal) {
                case FALLIDA:
                    bitacora.seek(punteroEstado);
                    bitacora.writeByte(FALLIDA);
                    break;
                case COMPROMETIDA:
                case ABORTADA:
                    // Se finalizó con éxito
                    // No es necesario guardar el estado pues al final se eliminará el bloque
                    
                    // Inicio de la eliminación del bloque de información de la Transacción finalizada
                    // Se asume que el bloque actual siempre será el último ya que no se puede editar una transacción anterior
                    bitacora.setLength(bitacora.length() - longitudBloque - 2);
                    bitacora.seek(6);
                    int cantidadT = Short.toUnsignedInt(bitacora.readShort());
                    bitacora.seek(6);
                    bitacora.writeShort(--cantidadT);
                    break;
                default:
                    // Si se pasa un parámetro desconocido, se asume que se confirma la transacción
                    bitacora.seek(punteroEstado);
                    bitacora.writeByte(COMPROMETIDA);
                    break;
            }
            // Cierre del archivo
            bitacora.close();
            
            // Indicador de que la transacción ha sido finalizada
            iniciada = false;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Retorna el estado actual de la bitácora, que registra los eventos de la conexión con la Base de Datos.
     * @return Retorna true si la Transacción siguie activa, o false en caso contrario.
     */
    public boolean estaActiva() { return iniciada; }
}

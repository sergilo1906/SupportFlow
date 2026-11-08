
package com.supportflow.util;

import com.supportflow.dao.TicketDAO;

// Clase InicializarBD per crear la BD
public class InicializarBD {

    public static void main(String[] args) {

        // Crear l'objecte ConexionBD
        ConexionBD baseDatos = new ConexionBD();

        try {
            // Crear les taules i conservar les dades anteriors
            EsquemaBD.inicializar(baseDatos);

            System.out.println("Base de datos preparada correctamente");

            // Crear l'objecte TicketDAO
            TicketDAO ticketDAO = new TicketDAO();

            // Mostrar els tickets guardats
            ticketDAO.mostrarTickets();

        } catch(Exception error) {
            System.out.println("Error: " + error.getMessage());
        }
    }
}


package autograde.logica;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;

/**
 *
 * @author juanl
 */
public class Conex {
    Connection cn;
    
    public Connection conectarBD(){
        
        try{
            Class.forName("com.mysql.jdbc.Driver");
            cn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/autograde","root","");
            System.out.println("Conexion exitosa");
        }catch(Exception e){
            System.out.println("Conexion fallida"+e);
        }
        return cn;
    }
}


package autograde.logica;

/**
 *
 * @author juanl
 */
public class Usuario {
    
    private int id;
    private String correoUsuario;
    private String contraseña;

    public Usuario() {
    }

    public Usuario(int id, String correoUsuario, String contraseña) {
        this.id = id;
        this.correoUsuario = correoUsuario;
        this.contraseña = contraseña;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }
    
    
}

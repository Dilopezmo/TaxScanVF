package autograde.igu.component;

import autograde.igu.swing.Button;
import autograde.igu.swing.MyPasswordField;
import autograde.igu.swing.MyTextField;
import autograde.logica.Conex;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.sql.ResultSet;
import net.miginfocom.swing.MigLayout;

public class PanelLoginAndRegister extends javax.swing.JLayeredPane{
        Conex con = new Conex();
        Connection cn = con.conectarBD();

    public PanelLoginAndRegister() {
        initComponents();
        initRegister();
        initLogin();
        login.setVisible(false);
        register.setVisible(true);
        
    }

    private void initRegister() {
        register.setLayout(new MigLayout("wrap", "push[center]push", "push[]25[]10[]10[]25[]push"));
        JLabel label = new JLabel("Create Account");
        label.setFont(new Font("sansserif", 1, 30));
        label.setForeground(new Color(7, 164, 121));
        register.add(label);
        MyTextField txtUser = new MyTextField();       
        txtUser.setPrefixIcon(new ImageIcon(getClass().getResource("/autograde/igu/icon/user.png")));
        txtUser.setHint("Name");
        register.add(txtUser, "w 60%");
        MyTextField txtEmail = new MyTextField();
        txtEmail.setPrefixIcon(new ImageIcon(getClass().getResource("/autograde/igu/icon/mail.png")));
        txtEmail.setHint("Email");
        register.add(txtEmail, "w 60%");
        MyPasswordField txtPass = new MyPasswordField();
        txtPass.setPrefixIcon(new ImageIcon(getClass().getResource("/autograde/igu/icon/pass.png")));
        txtPass.setHint("Password");
        register.add(txtPass, "w 60%");
        JButton cmdSignUp = new JButton();
        cmdSignUp.setBackground(new Color(7, 164, 121));
        cmdSignUp.setForeground(new Color(250, 250, 250));
        cmdSignUp.setText("SIGN UP");
        cmdSignUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String nombre = txtUser.getText();
                String email = txtEmail.getText();
                String contraseña = txtPass.getText();
                // Validar que los campos no estén vacíos
                if(nombre.isEmpty() || email.isEmpty() || contraseña.isEmpty()) {
                    JOptionPane.showMessageDialog(null,"Por favor completar datos");
                } else {
                    try {
                        // Verificar si el correo o el nombre de usuario ya están registrados
                        PreparedStatement ps = (PreparedStatement) cn.prepareStatement("SELECT * FROM usuarios WHERE Correo=? OR Nombre=?");
                        ps.setString(1, email);
                        ps.setString(2, nombre);
                        ResultSet rs = ps.executeQuery();
                        if(rs.next()) {
                            // Ya existe un usuario con ese correo o nombre
                            JOptionPane.showMessageDialog(null, "Correo o nombre de usuario ya registrados");
                        } else {
                            // No existe, se puede proceder con el registro
                            String consulta = "INSERT into usuarios(Nombre,Correo,Contraseña) values('" + nombre + "','" + email + "','" + contraseña + "')";
                            ps = (PreparedStatement) cn.prepareStatement(consulta);
                            ps.executeUpdate();
                            JOptionPane.showMessageDialog(null,"Datos registrados correctamente");
                            txtUser.setText("");
                            txtEmail.setText("");
                            txtPass.setText("");
                        }
                    } catch(Exception e) {
                        JOptionPane.showMessageDialog(null, "Error al registrar los datos" + e);
                    }
                }
            }
        });
        register.add(cmdSignUp, "w 40%, h 40");
        
    }
    
    private void initLogin() {
        login.setLayout(new MigLayout("wrap", "push[center]push", "push[]25[]10[]10[]25[]push"));
        JLabel label = new JLabel("Sign In");
        label.setFont(new Font("sansserif", 1, 30));
        label.setForeground(new Color(7, 164, 121));
        login.add(label);
        MyTextField txtEmail = new MyTextField();
        txtEmail.setPrefixIcon(new ImageIcon(getClass().getResource("/autograde/igu/icon/mail.png")));
        txtEmail.setHint("Email");
        login.add(txtEmail, "w 60%");
        MyPasswordField txtPass = new MyPasswordField();
        txtPass.setPrefixIcon(new ImageIcon(getClass().getResource("/autograde/igu/icon/pass.png")));
        txtPass.setHint("Password");
        login.add(txtPass, "w 60%");
        JButton cmdForget = new JButton("Forgot your password ?");
        cmdForget.setForeground(new Color(100, 100, 100));
        cmdForget.setFont(new Font("sansserif", 1, 12));
        cmdForget.setContentAreaFilled(false);
        cmdForget.setCursor(new Cursor(Cursor.HAND_CURSOR));
        login.add(cmdForget);
        Button cmdSignIn = new Button();
        cmdSignIn.setBackground(new Color(7, 164, 121));
        cmdSignIn.setForeground(new Color(250, 250, 250));
        cmdSignIn.setText("SIGN IN");
        
       cmdSignIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String usuario = txtEmail.getText();
                String pass = txtPass.getText();
                // Validar que los campos no estén vacíos
                if(!usuario.equals("") && !pass.equals("")) {
                // Aquí puedes agregar la lógica para verificar las credenciales del usuario
                    try {
                    // Verificar las credenciales en la base de datos o donde sea que las tengas almacenadas
                        PreparedStatement ps = (PreparedStatement) cn.prepareStatement("SELECT * FROM usuarios WHERE Correo=? AND Contraseña=?");
                        ps.setString(1, usuario);
                        ps.setString(2, pass);
                        ResultSet rs = ps.executeQuery();
                            if(rs.next()) {
                            // Credenciales válidas, hacer algo (por ejemplo, abrir una nueva ventana)
                            // Aquí puedes abrir la nueva ventana o realizar alguna otra acción
                            JOptionPane.showMessageDialog(null, "Credenciales válidas");
                            txtEmail.setText("");
                            txtPass.setText("");
                            } else {
                            // Credenciales inválidas, mostrar mensaje de error
                                JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos");
                            }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Error al verificar las credenciales");
                    }
                } else {
                     // Mostrar mensaje si faltan datos
                    JOptionPane.showMessageDialog(null, "Por favor completar los campos");
                }
            }
        });
        login.add(cmdSignIn, "w 40%, h 40");
    }
    

  

    public void showRegister(boolean show) {
        if (show) {
            register.setVisible(true);
            login.setVisible(false);
        } else {
            register.setVisible(false);
            login.setVisible(true);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        login = new javax.swing.JPanel();
        register = new javax.swing.JPanel();

        setLayout(new java.awt.CardLayout());

        login.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout loginLayout = new javax.swing.GroupLayout(login);
        login.setLayout(loginLayout);
        loginLayout.setHorizontalGroup(
            loginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        loginLayout.setVerticalGroup(
            loginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        add(login, "card3");

        register.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout registerLayout = new javax.swing.GroupLayout(register);
        register.setLayout(registerLayout);
        registerLayout.setHorizontalGroup(
            registerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 327, Short.MAX_VALUE)
        );
        registerLayout.setVerticalGroup(
            registerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        add(register, "card2");
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel login;
    private javax.swing.JPanel register;
    // End of variables declaration//GEN-END:variables

 
}


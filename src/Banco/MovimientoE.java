/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Banco;

import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author SERGIO MALDONADO
 */
public class MovimientoE extends javax.swing.JFrame {

    /**
     * Creates new form MovDeposito
     */
    Connection conexion;
    ResultSet cuenta;
    //int tipo = 2;
    public void DepositoE(){
        String Instruccion = "",Instruccion2 = "";
        String monto = Monto.getText();
        PreparedStatement pst = null;
        if(monto.equals("")){
                JOptionPane.showMessageDialog(null, "Campo Vacio");
        }
        else{
            try {
                 //Iniciamos Transaccion
                pst = conexion.prepareStatement("START TRANSACTION");
                int a = pst.executeUpdate();
                
                //Insert al movimiento
                Instruccion = "INSERT INTO movimiento (movimiento.Cuenta_Id,movimiento.Fecha,movimiento.Monto,movimiento.Tipo) VALUES (" +
                                cuenta.getString(1) + ",NOW()," + monto + "," + "3" + ");";
                pst = conexion.prepareStatement(Instruccion);
                a = pst.executeUpdate();
                    
                 //Update a la cuenta
                Instruccion2 = "UPDATE cuenta SET cuenta.Saldo = cuenta.Saldo + " + monto + "WHERE cuenta.Id = " + cuenta.getString(1) + ";";
                pst = conexion.prepareStatement(Instruccion2);
                a = pst.executeUpdate();
                    
                //Hacemos commit
                pst = conexion.prepareStatement("COMMIT");
                a = pst.executeUpdate();
                System.out.println("Se logró el commit");
                JOptionPane.showMessageDialog(null, "Se ha realizado el Movimiento a la cuenta " + cuenta.getString(2));
            } catch (SQLException ex) {
                System.err.println("ERROR: " + ex.getMessage());
                try {
                    pst = conexion.prepareStatement("ROLLBACK;");
                    int b = pst.executeUpdate();
                    System.out.println("Se logró el Rollback");
                    JOptionPane.showMessageDialog(null, "Error Inesperado");
                    //conexion.rollback();
                } catch (SQLException ex1) {
                    Logger.getLogger(MovimientoE.class.getName()).log(Level.SEVERE, null, ex1);
                    System.out.println("No se pudo realizar el Rollback");
                }
                Logger.getLogger(MovimientoE.class.getName()).log(Level.SEVERE, null, ex);
            }   
        }
    }
    public void RetiroE(){
        try {
            String Instruccion = "",Instruccion2 = "";
            String monto = Monto.getText();
            PreparedStatement pst = null;
            if(monto.equals("")){
                JOptionPane.showMessageDialog(null, "Campo Vacio");
            }
            else if (Integer.parseInt(monto)>Integer.parseInt(cuenta.getString(5))){
                JOptionPane.showMessageDialog(null, "La cuenta no tiene saldo suficiente para realizar esta operacion");
            }
            else{
                try {
                    //Iniciamos Transaccion
                    pst = conexion.prepareStatement("START TRANSACTION");
                    int a = pst.executeUpdate();
                    
                    //Insert al movimiento
                    Instruccion = "INSERT INTO movimiento (movimiento.Cuenta_Id,movimiento.Fecha,movimiento.Monto,movimiento.Tipo) VALUES (" +
                            cuenta.getString(1) + ",NOW()," + monto + "," + "2" + ");";
                    
                    pst = conexion.prepareStatement(Instruccion);
                    a = pst.executeUpdate();
                    
                    //Update a la cuenta
                    Instruccion2 = "UPDATE cuenta SET cuenta.Saldo = cuenta.Saldo - " + monto + "WHERE cuenta.Id = " + cuenta.getString(1) + ";";
                    pst = conexion.prepareStatement(Instruccion2);
                    a = pst.executeUpdate();
                    
                    //Hacemos commit
                    pst = conexion.prepareStatement("COMMIT");
                    a = pst.executeUpdate();
                    System.out.println("Se logró el commit");
                    JOptionPane.showMessageDialog(null, "Se ha realizado el Movimiento a la cuenta " + cuenta.getString(2));
                } catch (SQLException ex) {
                    System.err.println("ERROR: " + ex.getMessage());
                    try {
                        pst = conexion.prepareStatement("ROLLBACK;");
                        int b = pst.executeUpdate();
                        System.out.println("Se logró el Rollback");
                        JOptionPane.showMessageDialog(null, "Error Inesperado");
                        //conexion.rollback();
                    } catch (SQLException ex1) {
                        Logger.getLogger(MovimientoE.class.getName()).log(Level.SEVERE, null, ex1);
                        System.out.println("No se pudo realizar el Rollback");
                    }
                    Logger.getLogger(MovimientoE.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MovimientoE.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public MovimientoE() {
        initComponents();
    }
    public MovimientoE(Connection conex,ResultSet cuen) {
        initComponents();
        conexion = conex;
        cuenta = cuen;
        try {
            NoCuenta.setText(cuenta.getString(2));
        } catch (SQLException ex) {
            Logger.getLogger(MovimientoE.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        TipoT = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        Monto = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        NoCuenta = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Efectivo"));

        jLabel1.setText("Tipo");

        TipoT.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Deposito", "Retiro" }));
        TipoT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TipoTActionPerformed(evt);
            }
        });

        jLabel2.setText("Monto");
        jLabel2.setToolTipText("");

        Monto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                MontoKeyTyped(evt);
            }
        });

        jButton1.setText("Aceptar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Cancelar");

        jLabel4.setText("NoCuenta");

        NoCuenta.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4))
                .addGap(61, 61, 61)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TipoT, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2))
                    .addComponent(Monto)
                    .addComponent(NoCuenta))
                .addContainerGap(130, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NoCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(Monto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TipoT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(70, 70, 70)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(92, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TipoTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TipoTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TipoTActionPerformed

    private void MontoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MontoKeyTyped
        // TODO add your handling code here:
        char letra = evt.getKeyChar();
        if(((letra < '0') || (letra > '9')) && (letra != KeyEvent.VK_BACK_SPACE) && (letra !='.')){
            //System.out.println("Entro al condicion evento");
            evt.consume();
        }
        if (letra == '.' && Monto.getText().contains(".")) {
            evt.consume();
        }
    }//GEN-LAST:event_MontoKeyTyped

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if(TipoT.getSelectedIndex()==0){        //Es Deposito
            DepositoE();
        }
        else if(TipoT.getSelectedIndex()==1){   //Es Retiro
            RetiroE();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MovimientoE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MovimientoE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MovimientoE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MovimientoE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MovimientoE().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Monto;
    private javax.swing.JTextField NoCuenta;
    private javax.swing.JComboBox<String> TipoT;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}

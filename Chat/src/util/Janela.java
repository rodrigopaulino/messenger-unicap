/*
 * Este arquivo é propriedade de Rodrigo Paulino Ferreira de Souza.
 * Nenhuma informação nele contida pode ser reproduzida,
 * mostrada ou revelada sem permissão escrita do mesmo.
 */
package util;

import java.awt.event.KeyEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Classe geradora da interface grafica do Chat
 *
 * @author rodrigopaulino
 */
public class Janela extends javax.swing.JFrame {
	//~ Atributos de instancia -----------------------------------------------------------------------------------------------------

	private javax.swing.JButton aJButton;
	private javax.swing.JComboBox aJComboBox;
	private javax.swing.JLabel aJLabel;
	private javax.swing.JPanel aJPanel;
	private javax.swing.JScrollPane aJScrollPane;
	private javax.swing.JTextArea aJTextArea;
	private javax.swing.JTextField aJTextField;

	//~ Construtores ---------------------------------------------------------------------------------------------------------------

/**
     * Creates new form Janela
     */
	public Janela() {
		initComponents();
	}

	//~ Metodos --------------------------------------------------------------------------------------------------------------------

	/**
	 * -
	 */
	private void initComponents() {
		this.setTitle("SD Messenger");

		aJPanel = new javax.swing.JPanel();
		aJScrollPane = new javax.swing.JScrollPane();
		aJTextArea = new javax.swing.JTextArea();
		aJTextField = new javax.swing.JTextField();
		aJButton = new javax.swing.JButton();
		aJComboBox = new javax.swing.JComboBox();
		aJLabel = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		aJTextArea.setColumns(20);
		aJTextArea.setRows(5);
		aJTextArea.setEditable(false);
		aJScrollPane.setViewportView(aJTextArea);

		aJButton.setText("Enviar");
		aJButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					jButtonMouseClicked(evt);
				}
			});
		aJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent evt) {
					jTextFieldKeyPressed(evt);
				}
			});

		aJComboBox.setModel(new javax.swing.DefaultComboBoxModel());

		aJLabel.setText("Enviar para:");

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(aJPanel);
		aJPanel.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
					.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(aJScrollPane)
						.addComponent(aJTextField)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
							jPanel1Layout.createSequentialGroup().addGap(6, 6, 6)
							.addComponent(aJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 82,
								javax.swing.GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(aJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 176, Short.MAX_VALUE)
							.addComponent(aJButton))).addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
					.addComponent(aJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(aJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
							.addComponent(aJButton).addComponent(aJLabel))
						.addComponent(aJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
							javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap(7, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(aJPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE,
					javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(aJPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE,
					javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		pack();
	}

	/**
	 * - Metodo executado ao clicar no botao Enviar
	 *
	 * @param evt
	 */
	private void jButtonMouseClicked(java.awt.event.MouseEvent evt) {
		Socket socket;
		DataOutputStream transmissorDadosSaida;

		try {
			if (!aJTextField.getText().equals("")) {
				// Cria um socket e envia a mensagem para ser encaminhada para o destino
				socket = new Socket(Constantes.ADDRESS_FRONT_END, Constantes.PORT_NUMBER_FRONT_END);
				transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
				transmissorDadosSaida.writeBytes(Constantes.ID_ACAO_ENVIO_MSG + '\n');
				transmissorDadosSaida.writeBytes(aJTextField.getText() + '\n');
				transmissorDadosSaida.writeBytes(((Usuario) aJComboBox.getSelectedItem()).getEndereco());
				socket.close();

				// Atualiza os campos da janela
				aJTextArea.append('\n' + "[Eu " +
					new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) + "]: " +
					aJTextField.getText());
				aJTextField.setText("");
			}
		} catch (IOException e) {
			appendMessage("SD Messenger: O servidor parece nao estar mais funcionando! Volte a entrar no Chat outra hora.");
		}
	}

	/**
	 * - Metodo executado ao clicar na tecla ENTER quando estiver digitando algo no campo de texto
	 *
	 * @param evt
	 */
	private void jTextFieldKeyPressed(java.awt.event.KeyEvent evt) {
		Socket socket;
		DataOutputStream transmissorDadosSaida;

		try {
			if ((evt.getKeyCode() == KeyEvent.VK_ENTER) && !aJTextField.getText().equals("")) {
				// Cria um socket e envia a mensagem para ser encaminhada para o destino
				socket = new Socket(Constantes.ADDRESS_FRONT_END, Constantes.PORT_NUMBER_FRONT_END);
				transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
				transmissorDadosSaida.writeBytes(Constantes.ID_ACAO_ENVIO_MSG + '\n');
				transmissorDadosSaida.writeBytes(aJTextField.getText() + '\n');
				transmissorDadosSaida.writeBytes(((Usuario) aJComboBox.getSelectedItem()).getEndereco());
				socket.close();

				// Atualiza os campos da janela
				aJTextArea.append('\n' + "[Eu " +
					new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) + "]: " +
					aJTextField.getText());
				aJTextField.setText("");
			}
		} catch (IOException e) {
			appendMessage("SD Messenger: O servidor parece nao estar mais funcionando! Volte a entrar no Chat outra hora.");
		}
	}

	/**
	 * - Metodo responsavel por imprimir no TextArea as mensagens a serem exibidas
	 *
	 * @param pMensagem
	 */
	public void appendMessage(String pMensagem) {
		this.aJTextArea.setText((this.aJTextArea.getText().equals("")) ? pMensagem : (this.aJTextArea.getText() + '\n' + pMensagem));
	}

	/**
	 * - Metodo responsavel por atualizar a lista de Usuarios no ComboBox
	 *
	 * @param pNmUsuarios
	 */
	public void attUsuariosLogados(String pNmUsuarios) {
		String[] usuarios = pNmUsuarios.split(", ");
		this.aJComboBox.removeAllItems();

		for (int i = 0; i < usuarios.length; i++) {
			this.aJComboBox.addItem(new Usuario(usuarios[i].split("=")[1], usuarios[i].split("=")[0]));
		}
	}
}

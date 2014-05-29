/*
 * Este arquivo È propriedade de Rodrigo Paulino Ferreira de Souza.
 * Nenhuma informaÁ„o nele contida pode ser reproduzida,
 * mostrada ou revelada sem permiss„o escrita do mesmo.
 */
package cliente;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import util.Constantes;
import util.Janela;

/**
 * Classe do cliente do Chat
 *
 * @author rodrigopaulino
 */
public class Cliente extends Thread {
	//~ Atributos de instancia -----------------------------------------------------------------------------------------------------

	private Janela aJanela = new Janela();
	private ServerSocket aServerSocket;

	//~ Construtores ---------------------------------------------------------------------------------------------------------------

/**
         * Cria um novo objeto Cliente.
         *
         * @param pServerSocket  
         */
	Cliente(ServerSocket pServerSocket) {
		aServerSocket = pServerSocket;
	}

	//~ Metodos --------------------------------------------------------------------------------------------------------------------

	/**
	 * -
	 */
	public void run() {
		Socket socket;
		DataOutputStream transmissorDadosSaida;
		BufferedReader leitorEntrada;
		String ultimaMensagemLog = "";

		try {
			// Solicita inclus√£o do cliente nos usu√°rios cadastrados no Front End
			socket = new Socket(Constantes.ADDRESS_FRONT_END, Constantes.PORT_NUMBER_FRONT_END);
			transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
			transmissorDadosSaida.writeBytes(Constantes.ID_ACAO_LOGIN);
			socket.close();

			while (true) {
				String inMensagemFrontEnd = "";

				// Espera chegada de mensagem proveniente do Front End
				socket = this.aServerSocket.accept();
				leitorEntrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				inMensagemFrontEnd = leitorEntrada.readLine();

				// Verifica o Login foi bem sucedido para ent√£o atualizar a lista de usu√°rios e carregar a √∫ltima mensagem gravada no log
				if (inMensagemFrontEnd.equals(Constantes.ID_SUCESSO)) {
					aJanela.attUsuariosLogados(leitorEntrada.readLine());
					aJanela.appendMessage("SD Messenger: Conectado com sucesso!");

					ultimaMensagemLog = leitorEntrada.readLine();

					if (ultimaMensagemLog != null) {
						aJanela.appendMessage(ultimaMensagemLog);
					}
				} else if (inMensagemFrontEnd.equals(Constantes.ID_MENSAGEM)) { // Caso o cliente esteja recebendo uma mensagem, exibe-a
					aJanela.appendMessage(leitorEntrada.readLine());
				} else {
					aJanela.appendMessage("SD Messenger: Nao foi possivel se conectar ao servidor! Tente novamente mais tarde.");
				}
				socket.close();
			}
		} catch (IOException e) {
			aJanela.appendMessage("SD Messenger: Ocorreu um erro durante a conexao com o servidor! Feche e entre novamente.");
		}
	}

	/**
	 * - Executa a aplicacao cliente do Chat
	 *
	 * @param args
	 *
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Cliente construtor = new Cliente(new ServerSocket(Constantes.PORT_NUMBER_CLIENTE));
		construtor.aJanela.setVisible(true);
		construtor.start();
	}
}

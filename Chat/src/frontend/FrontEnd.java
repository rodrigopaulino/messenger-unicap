/*
 * Este arquivo é propriedade de Rodrigo Paulino Ferreira de Souza.
 * Nenhuma informação nele contida pode ser reproduzida,
 * mostrada ou revelada sem permissão escrita do mesmo.
 */
package frontend;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;

import util.Constantes;

/**
 * Classe que implementa o servidor front end da aplicacao
 *
 * @author rodrigopaulino
 */
public class FrontEnd extends Thread {
	//~ Atributos de instancia -----------------------------------------------------------------------------------------------------

	private Hashtable aUsuariosLogados = new Hashtable();
	private ServerSocket aServerSocket;

	//~ Construtores ---------------------------------------------------------------------------------------------------------------

/**
         * Cria um novo objeto FrontEnd.
         *
         * @param pServerSocket  
         */
	FrontEnd(ServerSocket pServerSocket) {
		aServerSocket = pServerSocket;
	}

	//~ Metodos --------------------------------------------------------------------------------------------------------------------

	/**
	 * -
	 */
	public void run() {
		while (true) {
			Socket socket;
			BufferedReader leitorEntrada;
			DataOutputStream transmissorDadosSaida;
			InetAddress enderecoRemetente;
			String mensagemRecebida = "";
			String usuarioDestino = "";
			String acaoRequisitada;

			try {
				// Espera chegada de solicitacao de acao proveniente do Cliente
				socket = aServerSocket.accept();
				leitorEntrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				acaoRequisitada = leitorEntrada.readLine();

				if (acaoRequisitada.equals(Constantes.ID_ACAO_LOGIN)) {
					// Guarda o endereco do cliente para inclui-lo na tabela de usuarios logados
					enderecoRemetente = socket.getInetAddress();
					socket.close();

					// Faz solicitacao de acao aos gerenciadores
					solicitarAcaoGerenciadorReplica(Constantes.ID_ACAO_RESGATAR_LOG, null);

					// Espera a chegada da resposta do gerenciador de replica
					socket = aServerSocket.accept();
					leitorEntrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					// Caso tenha conseguido sucesso na solicitacao, retorna ao cliente os usuarios logados e a ultima mensagem do log
					if (leitorEntrada.readLine().equals(Constantes.ID_SUCESSO)) {
						mensagemRecebida = leitorEntrada.readLine();
						socket.close();

						// Inclui o usuario na tabela de usuarios logados
						aUsuariosLogados.put(enderecoRemetente.getHostAddress(), enderecoRemetente.getHostName());

						socket = new Socket(enderecoRemetente, Constantes.PORT_NUMBER_CLIENTE);
						transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
						transmissorDadosSaida.writeBytes(Constantes.ID_SUCESSO + '\n');
						transmissorDadosSaida.writeBytes(aUsuariosLogados.values().toString() + '\n');
						transmissorDadosSaida.writeBytes((mensagemRecebida == null) ? "" : mensagemRecebida);
						socket.close();
					} else { // Caso haja falha na recuperacao da mensagem do log, informa falha ao cliente
						socket = new Socket(enderecoRemetente, Constantes.PORT_NUMBER_CLIENTE);
						transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
						transmissorDadosSaida.writeBytes(Constantes.ID_FALHA);
						socket.close();
					}
				} else if (acaoRequisitada.equals(Constantes.ID_ACAO_ENVIO_MSG)) {
					// Guarda a mensagem, o usuario de destino e depois monta a mensagem a ser gravada e exibida
					mensagemRecebida = leitorEntrada.readLine();
					usuarioDestino = leitorEntrada.readLine();
					socket.close();

					mensagemRecebida = usuarioDestino + "[" +
						new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) + "]: " +
						mensagemRecebida;

					// Faz solicitacao de acao aos gerenciadores
					solicitarAcaoGerenciadorReplica(Constantes.ID_ACAO_SALVAR_LOG, mensagemRecebida);

					// Espera a chegada da resposta do gerenciador de replica
					socket = aServerSocket.accept();
					leitorEntrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					// Caso obtenha sucesso na requisicao, envia a mensagem para o usuario de destino
					if (leitorEntrada.readLine().equals(Constantes.ID_SUCESSO)) {
						socket.close();

						socket = new Socket((String) aUsuariosLogados.get(usuarioDestino), Constantes.PORT_NUMBER_CLIENTE);
						transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
						transmissorDadosSaida.writeBytes(Constantes.ID_MENSAGEM + '\n');
						transmissorDadosSaida.writeBytes(mensagemRecebida);
						socket.close();
					}
				}
			} catch (IOException e) {
				System.out.println("Nao e possivel se comunicar com o Gerenciador de Replica!");
			}
		}
	}

	/**
	 * - Faz o tratamento de solicitacoes para dois gerenciadores de replicas
	 *
	 * @param pAcao
	 * @param pMensagem
	 *
	 * @throws IOException
	 */
	private void solicitarAcaoGerenciadorReplica(String pAcao, String pMensagem)
		throws IOException {
		Socket socket;
		DataOutputStream transmissorDadosSaida;
		boolean inPrimeiroGerenciadorInativo = false;

		if (pAcao.equals(Constantes.ID_ACAO_RESGATAR_LOG)) {
			try {
				// Cria um socket para solicitar ao primeiro gerenciador de replica a ultima mensagem do log
				socket = new Socket(Constantes.ADDRESS_GERENCIADOR_1, Constantes.PORT_NUMBER_GERENCIADOR);
				transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
				transmissorDadosSaida.writeBytes(Constantes.ID_ACAO_RESGATAR_LOG);
				socket.close();
			} catch (IOException e) {
				// Caso a conexao entre o front end e o primeiro gerenciador tenha falhado, tenta-se com o segundo
				socket = new Socket(Constantes.ADDRESS_GERENCIADOR_2, Constantes.PORT_NUMBER_GERENCIADOR);
				transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
				transmissorDadosSaida.writeBytes(Constantes.ID_ACAO_RESGATAR_LOG);
				socket.close();
			}
		} else if (pAcao.equals(Constantes.ID_ACAO_SALVAR_LOG)) {
			try {
				// Cria um socket para solicitar ao gerenciador de replica o salvamento da mensagem enviada
				socket = new Socket(Constantes.ADDRESS_GERENCIADOR_1, Constantes.PORT_NUMBER_GERENCIADOR);
				transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
				transmissorDadosSaida.writeBytes(Constantes.ID_ACAO_SALVAR_LOG + '\n');
				transmissorDadosSaida.writeBytes(pMensagem);
				socket.close();
			} catch (IOException e) {
				System.out.println("O Gerenciador de Replica numero 1 nao se encontra ativo");
				inPrimeiroGerenciadorInativo = true;
			}

			try {
				// Cria um socket para solicitar ao gerenciador de replica o salvamento da mensagem enviada
				socket = new Socket(Constantes.ADDRESS_GERENCIADOR_2, Constantes.PORT_NUMBER_GERENCIADOR);
				transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
				transmissorDadosSaida.writeBytes(Constantes.ID_ACAO_SALVAR_LOG + '\n');
				transmissorDadosSaida.writeBytes(pMensagem);
				socket.close();
			} catch (IOException e) {
				System.out.println("O Gerenciador de Replica numero 2 nao se encontra ativo");

				if (inPrimeiroGerenciadorInativo) {
					throw e;
				}
			}
		}
	}

	/**
	 * - Executa a aplicacao front end do Chat
	 *
	 * @param args
	 *
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		FrontEnd construtor = new FrontEnd(new ServerSocket(Constantes.PORT_NUMBER_FRONT_END));
		construtor.start();
	}
}

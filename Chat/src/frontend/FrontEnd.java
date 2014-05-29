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
import java.util.Iterator;

import util.Constantes;
import util.Usuario;

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
			String acaoRequisitada = "";
			String nmNovoUsuarioLogado = "";

			try {
				// Espera chegada de solicitacao de acao proveniente do Cliente
				socket = aServerSocket.accept();
				leitorEntrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				acaoRequisitada = leitorEntrada.readLine();

				if (acaoRequisitada.equals(Constantes.ID_ACAO_LOGIN)) {
					// Guarda o endereco do cliente para inclui-lo na tabela de usuarios logados
					enderecoRemetente = socket.getInetAddress();
					nmNovoUsuarioLogado = leitorEntrada.readLine();
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
						aUsuariosLogados.put(enderecoRemetente.getHostAddress(),
							new Usuario(enderecoRemetente.getHostAddress(), nmNovoUsuarioLogado));

						try {
							// Atualiza a lista de usuarios logados de todos os conectados
							this.atualizarUsuariosLogados();
						} catch (IOException e) {
							socket = new Socket(enderecoRemetente, Constantes.PORT_NUMBER_CLIENTE);
							transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
							transmissorDadosSaida.writeBytes(Constantes.ID_FALHA);
							socket.close();
						}

						socket = new Socket(enderecoRemetente, Constantes.PORT_NUMBER_CLIENTE);
						transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
						transmissorDadosSaida.writeBytes(Constantes.ID_SUCESSO + '\n');
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

					mensagemRecebida = "[" + ((Usuario) aUsuariosLogados.get(usuarioDestino)).getNome() + " " +
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

						socket = new Socket(usuarioDestino, Constantes.PORT_NUMBER_CLIENTE);
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
	 * Atualiza o combo box de todos os usuarios logados sempre que ha um log in ou log out
	 *
	 * @throws IOException
	 */
	private void atualizarUsuariosLogados() throws IOException {
		Iterator it;
		String usuariosLogados = this.getUsuariosLogados();

		it = this.aUsuariosLogados.values().iterator();

		while (it.hasNext()) {
			Usuario usuario = (Usuario) it.next();

			// Cria um socket para enviar a nova lista de usuarios logados para todos os usuarios logados
			Socket socket = new Socket(usuario.getEndereco(), Constantes.PORT_NUMBER_CLIENTE);
			DataOutputStream transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
			transmissorDadosSaida.writeBytes(Constantes.ID_ACAO_ATUALIZAR_USUARIOS + '\n');
			transmissorDadosSaida.writeBytes(usuariosLogados);
			socket.close();
		}
	}

	/**
	 * Retorna uma String contendo os nomes de usuarios atrelados aos seus respectivos enderecos
	 *
	 * @return
	 */
	private String getUsuariosLogados() {
		String retorno = "";
		String conector = "";
		Iterator it;
		it = this.aUsuariosLogados.values().iterator();

		while (it.hasNext()) {
			Usuario usuario = (Usuario) it.next();
			retorno = retorno + conector + usuario.getNome() + "=" + usuario.getEndereco();
			conector = ", ";
		}

		return retorno;
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

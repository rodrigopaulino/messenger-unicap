/*
 * Este arquivo é propriedade de Rodrigo Paulino Ferreira de Souza.
 * Nenhuma informação nele contida pode ser reproduzida,
 * mostrada ou revelada sem permissão escrita do mesmo.
 */
package gerenciadorreplicacao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import util.Constantes;

/**
 * Classe que implementa o servidor gerenciador de replicas da aplicacao
 *
 * @author rodrigopaulino
 */
public class GerenciadorReplicacao extends Thread {
	//~ Atributos de instancia -----------------------------------------------------------------------------------------------------

	private ServerSocket aServerSocket;

	//~ Construtores ---------------------------------------------------------------------------------------------------------------

/**
         * Cria um novo objeto GerenciadorReplicacao.
         *
         * @param pServerSocket  
         */
	GerenciadorReplicacao(ServerSocket pServerSocket) {
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
			String mensagemRecebida = "";
			String mensagemRespostaAcao = "";
			String parecerAcao = "";
			String acaoRequisitada;

			try {
				// Espera chegada de solicitacao de acao proveniente do Front End
				socket = aServerSocket.accept();
				leitorEntrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				acaoRequisitada = leitorEntrada.readLine();

				if (acaoRequisitada.equals(Constantes.ID_ACAO_SALVAR_LOG)) {
					mensagemRecebida = leitorEntrada.readLine();

					// Realiza a escrita no log de mensagens
					PrintWriter escritorArquivo = new PrintWriter(new BufferedWriter(new FileWriter("log.TXT", true)));
					escritorArquivo.println(mensagemRecebida);
					escritorArquivo.close();

					parecerAcao = (Constantes.ID_SUCESSO);
				} else if (acaoRequisitada.equals(Constantes.ID_ACAO_RESGATAR_LOG)) {
					String ultimaLinhaLog = "";

					// Realiza a leitura do log de mensagens
					BufferedReader leitorArquivo = new BufferedReader(new FileReader("log.TXT"));

					while ((ultimaLinhaLog = leitorArquivo.readLine()) != null) {
						mensagemRespostaAcao = ultimaLinhaLog;
					}
					leitorArquivo.close();

					parecerAcao = (Constantes.ID_SUCESSO);
				} else {
					parecerAcao = (Constantes.ID_FALHA);
					System.out.println("Acao nao identificada!");
				}
				socket.close();

				// Cria um socket para responder ao front end sobre a acao realizada
				socket = new Socket(Constantes.ADDRESS_FRONT_END, Constantes.PORT_NUMBER_FRONT_END);
				transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
				transmissorDadosSaida.writeBytes(parecerAcao + '\n');
				transmissorDadosSaida.writeBytes(mensagemRespostaAcao);
				socket.close();
			} catch (IOException e) {
				try {
					// Cria um socket para informar ao front end sobre o erro ocorrido
					socket = new Socket(Constantes.ADDRESS_FRONT_END, Constantes.PORT_NUMBER_FRONT_END);
					transmissorDadosSaida = new DataOutputStream(socket.getOutputStream());
					transmissorDadosSaida.writeBytes(Constantes.ID_FALHA);
					socket.close();

					System.out.println("A acao solicitada nao foi atendida!");
				} catch (IOException e1) {
					System.out.println("Nao foi possivel se comunicar com o Front End!");
				}
			}
		}
	}

	/**
	 * - Executa a aplicacao gerenciadora de replicas do Chat
	 *
	 * @param args
	 *
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		GerenciadorReplicacao construtor = new GerenciadorReplicacao(new ServerSocket(Constantes.PORT_NUMBER_GERENCIADOR));
		construtor.start();
	}
}

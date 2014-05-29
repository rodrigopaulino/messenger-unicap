/*
 * Este arquivo é propriedade de Rodrigo Paulino Ferreira de Souza.
 * Nenhuma informação nele contida pode ser reproduzida,
 * mostrada ou revelada sem permissão escrita do mesmo.
 */
package util;

/**
 * Classe do tipo Usuario do chat
 *
 * @author rodrigopaulino
 */
public class Usuario {
	//~ Atributos de instancia -----------------------------------------------------------------------------------------------------

	private String aEndereco;
	private String aNome;

	//~ Construtores ---------------------------------------------------------------------------------------------------------------

/**
         * Cria um novo objeto Usuario.
         *
         * @param pEndereco  
         * @param pNome  
         */
	public Usuario(String pEndereco, String pNome) {
		this.aEndereco = pEndereco;
		this.aNome = pNome;
	}

	//~ Metodos --------------------------------------------------------------------------------------------------------------------

	/**
	 * -
	 *
	 * @return
	 */
	public String getEndereco() {
		return this.aEndereco;
	}

	/**
	 * -
	 *
	 * @return
	 */
	public String getNome() {
		return this.aNome;
	}

	/**
	 * -
	 *
	 * @return
	 */
	public String toString() {
		return this.getNome();
	}
}

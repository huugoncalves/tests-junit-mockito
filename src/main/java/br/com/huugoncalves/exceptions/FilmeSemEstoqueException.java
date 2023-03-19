package br.com.huugoncalves.exceptions;

public class FilmeSemEstoqueException extends Exception {

	private static final long serialVersionUID = -805720739397148542L;

	public FilmeSemEstoqueException(String message) {
		super(message);
	}

}

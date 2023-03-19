package br.com.huugoncalves.services;

import br.com.huugoncalves.exceptions.NaoPodeDividirPorZeroException;

public class Calculadora {

	public int somar(int a, int b) {
		return a + b;
	}

	public int subtrai(int a, int b) {
		return a - b;
	}

	public int divide(int a, int b) throws NaoPodeDividirPorZeroException {
		if (0 == b) {
			throw new NaoPodeDividirPorZeroException();
		}
		return a / b;
	}

}

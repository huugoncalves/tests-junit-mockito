package br.com.huugoncalves.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.com.huugoncalves.exceptions.NaoPodeDividirPorZeroException;

public class CalculadoraTest {

	private Calculadora calc;

	@Before
	public void setup() {
		this.calc = new Calculadora();
	}

	@Test
	public void deveSomarDoisValores() {
		int a = 5;
		int b = 3;

		Assert.assertEquals(8, calc.somar(a, b));
	}

	@Test
	public void deveSubtrairDoisValores() {
		int a = 5;
		int b = 3;

		Assert.assertEquals(2, calc.subtrai(a, b));
	}

	@Test
	public void deveDividirDoisValores() throws NaoPodeDividirPorZeroException {
		int a = 6;
		int b = 3;

		Assert.assertEquals(2, calc.divide(a, b));
	}

	@Test(expected = NaoPodeDividirPorZeroException.class)
	public void deveLancarExcecaoAoDividirPorZero() throws NaoPodeDividirPorZeroException {
		int a = 10;
		int b = 0;

		Assert.assertEquals(2, calc.divide(a, b));
	}

}

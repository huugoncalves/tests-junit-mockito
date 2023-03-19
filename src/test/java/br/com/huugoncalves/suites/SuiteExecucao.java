package br.com.huugoncalves.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.com.huugoncalves.services.CalculadoraTest;
import br.com.huugoncalves.services.CalculoValorLocacaoTest;
import br.com.huugoncalves.services.LocacaoServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ CalculadoraTest.class, CalculoValorLocacaoTest.class, LocacaoServiceTest.class })
public class SuiteExecucao {

}

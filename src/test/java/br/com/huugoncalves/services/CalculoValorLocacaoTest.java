package br.com.huugoncalves.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.huugoncalves.builders.UsuarioBuilder;
import br.com.huugoncalves.daos.LocacaoDAO;
import br.com.huugoncalves.entidades.Filme;
import br.com.huugoncalves.entidades.Locacao;
import br.com.huugoncalves.entidades.Usuario;
import br.com.huugoncalves.exceptions.FilmeSemEstoqueException;
import br.com.huugoncalves.exceptions.LocadoraException;

@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {

	@InjectMocks
	private LocacaoService service;

	@Mock
	private SPCService spc;
	@Mock
	private LocacaoDAO dao;

	@Parameter
	public List<Filme> filmes;
	@Parameter(value = 1)
	public Double valorLocacao;
	@Parameter(value = 2)
	public String cenario;

	private static Filme filme1 = new Filme("Filme 1", 2, 4.0);
	private static Filme filme2 = new Filme("Filme 2", 2, 4.0);
	private static Filme filme3 = new Filme("Filme 3", 2, 4.0);
	private static Filme filme4 = new Filme("Filme 4", 2, 4.0);
	private static Filme filme5 = new Filme("Filme 5", 2, 4.0);
	private static Filme filme6 = new Filme("Filme 6", 2, 4.0);
	private static Filme filme7 = new Filme("Filme 7", 2, 4.0);

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Parameters(name = "{2}")
	public static Collection<Object[]> getParametros() {
		return Arrays.asList(new Object[][] { { Arrays.asList(filme1, filme2), 8.0, "2 Filmes: Sem desconto" },
				{ Arrays.asList(filme1, filme2, filme3), 11.0, "3 Filmes: 25%" },
				{ Arrays.asList(filme1, filme2, filme3, filme4), 13.0, "4 Filmes: 50%" },
				{ Arrays.asList(filme1, filme2, filme3, filme4, filme5), 14.0, "5 Filmes: 75%" },
				{ Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6), 14.0, "6 Filmes: 100%" },
				{ Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6, filme7), 18.0,
						"7 Filmes: Sem desconto" } });
	}

	@Test
	public void deveCalcularValorLocacaoConsiderandoDescontos() throws LocadoraException, FilmeSemEstoqueException {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();

		Locacao resultado = service.alugarFilme(usuario, filmes);

		assertThat(resultado.getValor(), is(valorLocacao));
	}

}

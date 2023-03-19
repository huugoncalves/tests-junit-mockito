package br.com.huugoncalves.services;

import static br.com.huugoncalves.matchers.MatchersProprios.caiNumaSegunda;
import static br.com.huugoncalves.matchers.MatchersProprios.ehHojeComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import br.com.huugoncalves.builders.FilmeBuilder;
import br.com.huugoncalves.builders.UsuarioBuilder;
import br.com.huugoncalves.entidades.Filme;
import br.com.huugoncalves.entidades.Locacao;
import br.com.huugoncalves.entidades.Usuario;
import br.com.huugoncalves.exceptions.FilmeSemEstoqueException;
import br.com.huugoncalves.exceptions.LocadoraException;
import br.com.huugoncalves.matchers.MatchersProprios;
import br.com.huugoncalves.utils.DataUtils;

public class LocacaoServiceTest {

	private LocacaoService service;
	private Usuario usuario;

	@Rule
	public ErrorCollector err = new ErrorCollector();

	@Before
	public void setup() {
		service = new LocacaoService();
		usuario = UsuarioBuilder.umUsuario().agora();
	}

	@Test
	public void deveAlugarFilme() throws Exception {
		assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 1, 4.0));
		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		// verificacao
		err.checkThat(locacao.getValor(), is(equalTo(8.00)));
		err.checkThat(locacao.getValor(), is(not(6.00)));

		err.checkThat(locacao.getDataRetorno(), MatchersProprios.ehHoje());
		err.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));

	}

	@Test(expected = FilmeSemEstoqueException.class)
	public void naoDeveAlugarFilmeSemEstoque() throws Exception {
		// cenario
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 1, 4.0),
				new Filme("Filme 3", 1, 4.0));
		for (Filme filme : filmes) {
			filme.setEstoque(0);
		}
		// acao
		service.alugarFilme(usuario, filmes);
	}

	@Test
	public void naoDeveAlugarFilmeSemEstoque_2() {
		// cenario
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 1, 4.0),
				new Filme("Filme 3", 1, 4.0));
		for (Filme filme : filmes) {
			filme.setEstoque(0);
		}
		// acao
		try {
			service.alugarFilme(usuario, filmes);
			fail("Deveria ter lançado uma exceção!");
		} catch (Exception e) {
			assertThat(e.getMessage(), is("Filme sem estoque!"));
		}
	}

	@Test
	public void naoDeveAlugarFilmeSemEstoque_3() {
		// cenario
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 1, 4.0),
				new Filme("Filme 3", 1, 4.0));
		for (Filme filme : filmes) {
			filme.setEstoque(0);
		}
		// acao
		Exception exception = assertThrows(Exception.class, () -> service.alugarFilme(usuario, filmes));
		assertThat(exception.getMessage(), is("Filme sem estoque!"));
	}

	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 1, 4.0),
				new Filme("Filme 3", 1, 4.0));
		// acao
		try {
			service.alugarFilme(null, filmes);
			fail("Deveria ter lançado uma exceção!");
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuario vazio!"));
		}
	}

	@Test
	public void naoDeveAlugarSemFilme() throws LocadoraException, FilmeSemEstoqueException {
		// cenaro
		this.setup();

		// acao
		Exception exception = assertThrows(LocadoraException.class, () -> service.alugarFilme(usuario, null));
		assertThat(exception.getMessage(), is("Filme vazio!"));
	}

	@Test
	public void deveDevolverNaSegundaAoAlugarAosSabados() throws LocadoraException, FilmeSemEstoqueException {
		assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		this.setup();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

		Locacao retorno = service.alugarFilme(usuario, filmes);

		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}

}

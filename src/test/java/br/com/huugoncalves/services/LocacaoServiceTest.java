package br.com.huugoncalves.services;

import static br.com.huugoncalves.matchers.MatchersProprios.caiNumaSegunda;
import static br.com.huugoncalves.matchers.MatchersProprios.ehHojeComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.com.huugoncalves.builders.FilmeBuilder;
import br.com.huugoncalves.builders.LocacaoBuilder;
import br.com.huugoncalves.builders.UsuarioBuilder;
import br.com.huugoncalves.daos.LocacaoDAO;
import br.com.huugoncalves.entidades.Filme;
import br.com.huugoncalves.entidades.Locacao;
import br.com.huugoncalves.entidades.Usuario;
import br.com.huugoncalves.exceptions.FilmeSemEstoqueException;
import br.com.huugoncalves.exceptions.LocadoraException;
import br.com.huugoncalves.matchers.MatchersProprios;
import br.com.huugoncalves.utils.DataUtils;

public class LocacaoServiceTest {

	@InjectMocks
	private LocacaoService service;

	@Mock
	private LocacaoDAO dao;
	@Mock
	private SPCService spc;
	@Mock
	private EmailService email;

	@Rule
	public ErrorCollector err = new ErrorCollector();

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void deveAlugarFilme() throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 1, 4.0));
		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		// verificacao
		err.checkThat(locacao.getValor(), is(equalTo(8.00)));
		err.checkThat(locacao.getValor(), is(not(6.00)));

		err.checkThat(locacao.getDataLocacao(), MatchersProprios.ehHoje());
		err.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));

	}

	@Test(expected = FilmeSemEstoqueException.class)
	public void naoDeveAlugarFilmeSemEstoque() throws Exception {
		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 1, 4.0),
				new Filme("Filme 3", 1, 4.0));
		for (Filme filme : filmes) {
			filme.setEstoque(0);
		}
		// acao
		service.alugarFilme(usuario, filmes);
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
		Usuario usuario = UsuarioBuilder.umUsuario().agora();

		// acao
		Exception exception = assertThrows(LocadoraException.class, () -> service.alugarFilme(usuario, null));
		assertThat(exception.getMessage(), is("Filme vazio!"));
	}

	@Test
	public void deveDevolverNaSegundaAoAlugarAosSabados() throws Exception {
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

		Locacao retorno = service.alugarFilme(usuario, filmes);

		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}

	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

		Mockito.when(spc.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

		try {
			service.alugarFilme(usuario, filmes);
			Assert.fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuario negativado!"));
		}

		Mockito.verify(spc).possuiNegativacao(usuario);
	}

	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		Usuario usuarioEmDia = UsuarioBuilder.umUsuario().comNome("Usuario em dia").agora();
		Usuario outroUsuarioAtrasado = UsuarioBuilder.umUsuario().comNome("Outro usuario atrasado").agora();
		List<Locacao> locacoes = Arrays.asList(LocacaoBuilder.umLocacao().comUsuario(usuario).atrasado().agora(),
				LocacaoBuilder.umLocacao().comUsuario(outroUsuarioAtrasado).atrasado().agora(),
				LocacaoBuilder.umLocacao().comUsuario(outroUsuarioAtrasado).atrasado().agora(),
				LocacaoBuilder.umLocacao().comUsuario(usuarioEmDia).agora());

		Mockito.when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

		// acao
		service.notificarAtrasos();

		// verificacao
		Mockito.verify(email, Mockito.times(3)).notificarAtraso(Mockito.any(Usuario.class));
		Mockito.verify(email).notificarAtraso(usuario);
		Mockito.verify(email, Mockito.never()).notificarAtraso(usuarioEmDia);
		Mockito.verify(email, Mockito.atLeastOnce()).notificarAtraso(outroUsuarioAtrasado);
		Mockito.verifyNoMoreInteractions(email);
	}

	@Test
	public void deveTratarErroSPC() throws Exception {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

		Mockito.when(spc.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrófica!"));

		Exception exception = assertThrows(LocadoraException.class, () -> service.alugarFilme(usuario, filmes));
		assertThat(exception.getMessage(), is("Problemas com o SPC, tente novamente!"));
	}

	@Test
	public void deveProrrogarUmaLocacao() {
		Locacao locacao = LocacaoBuilder.umLocacao().agora();
		Integer dias = 3;
		service.prorrogarLocacao(locacao, dias);

		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
		Mockito.verify(dao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();

		err.checkThat(locacaoRetornada.getValor(), is(locacao.getValor() * dias));
		err.checkThat(locacaoRetornada.getDataLocacao(), MatchersProprios.ehHoje());
		err.checkThat(locacaoRetornada.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(dias));
	}

}

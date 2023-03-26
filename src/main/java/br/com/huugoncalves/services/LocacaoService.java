package br.com.huugoncalves.services;

import static br.com.huugoncalves.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.huugoncalves.daos.LocacaoDAO;
import br.com.huugoncalves.entidades.Filme;
import br.com.huugoncalves.entidades.Locacao;
import br.com.huugoncalves.entidades.Usuario;
import br.com.huugoncalves.exceptions.FilmeSemEstoqueException;
import br.com.huugoncalves.exceptions.LocadoraException;
import br.com.huugoncalves.utils.DataUtils;

public class LocacaoService {

	private LocacaoDAO dao;
	private SPCService spcService;
	private EmailService emailService;

	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws LocadoraException, FilmeSemEstoqueException {
		if (usuario == null) {
			throw new LocadoraException("Usuario vazio!");
		}
		if (filmes == null || filmes.isEmpty()) {
			throw new LocadoraException("Filme vazio!");
		}
		Locacao locacao = new Locacao();
		Double precoLocacao = 0d;
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		for (int i = 0; i < filmes.size(); i++) {
			Filme filme = filmes.get(i);
			if (filme.getEstoque() < 1) {
				throw new FilmeSemEstoqueException("Filme sem estoque!");
			}
			Double valorFilme = filme.getPrecoLocacao();
			switch (i) {
				case 2:
					valorFilme = valorFilme * 0.75;
					break;
				case 3:
					valorFilme = valorFilme * 0.5;
					break;
				case 4:
					valorFilme = valorFilme * 0.25;
					break;
				case 5:
					valorFilme = 0d;
			}

			precoLocacao += valorFilme;
		}

		boolean negativado;
		try {
			negativado = spcService.possuiNegativacao(usuario);
		} catch (Exception e) {
			throw new LocadoraException("Problemas com o SPC, tente novamente!");
		}

		if (negativado) {
			throw new LocadoraException("Usuario negativado!");
		}

		locacao.setFilmes(filmes);
		locacao.setValor(precoLocacao);

		// Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);

		// Salvando a locacao...
		dao.salvar(locacao);

		return locacao;
	}

	public void notificarAtrasos() {
		List<Locacao> locacoes = dao.obterLocacoesPendentes();
		for (Locacao locacao : locacoes) {
			if (locacao.getDataRetorno().before(new Date())) {
				emailService.notificarAtraso(locacao.getUsuario());
			}
		}
	}

	public void prorrogarLocacao(Locacao locacao, Integer dias) {
		Locacao novaLocacao = new Locacao(locacao.getUsuario(), locacao.getFilmes(), new Date(),
				DataUtils.obterDataComDiferencaDias(dias), locacao.getValor() * dias);
		dao.salvar(novaLocacao);
	}

}
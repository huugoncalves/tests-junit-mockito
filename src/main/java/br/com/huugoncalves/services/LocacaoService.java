package br.com.huugoncalves.services;

import static br.com.huugoncalves.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.huugoncalves.entidades.Filme;
import br.com.huugoncalves.entidades.Locacao;
import br.com.huugoncalves.entidades.Usuario;
import br.com.huugoncalves.exceptions.FilmeSemEstoqueException;
import br.com.huugoncalves.exceptions.LocadoraException;
import br.com.huugoncalves.utils.DataUtils;

public class LocacaoService {

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
		// TODO adicionar método para salvar

		return locacao;
	}

}
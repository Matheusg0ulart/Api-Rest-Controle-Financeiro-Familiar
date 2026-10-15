package br.senac.fianceiro.relatorio.service;

import br.senac.fianceiro.conta.entity.ContaFinanceira;
import br.senac.fianceiro.conta.repository.ContaFinanceiraRepository;
import br.senac.fianceiro.relatorio.dto.DespesaPorCategoriaResponse;
import br.senac.fianceiro.relatorio.dto.EvolucaoMensalResponse;
import br.senac.fianceiro.relatorio.dto.ResumoFinanceiroResponse;
import br.senac.fianceiro.transacao.entity.StatusTransacao;
import br.senac.fianceiro.transacao.entity.TipoTransacao;
import br.senac.fianceiro.transacao.entity.Transacao;
import br.senac.fianceiro.transacao.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RelatorioService {

    private final ContaFinanceiraRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public RelatorioService(ContaFinanceiraRepository contaRepository, TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroResponse obterResumo(Long usuarioId, LocalDate inicio, LocalDate fim) {
        List<ContaFinanceira> contas = contaRepository.findContasAcessiveis(usuarioId);
        List<Long> contasIds = contas.stream().map(ContaFinanceira::getId).toList();

        BigDecimal saldoTotalContas = contas.stream()
                .map(ContaFinanceira::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (contasIds.isEmpty()) {
            return ResumoFinanceiroResponse.builder()
                    .totalReceitas(BigDecimal.ZERO)
                    .totalDespesas(BigDecimal.ZERO)
                    .saldoLiquido(BigDecimal.ZERO)
                    .saldoTotalContas(BigDecimal.ZERO)
                    .quantidadeTransacoes(0L)
                    .quantidadeContas(0L)
                    .build();
        }

        List<Transacao> transacoes = transacaoRepository.findComFiltros(contasIds, inicio, fim, null, null, null);

        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        long qtd = 0;

        for (Transacao t : transacoes) {
            if (t.getStatus() == StatusTransacao.CONFIRMADA) {
                if (t.getTipo() == TipoTransacao.RECEITA) {
                    totalReceitas = totalReceitas.add(t.getValor());
                    qtd++;
                } else if (t.getTipo() == TipoTransacao.DESPESA) {
                    totalDespesas = totalDespesas.add(t.getValor());
                    qtd++;
                }
            }
        }

        return ResumoFinanceiroResponse.builder()
                .totalReceitas(totalReceitas)
                .totalDespesas(totalDespesas)
                .saldoLiquido(totalReceitas.subtract(totalDespesas))
                .saldoTotalContas(saldoTotalContas)
                .quantidadeTransacoes(qtd)
                .quantidadeContas((long) contas.size())
                .build();
    }

    @Transactional(readOnly = true)
    public List<DespesaPorCategoriaResponse> obterDespesasPorCategoria(Long usuarioId, LocalDate inicio, LocalDate fim) {
        List<ContaFinanceira> contas = contaRepository.findContasAcessiveis(usuarioId);
        List<Long> contasIds = contas.stream().map(ContaFinanceira::getId).toList();

        if (contasIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Transacao> despesas = transacaoRepository.findComFiltros(contasIds, inicio, fim, TipoTransacao.DESPESA, null, null);

        Map<Long, String> nomeCategorias = new HashMap<>();
        Map<Long, BigDecimal> totais = new HashMap<>();
        Map<Long, Long> contadores = new HashMap<>();
        BigDecimal totalGeral = BigDecimal.ZERO;

        for (Transacao t : despesas) {
            if (t.getStatus() == StatusTransacao.CONFIRMADA) {
                Long catId = t.getCategoria() != null ? t.getCategoria().getId() : 0L;
                String catNome = t.getCategoria() != null ? t.getCategoria().getNome() : "Sem Categoria";

                nomeCategorias.put(catId, catNome);
                totais.put(catId, totais.getOrDefault(catId, BigDecimal.ZERO).add(t.getValor()));
                contadores.put(catId, contadores.getOrDefault(catId, 0L) + 1);
                totalGeral = totalGeral.add(t.getValor());
            }
        }

        List<DespesaPorCategoriaResponse> resultado = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : totais.entrySet()) {
            BigDecimal perc = BigDecimal.ZERO;
            if (totalGeral.compareTo(BigDecimal.ZERO) > 0) {
                perc = entry.getValue().multiply(BigDecimal.valueOf(100)).divide(totalGeral, 2, RoundingMode.HALF_UP);
            }
            resultado.add(DespesaPorCategoriaResponse.builder()
                    .categoriaId(entry.getKey() == 0L ? null : entry.getKey())
                    .categoriaNome(nomeCategorias.get(entry.getKey()))
                    .total(entry.getValue())
                    .porcentagem(perc)
                    .quantidade(contadores.get(entry.getKey()))
                    .build());
        }

        resultado.sort((a, b) -> b.getTotal().compareTo(a.getTotal()));
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<EvolucaoMensalResponse> obterEvolucaoMensal(Long usuarioId, Integer mesesAtras) {
        if (mesesAtras == null || mesesAtras <= 0) {
            mesesAtras = 6;
        }

        List<ContaFinanceira> contas = contaRepository.findContasAcessiveis(usuarioId);
        List<Long> contasIds = contas.stream().map(ContaFinanceira::getId).toList();

        YearMonth atual = YearMonth.now();
        YearMonth inicioYm = atual.minusMonths(mesesAtras - 1);
        LocalDate inicioData = inicioYm.atDay(1);
        LocalDate fimData = atual.atEndOfMonth();

        List<Transacao> transacoes = Collections.emptyList();
        if (!contasIds.isEmpty()) {
            transacoes = transacaoRepository.findComFiltros(contasIds, inicioData, fimData, null, null, null);
        }

        DateTimeFormatter fmtRotulo = DateTimeFormatter.ofPattern("MMM/yy", Locale.forLanguageTag("pt-BR"));
        List<EvolucaoMensalResponse> meses = new ArrayList<>();

        for (int i = 0; i < mesesAtras; i++) {
            YearMonth ym = inicioYm.plusMonths(i);
            String mesAno = ym.toString();
            String rotulo = ym.format(fmtRotulo);

            BigDecimal receitas = BigDecimal.ZERO;
            BigDecimal despesas = BigDecimal.ZERO;

            for (Transacao t : transacoes) {
                if (t.getStatus() == StatusTransacao.CONFIRMADA && t.getDataCompetencia() != null) {
                    YearMonth tYm = YearMonth.from(t.getDataCompetencia());
                    if (tYm.equals(ym)) {
                        if (t.getTipo() == TipoTransacao.RECEITA) {
                            receitas = receitas.add(t.getValor());
                        } else if (t.getTipo() == TipoTransacao.DESPESA) {
                            despesas = despesas.add(t.getValor());
                        }
                    }
                }
            }

            meses.add(EvolucaoMensalResponse.builder()
                    .mesAno(mesAno)
                    .rotulo(rotulo)
                    .receitas(receitas)
                    .despesas(despesas)
                    .saldo(receitas.subtract(despesas))
                    .build());
        }

        return meses;
    }
}

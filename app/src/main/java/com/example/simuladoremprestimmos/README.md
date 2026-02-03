# Simulador de Crédito Pessoal

Este projeto consiste num simulador de crédito pessoal desenvolvido em Android, utilizando Jetpack Compose e o padrão MVVM.

## Objetivo
Permitir ao utilizador simular um crédito pessoal, calculando:
- Prestação mensal
- Total pago
- Total de juros

A simulação é efetuada com base no montante pretendido e no prazo escolhido.

## Domínio do Problema
O simulador representa um crédito pessoal standard, caracterizado por:
- Empréstimo sem garantia real
- Taxa fixa durante toda a duração do empréstimo
- Prazos típicos até 84 meses
- Montantes habituais entre 500€ e 75 000€

Valores fora destes intervalos não são considerados, por deixarem de se enquadrar no domínio do crédito pessoal.

## Cálculo da Taxa
Na fase de simulação, a taxa anual é estimada com base no montante e no prazo escolhidos.
Esta taxa representa a taxa fixa inicial que, após definida, é aplicada durante toda a duração do empréstimo.

A taxa é calculada a partir de:
- Uma taxa base (6%)
- Um ajuste em função do montante
- Um ajuste em função do prazo

Este modelo é simplificado e tem como objetivo reproduzir o comportamento geral dos simuladores bancários, não substituindo a análise real efetuada pelas instituições financeiras.

## Arquitetura
O projeto segue uma separação clara de responsabilidades:

- UI (Jetpack Compose):
  Responsável apenas pela apresentação dos dados e interação com o utilizador.

- ViewModel:
  Gere o estado da aplicação, valida os dados introduzidos, aplica as regras do domínio do problema e coordena o cálculo da simulação.

- Domínio:
  Contém a lógica de cálculo financeiro do empréstimo, incluindo o cálculo da prestação, total pago e juros.

Esta organização facilita a manutenção, a evolução e a compreensão do projeto.

## Notas
Apesar de procurar aproximar-se da realidade, o simulador tem fins académicos e não representa uma proposta contratual real.

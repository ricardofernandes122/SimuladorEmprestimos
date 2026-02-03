# Simulador de Crédito Pessoal

Este projeto consiste num simulador de crédito pessoal desenvolvido em Android,
utilizando Jetpack Compose e o padrão de arquitetura MVVM.

## Objetivo
O objetivo do projeto é permitir ao utilizador simular um crédito pessoal,
aplicando conceitos de programação avançada, nomeadamente separação de responsabilidades,
gestão de estado e testes unitários.

A simulação permite calcular:
- Prestação mensal
- Total pago
- Total de juros

com base no montante pretendido e no prazo escolhido.

## Domínio do Problema
O simulador representa um crédito pessoal standard, caracterizado por:
- Empréstimo sem garantia real
- Taxa de juro fixa durante toda a duração do empréstimo
- Prazos típicos até 84 meses
- Montantes habituais entre 500€ e 75 000€

Valores fora destes intervalos não são considerados, por deixarem de se enquadrar
no domínio do crédito pessoal.

## Cálculo da Taxa
Na fase de simulação, a taxa anual é estimada com base no montante e no prazo escolhidos.
Após ser determinada, essa taxa é considerada fixa durante toda a duração do empréstimo.

A taxa é calculada a partir de:
- Uma taxa base (6%)
- Um ajuste em função do montante
- Um ajuste em função do prazo

Este modelo é simplificado e tem como objetivo reproduzir o comportamento geral
dos simuladores bancários, não substituindo a análise real efetuada pelas instituições financeiras.

## Arquitetura
O projeto segue uma separação clara de responsabilidades, de acordo com o padrão MVVM:

- **UI (Jetpack Compose)**  
  Responsável apenas pela apresentação dos dados e interação com o utilizador.

- **ViewModel**  
  Gere o estado da aplicação, valida os dados introduzidos, aplica as regras do domínio
  do problema e coordena o cálculo da simulação.

- **Domínio**  
  Contém a lógica de cálculo financeiro do empréstimo, incluindo o cálculo da prestação,
  total pago e total de juros, de forma independente da interface.

Esta organização facilita a manutenção, a testabilidade e a evolução do projeto.

## Testes Unitários
A lógica de cálculo do empréstimo é validada através de testes unitários,
implementados com JUnit.

Os testes cobrem:
- Casos normais de utilização
- Situações limite (valores mínimos e máximos do domínio)
- Casos inválidos (valores negativos ou nulos)
- Comportamento do cálculo face à variação da taxa e do prazo

Os testes seguem a estrutura **AAA (Arrange, Act, Assert)**,
garantindo clareza e fiabilidade na validação da lógica de negócio.

## Notas
Apesar de procurar aproximar-se da realidade, este simulador tem fins exclusivamente
académicos e não representa uma proposta contratual real.

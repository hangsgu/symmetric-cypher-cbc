# Teoria da Informação - Cifrador simétrico de bloco 

## Descrição
Esse projeto é um trabalho da faculdade em que consiste na criação de um cifrador simétrico de bloco que suporte consegue cifrar e desifrar arquivos. 

## Tecnologias
Optamos por utilizar Java pela ampla facilidade em acessar o nível de bits. Além disso, como já havíamos utilizado essa tecnologia no outro trabalho tivemos mais facilidade na implementação desse. 

## Instalação
Requisitos para iniciar: 
 - Java 8 
 - Maven

### Clonando o repositório
Para clonar o repositório deve rodar o seguinte comando:
`git clone https://github.com/hangsgu/symmetric-cypher-cbc`

### Como rodar
Primeiramente tem que fazer o build do projeto com: `mvn clean install`.

Para rodar a aplicação, tem que utilizar esse comando na home do projeto onde se deve substituir os valores de 
 `<input_file>`, `<action>` com os valores respectivos no path:
`mvn exec:java -Dexec.mainClass=com.unisinos.teoria.informacao.App -Dexec.args="<input_file> <key>"`

Valor para `<input_file>`:
 - O path do seu arquivo em relação a pasta principal do projeto
 - O programa irá encriptar e decriptar o arquivo fornecido, haverá 2 arquivos novos gerados: encrypted.txt e decrypted.txt

Valor para `<key>`:
 - Valor da chave para encriptação. Tamanho: 4

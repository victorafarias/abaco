# Skill de Revisão de Código para Agentes de IA - Documentação Completa

## 📋 Visão Geral

Este é um **skill completo e robusto** para Claude revisar código criado por outros agentes de IA (ou por ele mesmo). O skill implementa um processo sistemático de revisão de código em 5 fases, com checklists específicos por linguagem, análise estática automatizada e geração de relatórios estruturados.

## 🎯 Objetivo

Fornecer revisões de código **profissionais, sistemáticas e acionáveis** para implementações criadas por agentes de IA, identificando:
- Bugs e erros lógicos
- Vulnerabilidades de segurança
- Problemas de performance
- Código de baixa qualidade
- Padrões inadequados
- Problemas específicos de código gerado por IA

## 📁 Estrutura do Skill

```
ai-code-reviewer-skill/
│
├── SKILL.md                          ← Prompt principal do skill
│   └── Contém as 5 fases de revisão e todas as instruções
│
├── references/                       ← Documentação de referência
│   ├── general-checklist.md         ← Checklist universal para qualquer linguagem
│   ├── python-checklist.md          ← Checklist específico Python
│   ├── javascript-checklist.md      ← Checklist específico JavaScript/TypeScript
│   ├── java-checklist.md            ← Checklist específico Java
│   ├── security-patterns.md         ← Padrões e anti-padrões de segurança
│   └── report-template.md           ← Template do relatório de revisão
│
├── scripts/                          ← Scripts de automação
│   ├── static_analyzer.py           ← Análise estática automatizada
│   ├── generate_report.py           ← Geração de relatórios
│   ├── test_runner.py               ← Execução de testes
│   └── compare_implementations.py   ← Comparação entre implementações
│
└── USAGE_EXAMPLES.md                 ← Exemplos de uso do skill
```

## 🔄 Processo de Revisão (5 Fases)

### **Fase 1: Intake & Context**
- Coleta código, requisitos e contexto
- Identifica linguagem e frameworks
- Carrega checklist apropriado

### **Fase 2: Static Analysis**
- Executa análise automatizada (pylint, eslint, bandit, etc.)
- Identifica problemas de sintaxe, estilo e segurança básica
- Gera métricas de código

### **Fase 3: Deep Review**
Revisão manual em **7 dimensões**:
1. **Correctness & Logic** - Lógica correta, edge cases
2. **Code Quality** - Estrutura, legibilidade, manutenibilidade
3. **Security** - Vulnerabilidades, validação de input
4. **Performance** - Eficiência algorítmica, otimizações
5. **Error Handling** - Tratamento de exceções, resiliência
6. **Testing** - Testabilidade, cobertura
7. **AI-Specific** - Problemas típicos de código gerado por IA

### **Fase 4: Testing Validation**
- Executa testes existentes
- Identifica testes ausentes
- Valida edge cases manualmente

### **Fase 5: Report Generation**
- Gera relatório estruturado e profissional
- Prioriza issues (Critical → Major → Minor)
- Fornece exemplos de código corrigido
- Define status de aprovação

## 🚀 Como Usar

### Uso Básico

Simplesmente peça ao Claude para revisar código gerado por IA:

```
"Revise este código Python que outro agente criou para autenticação de usuários"
```

Claude automaticamente:
1. Detecta que deve usar o skill ai-code-reviewer
2. Lê o SKILL.md e carrega o checklist Python
3. Executa análise estática
4. Conduz revisão manual
5. Gera relatório completo

### Uso com Foco Específico

```
"Revise este JavaScript com foco em segurança"
"Analise este código e execute os testes também"
"Compare estas duas implementações do mesmo algoritmo"
```

### Informações Úteis para Fornecer

- **Linguagem** (se não for óbvia no código)
- **Propósito** (o que o código deve fazer)
- **Contexto** (quem criou, para que sistema)
- **Criticidade** (produção vs protótipo)

## ✨ Diferenciais do Skill

### 1. **Especialização em Código de IA**
Detecta problemas típicos de código gerado por IA:
- APIs ou bibliotecas alucinadas
- Padrões desatualizados
- Overengineering
- Mistura de versões incompatíveis
- Copy-paste de exemplos não adaptados

### 2. **Checklists Específicos por Linguagem**
- Python: PEP 8, type hints, geradores, asyncio
- JavaScript: ES6+, React, Node.js, TypeScript
- Java: OOP, Spring Boot, JPA, concorrência
- Geral: Aplicável a qualquer linguagem

### 3. **Análise de Segurança Dedicada**
- Padrões vulneráveis vs. seguros
- Exemplos de código corrigido
- OWASP Top 10
- Validação de input, SQL injection, XSS, etc.

### 4. **Automação Inteligente**
Scripts Python prontos para:
- Análise estática com múltiplas ferramentas
- Execução de testes (pytest, jest, unittest)
- Geração de relatórios estruturados
- Comparação de implementações

### 5. **Relatórios Profissionais**
Saída estruturada com:
- Executive summary
- Issues priorizados por severidade
- Exemplos de código (problema + solução)
- Métricas quantitativas
- Status de aprovação claro
- Recomendações acionáveis

## 📊 Exemplo de Relatório

```markdown
# Code Review Report

**Language:** Python | **Files:** 3 | **Lines:** 456

## Executive Summary
Identified 2 critical security issues and 7 code quality concerns.
Core functionality is correct but requires security fixes before deployment.

## Critical Issues 🚨

### 1. SQL Injection Vulnerability
**Location:** `auth.py:42`

❌ **Current:**
```python
query = f"SELECT * FROM users WHERE id = {user_id}"
```

✅ **Fix:**
```python
query = "SELECT * FROM users WHERE id = ?"
cursor.execute(query, (user_id,))
```

## Approval Status
- [x] Requires Revision - Fix critical issues first
```

## 🛠️ Scripts Incluídos

### `static_analyzer.py`
Análise estática automatizada com suporte para:
- Python: pylint, bandit
- JavaScript: eslint
- Análise genérica para outras linguagens

**Uso:**
```bash
python scripts/static_analyzer.py <code_dir> --language python --output report.json
```

### `generate_report.py`
Gera relatório consolidado em Markdown

**Uso:**
```bash
python scripts/generate_report.py \
  --static-analysis analysis.json \
  --manual-review notes.md \
  --output final_report.md
```

### `test_runner.py`
Executa testes e gera sumário

**Uso:**
```bash
python scripts/test_runner.py <test_dir> --framework pytest
```

## 🎓 Casos de Uso

### 1. **Revisão de Pull Request de IA**
Revisar código submetido por agentes autônomos antes de merge

### 2. **Validação de Código Gerado**
Verificar outputs de ferramentas como GitHub Copilot, ChatGPT Code

### 3. **Auditoria de Segurança**
Análise focada em vulnerabilidades antes de produção

### 4. **Code Review de Pares (AI-AI)**
Um agente revisa trabalho de outro agente

### 5. **Iteração e Melhoria**
Feedback para agentes melhorarem implementações

## 📝 Checklist de Implementação

O skill foi implementado seguindo as melhores práticas:

✅ **SKILL.md conciso** (<500 linhas)
✅ **Frontmatter YAML completo** (name + description)
✅ **Description acionável** (quando usar o skill)
✅ **Documentação progressiva** (SKILL.md → references → scripts)
✅ **Scripts testáveis** (executáveis standalone)
✅ **Checklists por linguagem**
✅ **Template de relatório profissional**
✅ **Exemplos de uso**
✅ **Padrões de segurança**
✅ **Sem arquivos desnecessários** (no README.md extra)

## 🔧 Personalização

O skill pode ser facilmente estendido:

1. **Adicionar nova linguagem:**
   - Criar `references/linguagem-checklist.md`
   - Referenciar no SKILL.md

2. **Adicionar padrões da empresa:**
   - Expandir `references/security-patterns.md`
   - Adicionar regras específicas aos checklists

3. **Integrar ferramentas internas:**
   - Modificar `scripts/static_analyzer.py`
   - Adicionar novos scripts em `scripts/`

## 📚 Referências Utilizadas

O skill foi construído com base em:
- OWASP Top 10 (segurança)
- PEP 8 (Python style guide)
- Airbnb JavaScript Style Guide
- Oracle Java Conventions
- Clean Code (Robert Martin)
- Skill Creator best practices

## 💡 Benefícios

**Para Desenvolvedores:**
- Revisões consistentes e completas
- Aprendizado de boas práticas
- Identificação de bugs antes de produção
- Documentação de problemas

**Para Times:**
- Padronização de qualidade
- Redução de dívida técnica
- Segurança melhorada
- Velocidade mantida com qualidade

**Para Sistemas de IA:**
- Feedback estruturado para melhoria
- Detecção de padrões problemáticos
- Validação de outputs automatizados
- Loop de aprendizado

## 🎯 Próximos Passos

Para usar este skill:

1. **Teste com código real** - Peça ao Claude para revisar código
2. **Ajuste para seu contexto** - Adicione padrões específicos da sua empresa
3. **Integre no workflow** - Use em CI/CD ou revisões de PR
4. **Colete feedback** - Melhore os checklists baseado em uso real

## 📧 Estrutura de Triggers

O skill é ativado quando o usuário:
- Menciona "revisar código de IA/agente"
- Pede "análise de código gerado automaticamente"
- Solicita "validação de implementação de IA"
- Usa termos como "code review", "peer review", "audit" com contexto de IA

## 🏆 Qualidade do Prompt

Este skill foi desenvolvido com:
- **Clareza**: Instruções não ambíguas
- **Estrutura**: Processo em 5 fases bem definido
- **Completude**: Checklists abrangentes
- **Acionabilidade**: Exemplos concretos de fixes
- **Extensibilidade**: Fácil adicionar linguagens/padrões
- **Profissionalismo**: Output de qualidade enterprise

---

**Versão:** 1.0  
**Criado:** 2025-01-29  
**Compatibilidade:** Claude com computer use (Linux)  
**Licença:** Uso livre para revisão de código

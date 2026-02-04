# AI Code Reviewer - Exemplo de Uso

Este documento demonstra como usar o skill de revisão de código para IA.

## Cenário 1: Revisão Básica

**Solicitação do Usuário:**
"Revise este código Python que um agente de IA criou para mim."

**Como o Skill Funciona:**

1. **Claude lê o SKILL.md** para entender o processo de revisão
2. **Carrega o checklist apropriado** (`references/python-checklist.md`)
3. **Executa análise estática** usando `static_analyzer.py`
4. **Conduz revisão manual** seguindo as 7 dimensões
5. **Gera relatório estruturado** usando `generate_report.py`

## Cenário 2: Revisão com Testes

**Solicitação do Usuário:**
"Revise este código JavaScript e execute os testes também."

**Fluxo:**

1. Análise estática com ESLint
2. Revisão manual do código
3. Execução dos testes com `test_runner.py --framework jest`
4. Geração de relatório completo incluindo resultados dos testes

## Cenário 3: Foco em Segurança

**Solicitação do Usuário:**
"Este código vai lidar com dados de usuários. Por favor, faça uma revisão focada em segurança."

**Processo:**

1. Claude consulta `references/security-patterns.md`
2. Executa análise com ferramentas de segurança (Bandit para Python)
3. Verifica padrões vulneráveis manualmente
4. Gera relatório destacando issues de segurança

## Exemplo de Output

```markdown
# Code Review Report

**Review Date:** 2025-01-29
**Language:** Python

## Executive Summary

Automated analysis identified **12 code quality issues** and **3 security concerns**.
The implementation successfully handles the core requirements but requires 
attention to input validation and error handling before production deployment.

## Critical Issues 🚨

### 1. SQL Injection Vulnerability
**Location:** `auth.py:42`
**Severity:** Critical

**Problem:**
Direct string interpolation in SQL query allows SQL injection.

```python
# Current (vulnerable)
query = f"SELECT * FROM users WHERE username = '{username}'"
```

**Fix:**
```python
# Recommended
query = "SELECT * FROM users WHERE username = ?"
cursor.execute(query, (username,))
```

### 2. Hardcoded API Key
**Location:** `config.py:15`
**Severity:** Critical

API key is hardcoded in source code. Move to environment variables.

## Major Concerns ⚠️

1. **Missing error handling** in async operations (api.py:67-89)
2. **Function too long** - `process_data()` is 127 lines (utils.py:45)
3. **No input validation** on user-facing endpoints

## Positive Observations ✅

- ✅ Excellent use of type hints throughout
- ✅ Clear function naming conventions
- ✅ Good separation of concerns
- ✅ Comprehensive docstrings

## Recommendations

### Immediate Actions
1. Fix SQL injection vulnerability (Critical)
2. Move API key to environment variable (Critical)
3. Add error handling to async operations (Major)

## Approval Status

- [ ] Approved
- [ ] Approved with minor changes
- [x] **Requires Revision** - Critical security issues must be addressed
- [ ] Requires major refactoring
```

## Dicas para Melhores Resultados

### Ao Solicitar Revisão

**Bom:**
- "Revise este código que gerei para autenticação de usuários"
- "Um agente criou este código. Verifique se há problemas de segurança"
- "Analise este JavaScript gerado por IA e me dê feedback"

**Menos Efetivo:**
- "Isso está bom?" (muito vago)
- "Revise" (sem contexto do que foi implementado)

### Informações Úteis para Fornecer

1. **Linguagem de programação** (se não for óbvio)
2. **Propósito do código** (o que deve fazer)
3. **Restrições ou requisitos** especiais
4. **Nível de criticidade** (produção vs. protótipo)

## Comparação: Antes vs. Depois

### Antes (Sem o Skill)

```
Usuário: "Revise este código"

Claude: "O código parece bom. Algumas sugestões:
- Considere adicionar tratamento de erros
- Os nomes das variáveis poderiam ser mais descritivos
- Talvez adicionar alguns comentários"
```

**Problemas:**
- Feedback genérico e superficial
- Sem estrutura
- Perde issues críticos
- Sem checklist sistemático

### Depois (Com o Skill)

```
Usuário: "Revise este código"

Claude: 
1. Carrega checklist Python
2. Executa análise estática
3. Conduz revisão em 7 dimensões
4. Gera relatório estruturado com:
   - Issues críticos com exemplos de código
   - Análise de segurança
   - Métricas de qualidade
   - Recomendações priorizadas
   - Status de aprovação
```

**Benefícios:**
- Revisão sistemática e completa
- Identifica issues críticos
- Feedback acionável com exemplos
- Relatório profissional
- Considera padrões específicos da linguagem

## Casos de Uso Avançados

### 1. Revisão Iterativa

```
Usuário: "Revise meu código"
Claude: [Gera relatório com 5 issues críticos]

Usuário: "Corrigi os issues críticos, revise novamente"
Claude: [Nova revisão focando nas mudanças]
```

### 2. Comparação de Implementações

```
Usuário: "Dois agentes implementaram a mesma funcionalidade. 
Qual está melhor?"

Claude: [Usa scripts/compare_implementations.py para análise lado-a-lado]
```

### 3. Auditoria de Segurança

```
Usuário: "Este código vai para produção com dados sensíveis. 
Faça uma auditoria de segurança completa."

Claude: [Foco especial em security-patterns.md e ferramentas de segurança]
```

## Estrutura de Arquivos do Skill

```
ai-code-reviewer-skill/
├── SKILL.md                              # Instruções principais
├── references/                           # Documentação de referência
│   ├── general-checklist.md             # Checklist universal
│   ├── python-checklist.md              # Específico Python
│   ├── javascript-checklist.md          # Específico JS/TS
│   ├── java-checklist.md                # Específico Java
│   ├── security-patterns.md             # Padrões de segurança
│   └── report-template.md               # Template de relatório
└── scripts/                              # Ferramentas de automação
    ├── static_analyzer.py               # Análise estática
    ├── generate_report.py               # Geração de relatórios
    ├── test_runner.py                   # Execução de testes
    └── compare_implementations.py       # Comparação de código
```

## Personalizações Possíveis

O skill pode ser estendido com:

1. **Novos checklists** para outras linguagens (Go, Rust, etc.)
2. **Padrões específicos** da sua empresa
3. **Scripts personalizados** para seu workflow
4. **Integrações** com ferramentas internas

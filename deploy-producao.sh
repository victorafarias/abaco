#!/usr/bin/env bash
set -euo pipefail

REGISTRY="basis-registry.basis.com.br/abaco"
SERVICES=("frontend" "backend")

# Retorna a tag master-N mais recente (ordenação numérica)
get_latest_master_tag() {
  crane ls "${REGISTRY}/$1" 2>/dev/null \
    | grep -E '^master[-_][0-9]+$' \
    | awk -F'[-_]' '{print $2, $0}' | sort -k1 -n | awk '{print $2}' | tail -1
}

# Retorna o maior N entre tags producao-N
get_latest_producao_number() {
  crane ls "${REGISTRY}/$1" 2>/dev/null \
    | grep -E '^producao[-_][0-9]+$' \
    | grep -oE '[0-9]+$' | sort -n | tail -1
}

declare -a COPIES=()

for service in "${SERVICES[@]}"; do
  master_tag="$(get_latest_master_tag "${service}")"
  if [[ -z "${master_tag}" ]]; then
    echo "${service}: nenhuma tag master encontrada"
    continue
  fi

  latest_n="$(get_latest_producao_number "${service}")"
  next_n=$(( ${latest_n:-0} + 1 ))
  sep=$(echo "${master_tag}" | grep -oP '(?<=master)[-_]')
  new_tag="producao${sep}${next_n}"

  echo "${service}: ${master_tag} -> ${new_tag}"
  COPIES+=("${REGISTRY}/${service}:${master_tag}|${REGISTRY}/${service}:${new_tag}")
done

[[ ${#COPIES[@]} -eq 0 ]] && exit 0

echo ""
read -r -p "Confirma o deploy? [y/yes para continuar] " answer
if [[ ! "${answer}" =~ ^[Yy]([Ee][Ss])?$ ]]; then
  echo "Deploy cancelado."
  exit 0
fi

echo ""
for entry in "${COPIES[@]}"; do
  src="${entry%%|*}"
  dst="${entry##*|}"
  echo "Copiando ${src} -> ${dst}"
  crane copy "${src}" "${dst}"
done
echo "Deploy concluído."

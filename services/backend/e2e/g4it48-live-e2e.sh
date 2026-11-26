#!/usr/bin/env bash
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

# G4IT-48 live-stack E2E check. Account values are never printed.
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="${G4IT_E2E_ENV_FILE:-$DIR/../../../workspace/docker/.env}"

load_test_account() {
  local load_user=false load_password=false
  [[ -n "${G4IT_E2E_USER:-}" ]] || load_user=true
  [[ -n "${G4IT_E2E_PASSWORD:-}" ]] || load_password=true
  if [[ "$load_user" == false && "$load_password" == false ]]; then
    return
  fi
  if [[ ! -f "$ENV_FILE" || ! -r "$ENV_FILE" ]]; then
    echo "Cannot read E2E account configuration: $ENV_FILE" >&2
    exit 2
  fi

  local line key value number=0
  local assignment='^[[:space:]]*(export[[:space:]]+)?(G4IT_E2E_USER|G4IT_E2E_PASSWORD)[[:space:]]*=(.*)$'
  local double_quoted='^"([^"]*)"[[:space:]]*(#.*)?$'
  local single_quoted="^'([^']*)'[[:space:]]*(#.*)?$"
  # Read only the account keys as literal data; never source the configuration.
  while IFS= read -r line || [[ -n "$line" ]]; do
    number=$((number + 1))
    line="${line%$'\r'}"
    [[ "$line" =~ $assignment ]] || continue
    key="${BASH_REMATCH[2]}"
    value="${BASH_REMATCH[3]}"
    value="${value#"${value%%[![:space:]]*}"}"
    case "$value" in
      \"*|\'*)
        if [[ "$value" =~ $double_quoted || "$value" =~ $single_quoted ]]; then
          value="${BASH_REMATCH[1]}"
        else
          echo "Invalid quoted value for $key at configuration line $number" >&2
          exit 2
        fi
        ;;
      \#*) value="" ;;
      *)
        value="${value%%[[:space:]]#*}"
        value="${value%"${value##*[![:space:]]}"}"
        ;;
    esac
    case "$key" in
      G4IT_E2E_USER)
        if [[ "$load_user" == true ]]; then G4IT_E2E_USER="$value"; fi
        ;;
      G4IT_E2E_PASSWORD)
        if [[ "$load_password" == true ]]; then G4IT_E2E_PASSWORD="$value"; fi
        ;;
    esac
  done < "$ENV_FILE"
}

load_test_account
: "${G4IT_E2E_USER:?Set G4IT_E2E_USER in the account configuration or environment}"
: "${G4IT_E2E_PASSWORD:?Set G4IT_E2E_PASSWORD in the account configuration or environment}"

KC="${KC:-http://localhost:8180/auth/realms/g4it}"
API="${API:-http://localhost:8080}"
CSV="${CSV:-$DIR/g4it48-physical-equipment.csv}"
WORK="${WORK:-$DIR/work}"
C=(curl --noproxy '*' -sS)
mkdir -p "$WORK"

auth() {
  TOKEN=$("${C[@]}" -d grant_type=password -d client_id=g4it \
    --data-urlencode "username=$G4IT_E2E_USER" --data-urlencode "password=$G4IT_E2E_PASSWORD" \
    "$KC/protocol/openid-connect/token" | jq -r '.access_token // empty')
  [ -n "$TOKEN" ] || { echo "TOKEN: direct grant failed"; exit 2; }
  A=(-H "Authorization: Bearer $TOKEN")
}
auth
echo "TOKEN: obtained (redacted)"

"${C[@]}" "${A[@]}" "$API/users/me" > "$WORK/me.json"
ORG="${ORG:-$(jq -r '[.organizations[] | select(any(.workspaces[]?; any(.roles[]?; .=="ROLE_INVENTORY_WRITE")) or any(.roles[]?; .=="ROLE_ORGANIZATION_ADMINISTRATOR"))][0].name' "$WORK/me.json")}"
WS="${WS:-$(jq -r --arg o "$ORG" '.organizations[] | select(.name==$o) | .workspaces[0].id' "$WORK/me.json")}"
echo "ORG=$ORG WS=$WS"
B="$API/organizations/$ORG/workspaces/$WS/inventories"

NAME="G4IT48-E2E-$(date +%Y%m%d%H%M%S)"
INV=$("${C[@]}" "${A[@]}" -H 'Content-Type: application/json' -d "{\"name\":\"$NAME\",\"type\":\"INFORMATION_SYSTEM\"}" "$B" | jq -r '.id')
echo "INVENTORY $NAME id=$INV"
[[ "$INV" =~ ^[0-9]+$ ]] || exit 3
cleanup() {
  auth
  code=$("${C[@]}" -o /dev/null -w '%{http_code}' -X DELETE "${A[@]}" "$B/$INV")
  echo "DELETE inventory -> $code"
  echo "REMAINING with name: $("${C[@]}" "${A[@]}" "$B" | jq --arg n "$NAME" '[.[] | select(.name==$n)] | length')"
}
trap cleanup EXIT

wait_task() {
  local st=""
  [[ "$1" =~ ^[0-9]+$ ]] || { echo "TASK id missing"; return 1; }
  for _ in $(seq 1 90); do
    auth
    st=$("${C[@]}" "${A[@]}" "$B?inventoryId=$INV" | jq -r --arg t "$1" '.[0].tasks[]? | select((.id|tostring)==$t) | .status')
    case "$st" in COMPLETED*|FAILED*|SKIPPED*) echo "TASK $1 status=$st"; return 0;; esac
    sleep 5
  done
  echo "TASK $1 timeout status=$st"; return 1
}

LOAD=$("${C[@]}" "${A[@]}" -H 'Accept-Language: en' -F "EQUIPEMENT_PHYSIQUE=@$CSV" "$B/$INV/load-input-files" | jq -r '.taskId')
echo "LOAD taskId=$LOAD"
wait_task "$LOAD"
"${C[@]}" "${A[@]}" -o "$WORK/reject.zip" -w 'REJECT download -> %{http_code}\n' "$API/organizations/$ORG/workspaces/$WS/download-reject/$LOAD" || true
rm -rf "$WORK/reject" && unzip -oq "$WORK/reject.zip" -d "$WORK/reject" 2>/dev/null && head -20 "$WORK"/reject/* || true
"${C[@]}" "${A[@]}" "$B?inventoryId=$INV" | jq -c '.[0] | {physicalEquipmentCount, tasks: [.tasks[] | {id,type,status,progressPercentage,details}]}'

EVAL=$("${C[@]}" "${A[@]}" -X POST "$B/$INV/evaluating" | jq -r ".taskId // .")
echo "EVAL taskId=$EVAL"
wait_task "$EVAL"

"${C[@]}" "${A[@]}" -H 'Accept: application/zip' -o "$WORK/export.zip" -w 'DOWNLOAD -> %{http_code}\n' "$B/$INV/indicators/export/download"
rm -rf "$WORK/export"
unzip -oq "$WORK/export.zip" -d "$WORK/export"
ls "$WORK/export"
for f in "$WORK"/export/*physical_equipment*.csv; do
  echo "== $(basename "$f")"
  head -1 "$f" | tr ';' '\n' | grep -n -i 'contexte\|fraction' || echo "  (no G4IT-48 column)"
  if [[ "$(basename "$f")" == ind_* ]]; then
    grep -i 'g48-' "$f" | awk -F';' '{print "name="$20" quantity="$13" contexteUsage="$(NF-1)" fractionUsage="$NF}' | sort -u
  else
    grep -i 'g48-' "$f" || true
  fi
done
"${C[@]}" "${A[@]}" -o "$WORK/equipments.json" -w 'EQUIPMENT INDICATORS -> %{http_code}\n' "$B/$INV/indicators/equipments" || true
jq -c "[.. | objects | select(has(\"byodDevicesPercentage\")) | {label, byodDevicesPercentage, copeDevicesPercentage}] | unique" "$WORK/equipments.json"

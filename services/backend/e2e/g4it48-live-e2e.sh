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

# Synthetic Smartphone/electricity factors are required to compare allocated impacts.
# The fixture is idempotent, test-only and never overwrites existing factors.
SEED="${G4IT_E2E_SEED:-1}"
PG_CONTAINER="${PG_CONTAINER:-postgresdb}"
PG_DB="${PG_DB:-postgres}"
PG_USER="${PG_USER:-postgres}"
FIXTURE="${FIXTURE:-$DIR/../src/test/resources/sql/g4it48-local-reference.sql}"
psql_local() {
  docker exec -i -e PGOPTIONS='-c g4it48.local_seed=on' "$PG_CONTAINER" \
    psql -v ON_ERROR_STOP=1 -q -X -U "$PG_USER" -d "$PG_DB" "$@"
}
if [[ "$SEED" == "1" ]]; then
  factors_query="SELECT (SELECT count(*) FROM ref_item_impact WHERE name = 'smartphone-2' AND subscriber IS NULL AND workspace_id IS NULL AND value > 0 AND avg_electricity_consumption > 0) || ':' || (SELECT count(*) FROM ref_item_impact WHERE category = 'electricity-mix' AND location = 'France' AND subscriber IS NULL AND workspace_id IS NULL)"
  before=$(psql_local -tA -c "$factors_query")
  psql_local < "$FIXTURE"
  after=$(psql_local -tA -c "$factors_query")
  echo "REFERENCE factors smartphone:mix before=$before after=$after"
  if [[ "$before" != "$after" ]]; then
    echo "Reference factors were added. The backend caches the referential (g4it.cache.database.ttl, 20 min by default):"
    echo "restart the backend, then run this script again."
    exit 4
  fi
fi

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

FAILURES=0
check() { if [[ "$1" == ok ]]; then echo "PASS $2"; else echo "FAIL $2"; FAILURES=$((FAILURES + 1)); fi; }

for name in g48-phone-bad g48-unknown-byod; do
  if grep -qs "$name" "$WORK"/reject/*; then check ok "$name rejected"; else check ko "$name rejected"; fi
done
SRC="$WORK/export/physical_equipment.csv"
if [[ -f "$SRC" ]] && ! grep -q 'g48-unknown-byod' "$SRC"; then
  check ok "g48-unknown-byod absent from loaded equipment"
else
  check ko "g48-unknown-byod absent from loaded equipment"
fi

# Allocated impact per source unit compared with the unallocated Smartphone, per lifecycle step and criterion.
IND="$(ls "$WORK"/export/ind_physical_equipment*.csv 2>/dev/null | head -1 || true)"
if [[ -z "$IND" ]]; then
  check ko "evaluated physical equipment export present"
else
  for spec in g48-phone-byod:2:0.57 g48-phone-cope:3:0.85 g48-phone-frac:1:0.5; do
    IFS=: read -r name qty expected <<< "$spec"
    result=$(awk -F';' -v n="$name" -v q="$qty" -v e="$expected" '
      $20 == "g48-phone-none" && $8 == "OK" && $12 != "" { base[$5 FS $6] = $12 / 4 }
      $20 == n && $8 == "OK" && $12 != "" { row[$5 FS $6] = $12 / q }
      END {
        compared = 0; bad = 0
        for (k in row) {
          if (!(k in base) || base[k] == 0) continue
          compared++
          r = row[k] / base[k]
          if (r - e > 1e-6 || e - r > 1e-6) { bad++; printf "  %s ratio=%.6f expected=%s\n", k, r, e > "/dev/stderr" }
        }
        printf "%d %d", compared, bad
      }' "$IND")
    read -r compared bad <<< "$result"
    if [[ "$compared" -gt 0 && "$bad" -eq 0 ]]; then
      check ok "$name impact ratio $expected on $compared step/criterion pairs"
    else
      check ko "$name impact ratio $expected (compared=$compared mismatches=$bad)"
    fi
  done
fi

echo "RESULT failures=$FAILURES"
[[ "$FAILURES" -eq 0 ]]

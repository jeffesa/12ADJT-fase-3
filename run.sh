#!/bin/bash
# ═══════════════════════════════════════════════════════════════
# Tech Challenge Fase 3 — Sistema Hospitalar de Agendamento
# Script runner: facilita execução do projeto multi-serviço.
#
# Uso interativo:  ./run.sh
# Uso direto:      ./run.sh [docker|stop|tests|reset-db|kill|health]
# ═══════════════════════════════════════════════════════════════

set -o pipefail

# Portas dos serviços
PORTS=(8081 8082 8083)

# Cores
C_CYAN='\033[0;36m'
C_GREEN='\033[0;32m'
C_RED='\033[0;31m'
C_YELLOW='\033[1;33m'
C_RESET='\033[0m'

info()  { echo -e "${C_CYAN}$1${C_RESET}"; }
ok()    { echo -e "${C_GREEN}$1${C_RESET}"; }
warn()  { echo -e "${C_YELLOW}$1${C_RESET}"; }
err()   { echo -e "${C_RED}$1${C_RESET}"; }

# ─── Validação de pré-requisitos ───────────────────────────────

check_java() {
  if command -v java &>/dev/null; then
    ok "✅ Java encontrado: $(java -version 2>&1 | head -1)"
    return 0
  fi
  warn "⚠️  Java não encontrado (necessário apenas para rodar testes localmente; não é preciso para Docker)."
  return 1
}

check_maven() {
  if command -v mvn &>/dev/null; then
    ok "✅ Maven encontrado: $(mvn -v 2>&1 | head -1)"
    return 0
  fi
  warn "⚠️  Maven não encontrado (necessário apenas para 'tests')."
  return 1
}

# Garante que o daemon Docker está ativo (suporta Docker Desktop, Colima)
ensure_docker() {
  if docker ps &>/dev/null; then
    return 0
  fi
  # macOS com Colima
  if command -v colima &>/dev/null; then
    unset DOCKER_HOST
    docker context use colima &>/dev/null
    if docker ps &>/dev/null; then
      return 0
    fi
    warn "⚠️  Docker não está respondendo. Iniciando Colima..."
    colima start 2>/dev/null
    sleep 5
    if docker ps &>/dev/null; then
      ok "✅ Docker disponível (Colima)."
      return 0
    fi
    err "❌ Não foi possível iniciar o Docker via Colima. Tente: colima stop && colima start"
    return 1
  fi
  err "❌ Docker não está rodando."
  echo "   - macOS: abra o Docker Desktop ou instale o Colima (brew install colima)"
  echo "   - Linux: sudo systemctl start docker"
  return 1
}

# Descobre o comando de compose disponível (docker compose v2 ou docker-compose v1)
compose() {
  if docker compose version &>/dev/null; then
    docker compose "$@"
  elif command -v docker-compose &>/dev/null; then
    docker-compose "$@"
  else
    err "❌ Nem 'docker compose' nem 'docker-compose' encontrados."
    return 1
  fi
}

kill_port() {
  local PORT=$1
  local PID
  PID=$(lsof -ti:"$PORT" 2>/dev/null)
  if [ -n "$PID" ]; then
    warn "⚠️  Porta $PORT em uso (PID: $PID). Encerrando..."
    kill -9 "$PID" 2>/dev/null
    sleep 1
    ok "✅ Porta $PORT liberada."
  else
    echo "   Porta $PORT já está livre."
  fi
}

kill_all_ports() {
  info "🔪 Liberando portas dos serviços (${PORTS[*]})..."
  for p in "${PORTS[@]}"; do kill_port "$p"; done
}

# ─── Ações ─────────────────────────────────────────────────────

run_docker() {
  info "\n🐳 Subindo a aplicação completa via Docker Compose..."
  ensure_docker || return 1
  compose down 2>/dev/null
  compose up --build -d || { err "❌ Falha ao subir os containers."; return 1; }
  echo ""
  ok "✅ Containers iniciados. Aguardando healthchecks..."
  echo ""
  info "📍 Scheduling  — Swagger:  http://localhost:8081/swagger-ui.html"
  info "📍 Scheduling  — Health:   http://localhost:8081/actuator/health"
  info "📍 Notification— Health:   http://localhost:8082/actuator/health"
  info "📍 History     — Health:   http://localhost:8083/actuator/health"
  info "📍 History     — GraphiQL: http://localhost:8083/graphiql"
  info "📍 RabbitMQ    — Console:  http://localhost:15672 (guest/guest)"
  echo ""
  echo "   Acompanhe os logs com: ./run.sh logs"
}

show_logs() {
  ensure_docker || return 1
  compose logs -f
}

stop_docker() {
  info "\n🛑 Parando os containers..."
  ensure_docker || return 1
  compose down
  ok "✅ Containers parados."
}

reset_db() {
  info "\n🗑️  Limpando bancos e volumes (docker-compose down -v)..."
  ensure_docker || return 1
  compose down -v
  ok "✅ Volumes removidos. Suba novamente com: ./run.sh docker"
}

run_tests() {
  check_java || return 1
  check_maven || return 1
  info "\n🧪 Executando testes (mvn clean verify, profile test)..."
  mvn clean verify -Dspring.profiles.active=test
}

health_check() {
  info "\n🩺 Verificando health dos serviços..."
  local names=("scheduling (8081)" "notification (8082)" "history (8083)")
  local urls=("http://localhost:8081/actuator/health" \
              "http://localhost:8082/actuator/health" \
              "http://localhost:8083/actuator/health")
  for i in "${!urls[@]}"; do
    local code
    code=$(curl -s -o /dev/null -w "%{http_code}" "${urls[$i]}" 2>/dev/null)
    if [ "$code" = "200" ]; then
      ok "  ✅ ${names[$i]}: UP ($code)"
    else
      err "  ❌ ${names[$i]}: indisponível ($code)"
    fi
  done
}

# ─── Menu interativo ───────────────────────────────────────────

show_menu() {
  echo ""
  echo -e "${C_CYAN}╔════════════════════════════════════════════════════╗${C_RESET}"
  echo -e "${C_CYAN}║   Tech Challenge Fase 3 — Sistema Hospitalar        ║${C_RESET}"
  echo -e "${C_CYAN}╚════════════════════════════════════════════════════╝${C_RESET}"
  echo "  1) Subir tudo (Docker Compose up --build)"
  echo "  2) Parar containers (Docker Compose down)"
  echo "  3) Rodar testes (mvn clean verify)"
  echo "  4) Limpar bancos (Docker Compose down -v)"
  echo "  5) Liberar portas (8081, 8082, 8083)"
  echo "  6) Health check dos serviços"
  echo "  7) Ver logs (Docker Compose logs -f)"
  echo "  0) Sair"
  echo ""
  read -r -p "Escolha uma opção: " option
  case $option in
    1) run_docker ;;
    2) stop_docker ;;
    3) run_tests ;;
    4) reset_db ;;
    5) kill_all_ports ;;
    6) health_check ;;
    7) show_logs ;;
    0) echo "👋 Até mais!" && exit 0 ;;
    *) err "❌ Opção inválida." && show_menu ;;
  esac
}

# ─── Entry point ───────────────────────────────────────────────

if [ -n "$1" ]; then
  case $1 in
    docker|up)   run_docker ;;
    stop|down)   stop_docker ;;
    tests|test)  run_tests ;;
    reset-db)    reset_db ;;
    kill)        kill_all_ports ;;
    health)      health_check ;;
    logs)        show_logs ;;
    *) echo "Uso: ./run.sh [docker|stop|tests|reset-db|kill|health|logs]" ;;
  esac
else
  show_menu
fi

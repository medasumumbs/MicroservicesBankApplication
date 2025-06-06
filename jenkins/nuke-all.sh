#!/bin/bash
set -e

# Загрузка переменных из .env
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Проверка переменной
if [ -z "$DOCKER_REGISTRY" ]; then
  echo "DOCKER_REGISTRY не задан в .env"
  exit 1
fi

echo "Using DOCKER_REGISTRY: $DOCKER_REGISTRY"

echo "Uninstalling Helm releases..."
for ns in test prod; do
  helm uninstall accounts-service -n "$ns" || true
  helm uninstall antifraud-service -n "$ns" || true
  helm uninstall cash-in-cash-out-service -n "$ns" || true
  helm uninstall currency-exchange-service -n "$ns" || true
  helm uninstall exchange-generator-service -n "$ns" || true
  helm uninstall gateway-service -n "$ns" || true
  helm uninstall notifications-service -n "$ns" || true
  helm uninstall transfer-service -n "$ns" || true
  helm uninstall ui-service -n "$ns" || true
  helm uninstall notifications-db -n "$ns" || true
  helm uninstall keycloak -n "$ns" || true
done

echo "Deleting secrets..."
for ns in test prod; do
  kubectl delete secret myapp-notifications-db -n "$ns" --ignore-not-found
  kubectl delete secret myapp-keycloak -n "$ns" --ignore-not-found
  kubectl delete secret myapp-postgresql -n "$ns" --ignore-not-found
done

echo "Deleting namespaces..."
kubectl delete ns test --ignore-not-found
kubectl delete ns prod --ignore-not-found

echo "Shutting down Jenkins..."
docker compose down -v || true
docker stop jenkins && docker rm jenkins || true
docker volume rm jenkins_home || true

echo "Removing images..."
docker image rm ${DOCKER_REGISTRY}/accounts-service:1 || true
docker image rm ${DOCKER_REGISTRY}/antifraud-service:1 || true
docker image rm ${DOCKER_REGISTRY}/cash-in-cash-out-service:1 || true
docker image rm ${DOCKER_REGISTRY}/currency-exchange-service:1 || true
docker image rm ${DOCKER_REGISTRY}/exchange-generator-service:1 || true
docker image rm ${DOCKER_REGISTRY}/gateway-service:1 || true
docker image rm ${DOCKER_REGISTRY}/notifications-service:1 || true
docker image rm ${DOCKER_REGISTRY}/transfer-service:1 || true
docker image rm ${DOCKER_REGISTRY}/ui-service:1 || true
docker image rm jenkins/jenkins:lts-jdk21 || true

echo "Pruning system..."
docker system prune -af --volumes

echo "Done! All clean."
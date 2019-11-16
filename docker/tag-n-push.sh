#!/usr/bin/env bash

CONTAINER_REGISTRY="docker.io"
CONTAINER_REGISTRY_REPO="no42org"
VERSION=$(../.circleci/pom2version.py ../pom.xml)
OCI_TAGS=(${VERSION})

if [ -n "${CIRCLE_BUILD_NUM}" ]; then
  OCI_TAGS+=("${VERSION}-b${CIRCLE_BUILD_NUM}")
fi

for TAG in ${OCI_TAGS[*]}; do
  docker tag oca-check "${CONTAINER_REGISTRY}/${CONTAINER_REGISTRY_REPO}/oca-check:${TAG}"
  docker push "${CONTAINER_REGISTRY}/${CONTAINER_REGISTRY_REPO}/oca-check:${TAG}"
done

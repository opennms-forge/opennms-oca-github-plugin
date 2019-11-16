#!/usr/bin/env bash

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

BASE_IMAGE="jetty"
BASE_IMAGE_VERSION="9.4.18-jre8"
CONTAINER_REGISTRY="docker.io"
CONTAINER_REGISTRY_REPO="opennms"


if [[ "${CIRCLE_BRANCH}" == "master" ]]; then
  VERSION=$(../circleci/pom2version.py ../pom.xml)
else
  VERSION="bleeding"
fi

OCI_TAGS="${VERSION}"

docker build -t oca-check \
  --build-arg BUILD_DATE="$(date -u +\"%Y-%m-%dT%H:%M:%S%z\")" \
  --build-arg BASE_IMAGE="${BASE_IMAGE}" \
  --build-arg BASE_IMAGE_VERSION="${BASE_IMAGE_VERSION}" \
  --build-arg VERSION="${VERSION}" \
  --build-arg SOURCE="${CIRCLE_REPOSITORY_URL}" \
  --build-arg REVISION="$(git describe --always)" \
  --build-arg BUILD_JOB_ID="${CIRCLE_WORKFLOW_JOB_ID}" \
  --build-arg BUILD_NUMBER="${CIRCLE_BUILD_NUM}" \
  --build-arg BUILD_URL="${CIRCLE_BUILD_URL}" \
  --build-arg BUILD_BRANCH="${CIRCLE_BRANCH}" \
  .

if [ -n "${CIRCLE_BUILD_NUM}" ]; then
  OCI_TAGS+=("${VERSION}-b${CIRCLE_BUILD_NUM}")
fi

for TAG in ${OCI_TAGS[*]}; do
  docker tag oca-check "${CONTAINER_REGISTRY}/${CONTAINER_REGISTRY_REPO}/oca-check:${TAG}"
done

docker image save oca-check -o images/container.oci


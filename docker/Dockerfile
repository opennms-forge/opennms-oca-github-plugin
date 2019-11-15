
ARG BASE_IMAGE="jetty"
ARG BASE_IMAGE_VERSION="9.4.18-jre8"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION}

ENV OCA_CHECK_DEPLOY_DIR /var/lib/jetty/webapps
ENV OCA_PLUGIN_CONFIG_DIR /etc/oca-github-plugin
ENV OCA_PLUGIN_CONFIG ${OCA_PLUGIN_CONFIG_DIR}/oca-github-plugin.properties
ENV OCA_PLUGIN_MAPPING ${OCA_PLUGIN_CONFIG_DIR}/oca-mapping.properties
ENV GITHUB_API_TOKEN myToken
ENV GITHUB_USER myUser
ENV GITHUB_REPOSITORY myRepo
ENV GITHUB_WEBHOOK_SECRET myWebhookSecret
ENV OCA_REGEXP_REDO myRegexpRedo
ENV MAPPING_FILE_LOCATION myMappingFileLocation

USER root

COPY deploy/oca-github-plugin-*.war ${OCA_CHECK_DEPLOY_DIR}/oca-check.war

RUN mkdir ${OCA_PLUGIN_CONFIG_DIR} && \
    chown -R jetty:jetty ${OCA_CHECK_DEPLOY_DIR} ${OCA_PLUGIN_CONFIG_DIR}

COPY container-fs/oca-config/* ${OCA_PLUGIN_CONFIG_DIR}/
COPY container-fs/entrypoint.sh /

ARG VERSION

LABEL org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.title="OCA Check GitHub Plugin ${VERSION}" \
      org.opencontainers.image.source="${SOURCE}" \
      org.opencontainers.image.revision="${REVISION}" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.vendor="The OpenNMS Group, Inc." \
      org.opencontainers.image.authors="OpenNMS Community" \
      org.opencontainers.image.licenses="AGPL-3.0" \
      org.opennms.image.base="${BASE_IMAGE}:${BASE_IMAGE_VERSION}" \
      org.opennme.cicd.jobid="${BUILD_JOB_ID}" \
      org.opennms.cicd.buildnumber="${BUILD_NUMBER}" \
      org.opennms.cicd.buildurl="${BUILD_URL}" \
      org.opennms.cicd.branch="${BUILD_BRANCH}"

USER jetty

ENTRYPOINT [ "/entrypoint.sh" ]

CMD [ "-h" ]

EXPOSE 8080
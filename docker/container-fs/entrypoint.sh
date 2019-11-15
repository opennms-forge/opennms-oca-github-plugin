#!/usr/bin/env bash
# =====================================================================
# Build script running OCA check service in Docker environment
#
# Source: https://github.com/indigo423/docker-oca-check
# Web: https://www.opennms.org
#
# =====================================================================

# Error codes
E_ILLEGAL_ARGS=126

# Help function used in error messages and -h option
usage() {
    echo ""
    echo "Docker entry script for OCA plugin service container"
    echo ""
    echo "-s: Run OCA plugin service."
    echo "-h: Show this help."
    echo ""
}

configure() {
    if [ ! -d "${OCA_PLUGIN_CONFIG_DIR}" ]; then
        echo "OCA Plugin etc directory doesn't exist in ${OCA_PLUGIN_CONFIG_DIR}."
        exit ${E_ILLEGAL_ARGS}
    fi

    sed -i "s,GITHUB_API_TOKEN,${GITHUB_API_TOKEN}," "${OCA_PLUGIN_CONFIG}"
    sed -i "s,GITHUB_USER,${GITHUB_USER}," "${OCA_PLUGIN_CONFIG}"
    sed -i "s,GITHUB_REPOSITORY,${GITHUB_REPOSITORY}," "${OCA_PLUGIN_CONFIG}"
    sed -i "s,GITHUB_WEBHOOK_SECRET,${GITHUB_WEBHOOK_SECRET}," "${OCA_PLUGIN_CONFIG}"
    sed -i "s,OCA_REGEXP_REDO,${OCA_REGEXP_REDO}," "${OCA_PLUGIN_CONFIG}"
    sed -i "s,MAPPING_FILE_LOCATION,${MAPPING_FILE_LOCATION}," "${OCA_PLUGIN_CONFIG}"
}

start () {
    java ${JAVA_OPTS} -Djava.io.tmpdir=/tmp/jetty -jar /usr/local/jetty/start.jar -Dproperty.file="${OCA_PLUGIN_CONFIG}"
}

# Evaluate arguments for build script.
if [[ "${#}" == 0 ]]; then
    usage
    exit ${E_ILLEGAL_ARGS}
fi

# Evaluate arguments for build script.
while getopts fhis flag; do
    case ${flag} in
        h)
            usage
            exit
            ;;
        s)
            configure
            start
            exit
            ;;
        *)
            usage
            exit ${E_ILLEGAL_ARGS}
            ;;
    esac
done

# Strip of all remaining arguments
shift $((OPTIND - 1));

# Check if there are remaining arguments
if [[ "${#}" -gt 0 ]]; then
    echo "Error: To many arguments: ${*}."
    usage
    exit ${E_ILLEGAL_ARGS}
fi
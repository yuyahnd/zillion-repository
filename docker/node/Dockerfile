# Image Setup
ARG NODE_VERSION
FROM node:${NODE_VERSION}

## vue cli
ARG INSTALL_VUE_CLI=false
RUN if [ ${INSTALL_VUE_CLI} = true ]; then \
    npm install -g @vue/cli \
;fi

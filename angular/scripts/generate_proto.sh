#!/usr/bin/env bash

set -e
set -x

SRC_PROTO_FOLDER="../shared/protos"

OUT_FILE=dam-service

OUT_DIR="./src/app/shared/proto"

PBJS_BIN="./node_modules/protobufjs/bin/pbjs"
PBTS_BIN="./node_modules/protobufjs/bin/pbts"

# Clean the output folder before generating protos
rm -rf "${OUT_DIR}"

mkdir -p "${OUT_DIR}"

# For some reason, the maven protoc plugin needs this file, but it causes an error with this compiler
ALL_PROTOS="$(find "${SRC_PROTO_FOLDER}" -name '*.proto' -and -not -name 'http.proto')"

${PBJS_BIN} -t static-module -w commonjs -o "${OUT_DIR}/${OUT_FILE}.js" ${ALL_PROTOS}
${PBTS_BIN} -o "${OUT_DIR}/${OUT_FILE}.d.ts" "${OUT_DIR}/${OUT_FILE}.js"

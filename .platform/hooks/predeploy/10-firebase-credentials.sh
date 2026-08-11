#!/usr/bin/env bash
set -euo pipefail

secret_id="daytodo/firebase-service-account"
aws_region="${AWS_REGION:-ap-northeast-2}"
credential_dir="/opt/daytodo"
credential_path="${credential_dir}/firebase-service-account.json"

install -d -m 0750 -o root -g webapp "${credential_dir}"

temporary_path="$(mktemp "${credential_dir}/firebase-service-account.json.XXXXXX")"
trap 'rm -f "${temporary_path}"' EXIT

aws secretsmanager get-secret-value \
  --secret-id "${secret_id}" \
  --region "${aws_region}" \
  --query SecretString \
  --output text \
  --no-cli-pager > "${temporary_path}"

test -s "${temporary_path}"
chown root:webapp "${temporary_path}"
chmod 0640 "${temporary_path}"
mv -f "${temporary_path}" "${credential_path}"

trap - EXIT

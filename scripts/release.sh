#!/bin/bash

which git || exit 1

function compute_version() {
    git fetch --tags
    local current_version=
    local latest_version=
    local major_version=
    local minor_version=
    local update_version=
    current_version=$(git describe --tags "$(git rev-list --tags --max-count=1)")
    if [ ! -z "${current_version}" ]; then
        declare -a version=($(echo "${current_version}" | tr "." " "))
        major_version=${version[0]}
        minor_version=${version[1]}
        update_version=${version[2]}
        if [ $((${update_version} + 1)) -ge 11 ]; then
            update_version=0
            minor_version=$((${minor_version} + 1))
            if [ ${minor_version} -ge 20 ]; then
                minor_version=0
                major_version=$((${major_version} + 1 ))
            fi
        else
            update_version=$((${update_version} + 1))
        fi
        latest_version="${major_version}.${minor_version}.${update_version}"
    fi

    if [ -z "${latest_version}" ]; then
        echo "Could not calculate the latest version tag"
        exit 1
    fi

    git config user.email "rugging24@gmail.com"
    git config user.name "Olakunle Olaniyi"
    git tag -a "${latest_version}" -m "creating ${latest_version} release tag"
    git push origin tag "${latest_version}"
    echo ${latest_version}
}

function release() {
  local latest_version=
  local password="${1}"
  latest_version=$(git describe --tags "$(git rev-list --tags --max-count=1)")
  ./gradlew build -PpackageVersion="${latest_version}"
  ./gradlew publish -PmvnRepoPassword="${password}" -PpackageVersion="${latest_version}"
  gh api \
      --method POST \
      -H "Accept: application/vnd.github+json" \
      -H "X-GitHub-Api-Version: 2022-11-28" \
      /repos/rugging24/java-testcompose/releases \
      -f tag_name="${latest_version}" \
     -f target_commitish='main' \
     -f name="${latest_version}" \
     -f body="Release ${latest_version}" \
     -F draft=false \
     -F prerelease=false \
     -F generate_release_notes=true
}

PASSWORD="${1}"

compute_version
release "${PASSWORD}"
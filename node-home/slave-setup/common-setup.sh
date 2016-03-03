#!/usr/bin/env bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
ME=$( basename "$0" )
. ${DIR}/env.sh

# This script will be run on both master and nodes (after-copy).

# ==============================================================================
# DO STUFF

# For example:
# clone git reference repositories (see https://www.cloudbees.com/blog/advanced-git-jenkins)
# that will allow faster clone in builds:

# section Creating/Updating reference repositories
# cd ${WORKSPACES}/.ref
# git clone --bare git@your.git.server.com:some/repo.git

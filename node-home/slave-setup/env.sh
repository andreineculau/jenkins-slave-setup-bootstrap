export THIS_REPO_HOME=/path/to/where/this/repo/will/be/cloned
export NODE_SETUP_HOME=${THIS_REPO_HOME}/node-home/slave-setup

# JENKINS_HOME aka ITEM_ROOTDIR, or container's home
# see https://wiki.jenkins-ci.org/display/JENKINS/Jetty
export JENKINS_HOME=/path/to/jenkins/home/as/in/the/jenkins/configuration/page
export WORKSPACES=${JENKINS_HOME}/workspace
export BUILDS=${JENKINS_HOME}/build

function exe() {
    echo "$(pwd)\$ $@"
    eval "$@"
}

function help() {
    for var in $(echo \
                      THIS_REPO_HOME \
                      NODE_SETUP_HOME \
                      JENKINS_HOME \
                      WORKSPACES \
                      BUILDS \
                        | tr ' ' '\n' \
                        | sort
                ) ; do
        eval "echo ${var}=\$${var}"
    done
}

function section() {
    echo ""
    date
    echo -e "$@"
    printf %$(echo -ne "$@" | tail -1 | wc -c)s | tr " " "="
}

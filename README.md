# jenkins-slave-setup-bootstrap

Bootstrap your https://wiki.jenkins-ci.org/display/JENKINS/Slave+Setup+Plugin configuration.

Fork this repo, and clone your fork on the Jenkins master,
in the same folder as ${THIS_REPO_HOME} variable in [env.sh](env.sh).

Configure the plugin:

* pre-launch script: `${THIS_REPO_HOME}/master-before-node-launch`
* prepare script: `${THIS_REPO_HOME}/master-after-node-launch`
* setup files directory: `${THIS_REPO_HOME}/node-home`
* setup script after copy: `./slave-setup/node-after-copy`
* label expressions: whatever label(s) targets the nodes you want to provision; I usually assign nodes the `jenkins-slave-setup` label for clarity/visibility.

When restarting a node targeted by the label expressions, the plugin will

* run the `pre-launch script` on the master,
* launch the node (i.e. startup the node),
* run the `prepare script` on the master (at this time the node is reachable),
* copy `${THIS_REPO_HOME}/node-home/*` to the node's `${JENKINS_HOME}/` (the slave-setup folder in our case)
* run the `setup script after copy` on the node

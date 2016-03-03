# jenkins-slave-setup-bootstrap

Bootstrap your https://wiki.jenkins-ci.org/display/JENKINS/Slave+Setup+Plugin configuration.

Fork this repo, and clone your fork on the Jenkins master,
in the same folder as `${THIS_REPO_HOME}` variable in [env.sh](env.sh).

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

## How to keep nodes up-to-date

Ideally, when you make a change to this repository, your nodes would be provisioned again.
In order to do that, it's enough to soft-relaunch them, but how?

* create a job `soft-relaunch-nodes`
  * with a `NODE_LABELS` string parameter - a regex equivalent to the `label expressions`
  * with a `INCLUDE_OFFLINE_NODES` boolean parameter
  * with a custom workspace set to `${THIS_REPO_HOME}` variable in [env.sh](env.sh)
  * with a GIT SCM pointing to your fork's GIT URL
  * with a build trigger: `Poll SCM`, say every 5 minutes `H/5 * * * *`
  * with a `Execute system Groovy script` like [soft-relaunch.groovy](soft-relaunch.groovy)

The groovy script will

* will wait for a node to be free (not running any jobs)
* mark it as offline
* bring it down
* bring it up again (at which point the plugin will kick in)

Once you have this in place, just commit and push to your fork,
and Jenkins will automatically start provisioning your nodes every 5 minutes or less,
whenever each node becomes free.

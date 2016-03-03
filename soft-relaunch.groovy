import hudson.model.*
import jenkins.model.*
import hudson.slaves.*

def NODE_LABELS = build.buildVariableResolver.resolve("NODE_LABELS").split(",").collect { it -> it.trim() }
def INCLUDE_OFFLINE_NODES = build.buildVariableResolver.resolve("INCLUDE_OFFLINE_NODES").toBoolean()

if (NODE_LABELS.size() == 0) {
  println "NODE_LABELS is not set"
  return 1
}

def nodeQueue = []

Jenkins.instance.nodes.each { node ->
  def labelString = node.getLabelString()
  def labels = labelString.split(" ").collect { it -> it.trim() }
  labels.add(node.name)
  def computer = node.toComputer()

  def match = false
  labels.each { label ->
    NODE_LABELS.each { NODE_LABEL ->
      def matchRE = "^${NODE_LABEL}\$"
      match = match || (label =~ matchRE)
    }
  }
  if (!match) return

  if (node.nodeName == "master") {
    println "\nSKIPPING (master node) ${node.nodeName} with labels: ${labelString}"
    println "matches ${NODE_LABELS}"
    println "="*80
    return
  }

  if (!computer.isLaunchSupported()) {
    println "\nSKIPPING (launch not supported) ${node.nodeName} with labels: ${labelString}"
    println "matches ${NODE_LABELS}"
    println "="*80
    return
  }

  if (computer.isOffline() && !INCLUDE_OFFLINE_NODES) {
    println "\nSKIPPING (node is offline) ${node.nodeName} with labels: ${labelString}"
    println "matches ${NODE_LABELS}"
    println "="*80
    return
  }

  println "\n${node.nodeName} with labels: ${labelString}"
  println "matches ${NODE_LABELS}"
  println "="*80

  nodeQueue.add([node: node, phase: (computer.isOnline() ? "online" : "wait_for_offline")])
}

def cause = OfflineCause.create(Messages._Hudson_NodeBeingRemoved())

while (!Thread.currentThread().isInterrupted() && nodeQueue.size() > 0) {
  def sleepSeconds = 5
  def newNodeQueue = []

  nodeQueue.each { nodeQueueItem ->
    if (Thread.currentThread().isInterrupted()) return 1

    def node = nodeQueueItem.node
    def phase = nodeQueueItem.phase
    def labelString = node.getLabelString()
    def computer = node.toComputer()

    switch (phase) {
      case "online":
        /*println "Suspending node ${node.name}"
        computer.setAcceptingTasks(false)
        phase = "suspended"
        break
      case "suspended":*/
        if (computer.countBusy() == 0) {
          println "Marking node ${node.name} as offline"
          computer.setTemporarilyOffline(true, cause)
          phase = "wait_for_offline"
        }
        break
      case "wait_for_offline":
        println "Waiting for node ${node.name} to become offline"
        computer.waitUntilOffline()
	    println "Disconnecting node ${node.name}"
        computer.disconnect(cause)
        println "Reinstating node ${node.name}"
        computer.setAcceptingTasks(true)
        phase = "disconnected"
        break
      case "disconnected":
	    println "Connecting node ${node.name}"
        computer.connect(false)
        phase = "connected"
        break
      case "connected":
        println "Marking node ${node.name} as online"
        computer.setTemporarilyOffline(false, computer.getOfflineCause())
        phase = "done"
        break
      default:
        println "Unknown phase ${phase}"
        phase = "unknown"
        break
    }

    if (!["done", "unknown"].contains(phase)) {
      newNodeQueue.add([node: node, phase: phase])
    }
  }

  nodeQueue = newNodeQueue
  def nodeQueueNamesAndPhases = nodeQueue.collect({ it -> "${it.node.name} ${it.phase}" }).join("\n")
  if (nodeQueue.size() > 0) {
    println "="*80
    println "Sleeping ${sleepSeconds} seconds before taking care of the queued nodes:"
    println "${nodeQueueNamesAndPhases}"
    println "="*80

    // see http://stackoverflow.com/a/16324615/465684
    try {
      Thread.currentThread().sleep(sleepSeconds * 1000)
    } catch (final Exception e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      return;
    }
  }
}

return 0

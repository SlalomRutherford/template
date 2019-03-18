import groovy.json.JsonOutput
/**
 * Hosts the common utility functions for jenkins pipelines
 */

/**
 * Send slack message to channel given in the config file
 * @param title Title of slack message
 * @param msg   Message
 * @param color Colour code
 */
void sendSlackMessage(String title, String msg, String color) {
  if("${config}" == null || "${config.SLACK_WEBHOOK_URL}" == null) {
    println "No SLACK_WEBHOOK_URL mentioned"
    return
  }
  echo "Sending Slack Message"
  def header = env.JOB_NAME.replace('/', ': ')
  def payload = JsonOutput.toJson([
          attachments: [[
                                fallback: header,
                                text: header,
                                color: color,
                                fields:[[
                                                title: "#${env.BUILD_NUMBER} - ${title}",
                                                value: msg
                                        ]]
                        ]]
  ])
  sh "curl -X POST --data-urlencode \'payload=${payload}\' ${config.SLACK_WEBHOOK_URL}"
  echo "Slack Message Sent"
}

/**
 * Get the CFN stack name
 * @param  env [environment name]
 * @return     [CFN stack name wrt environment name]
 */
String getCFNStackName(String env) {
  return "${env}-nova-regional-offers"
}

/**
 * Deploy application in ECS
 * @param env     [env name]
 * @param version [version number]
 */
void ecsDeploy(String env, String version) {
  def stackName = getCFNStackName(env) + "-service"
  stackName = stackName.substring(0,31)
  ansiColor('xterm') {
    sh """
      #!/bin/bash
      # configure system-wide environment variables and aliases needed for nvm and npm
      source /etc/profile
      export AWS_DEFAULT_REGION=us-east-1
      ecs-service deploy ${stackName} ${version} \
        cfn/templates/service.json cfn/${env}.params.json \
        -e cfn/${env}.env \
        -t cfn/${env}.tags.json \
        -r us-east-1
    """
  }
}

void deleteStack() {
  def stackName = getCFNStackName('sole') + "-service"
  stackName = stackName.substring(0,31)
  sh "aws cloudformation  delete-stack --stack-name ${stackName} --region us-east-1"
}

void deployResourceStack(String env) {
  echo "Deploying Resource Stack ${env}"
  def stackName = "${getCFNStackName(env)}-resources"
  def tags = "--tags Aggregate=${env}-nova-regional-offers Environment=${env} LOB=AMRP Workstream=Offers Team=Things Component=${stackName}"
  def parameterOverrides = paramsFromFile("cfn/${env}.offer.params.json")
  def args = "deploy --stack-name ${stackName} --region us-east-1 --template-file cfn/templates/resources.yaml --parameter-overrides ${parameterOverrides} --capabilities CAPABILITY_IAM --no-fail-on-empty-changeset"
  sh "aws cloudformation ${args} ${tags}"
  sh "aws cloudformation update-termination-protection --enable-termination-protection --stack-name ${stackName} --region us-east-1"
  sh "aws cloudformation detect-stack-drift --stack-name ${stackName} --region us-east-1"
}

void deployConsumerStack(String env, String version, String bucketName = 'dev-lambda-functions-archive') {
  echo "Deploy Lambda Stack ${env}"
  def stackName = "${getCFNStackName(env)}-lambda"
  def tags = "--tags Aggregate=${env}-nova-regional-offers Environment=${env} LOB=AMRP Workstream=Offers Team=Things Component=${stackName}"
  def template = readYaml file: 'cfn/templates/consumer-template.yaml'
  template.Resources.ConsumerLambda.Properties.CodeUri = "./../nova-regional-offers-consumer/build/libs/nova-regional-offers-consumer-${version}.jar"
  writeYaml file:"build/consumer-template-${env}-offer.yaml", data:template
  def parameterOverrides = paramsFromFile("cfn/${env}.offer.params.json")
  def argsPackage = "package --template-file build/consumer-template-${env}-offer.yaml --s3-bucket ${bucketName} --s3-prefix sam/${env}-nova-regional-offers-lambda-${version} --output-template-file build/packaged-consumer-template-${env}-offer.yaml"
  sh "aws cloudformation ${argsPackage}"
  def argsDeploy = "deploy --template-file build/packaged-consumer-template-${env}-offer.yaml --region us-east-1 --stack-name ${stackName} --parameter-overrides ${parameterOverrides} --capabilities CAPABILITY_NAMED_IAM --no-fail-on-empty-changeset"
  sh "aws cloudformation ${argsDeploy} ${tags}"
  sh "aws cloudformation detect-stack-drift --stack-name ${stackName} --region us-east-1"
}

void deployCronStack(String env, String version, String bucketName = 'dev-lambda-functions-archive') {
  echo "Deploy Lambda Stack ${env}"
  def stackName = "${getCFNStackName(env)}-cron-lambda"
  def tags = "--tags Aggregate=${env}-nova-regional-offers Environment=${env} LOB=AMRP Workstream=Offers Team=Things Component=${stackName}"
  def template = readYaml file: 'cfn/templates/cron-template.yaml'
  template.Resources.CronLambda.Properties.CodeUri = "./../nova-regional-offers-consumer/build/libs/nova-regional-offers-consumer-${version}.jar"
  writeYaml file:"build/cron-template-${env}-offer.yaml", data:template
  def parameterOverrides = paramsFromFile("cfn/${env}.offer.params.json")
  def argsPackage = "package --template-file build/cron-template-${env}-offer.yaml --s3-bucket ${bucketName} --s3-prefix sam/${env}-nova-regional-offers-lambda-${version} --output-template-file build/packaged-cron-template-${env}-offer.yaml"
  sh "aws cloudformation ${argsPackage}"
  def argsDeploy = "deploy --template-file build/packaged-cron-template-${env}-offer.yaml --region us-east-1 --stack-name ${stackName} --parameter-overrides ${parameterOverrides} --capabilities CAPABILITY_NAMED_IAM --no-fail-on-empty-changeset"
  sh "aws cloudformation ${argsDeploy} ${tags}"
  sh "aws cloudformation detect-stack-drift --stack-name ${stackName} --region us-east-1"
}

/**
 * Read json file and return as String required by aws cli
 * @param  filename          [Name of the file]
 * @param  ['ParameterKey'   [Key ]
 * @param  'ParameterValue'] [Value]
 * @return                   [String of key values pairs]
 */
def paramsFromFile(String filename, keyPair = ['ParameterKey', 'ParameterValue']) {
  assert keyPair.size() == 2

  def paramsJson = readJSON(file: filename)

  paramsJson.collect { item ->
    keyPair.collect { key ->
      item.get(key)
    }.join('=')
  }.join(' ')

}

/**
 * Get artifact from jfrog
 * @param location location in jfrog
 * @param target target folder
 */
void copyArtifactFromJfrog(String location, String target) {
  rtDownload (
          serverId: "jfrog-artifactory",
          spec:
                  """{
        "files": [{
            "pattern": "libs-release-local/${location}",
            "target": "${target}/"
          }]
      }"""
  )
}

return this

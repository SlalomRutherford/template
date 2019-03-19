//TODO:  Change settings where relevant.
// Jenkins deployment pipeline
pipeline {
    agent none

    // NOTE: manually add the Git Tag parameter with name buildVersion and set the parameter type to Tag
    stages {
        stage('Deploy to the development environment') {
            agent {
                node {
                    label 'general'
                }
            }
            steps {
                checkout(scm: [$class: 'GitSCM', userRemoteConfigs: [[url: 'git@github.com:LoyaltyOne/rtc-rice.git', credentialsId: 'jenkins-ssh-key']], branches: [[name: "refs/tags/${params.buildVersion}"]]], poll: false)

                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'dev-aws-deployer']]) {
                    sh '''
                    #!/bin/bash
                    set -e
                    set +x
                    source /etc/profile
                    ecs-service deploy dev-rice ${buildVersion} env/service.json env/dev.params.json -e env/dev.env -t env/dev.tags.json -r us-east-1
                    '''
                }

                stash 'project'
            }
        }

        stage('Sandbox promotion question') {
            agent none
            steps {
                input(message: 'Do you want to promote this image to the sandbox environment?', submitter: 'OS-AWS-ApolloDeveloper')
            }
        }

        stage('Deploy to the Sandbox environment') {
            agent {
                node {
                    label 'general'
                }
            }
            steps {
                unstash 'project'
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'sandbox-aws-deployer']]) {
                    sh '''
                    #!/bin/bash
                    set -e
                    set +x
                    source /etc/profile
                    ecs-service deploy sandbox-rice ${buildVersion} env/service.json env/sandbox.params.json -e env/sandbox.env -t env/sandbox.tags.json -r us-east-1
                    '''
                }
                stash 'project'
            }
        }

        stage('Production promotion question') {
            agent none
            steps {
                input(message: 'Do you want to promote this image - version ${buildVersion} - to the production environment?', submitter: 'OS-AWS-ApolloDeveloper')
            }
        }

        stage('Deploy to the Production environment') {
            agent {
                node {
                    label 'general'
                }
            }
            steps {
                unstash 'project'
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'prod-aws-deployer']]) {
                    sh '''
                    #!/bin/bash
                    set -e
                    set +x
                    source /etc/profile
                    ecs-service deploy prod-rice ${buildVersion} env/service.json env/prod.params.json -e env/prod.env -t env/prod.tags.json -r us-east-1
                    '''
                }
            }
        }

    }

}

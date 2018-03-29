def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        // our complete declarative pipeline can go in here
        agent {
            node {
                label ${pipelineParams.agent}
            }
        }

        stages {
            stage('checkout git') {
                steps {
                    git branch: pipelineParams.branch, url: pipelineParams.scmUrl
                }
            }

            stage('build') {
                steps {
                    sh "echo 'Building...'"
                }
            }

            stage ('test') {
                steps {
                    parallel (
                        "unit tests": { sh "echo 'Running unit tests...'" },
                        "integration tests": { sh "echo 'Running integration tests...'" }
                    )
                }
            }

            stage('deploy developmentServer'){
                steps {
                    sh "echo 'Deploy to ${pipelineParams.developmentServer}...'"
                }
            }

            stage('deploy staging'){
                steps {
                    sh "echo 'Deploy to ${pipelineParams.stagingServer}...'"
                }
            }

            stage('deploy production'){
                steps {
                    sh "echo 'Deploy to ${pipelineParams.productionServer}...'"
                }
            }
        }
        post {
            failure {
                mail to: pipelineParams.email, subject: 'Pipeline failed', body: "${env.BUILD_URL}"
            }
        }
    }
}

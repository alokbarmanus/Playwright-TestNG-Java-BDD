pipeline {
    agent any

    parameters {
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: 'Run the TestNG Playwright suite'
        )
        choice(
            name: 'TEST_ENV',
            choices: ['dev', 'sit', 'uat'],
            description: 'Target environment for the test suite'
        )
    }

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    environment {
        CI = 'true'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/alokbarmanus/Playwright-TestNG-Java-BDD.git'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn -B -ntp -DskipTests compile test-compile'
            }
        }

        stage('Install Playwright Browsers') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                sh 'mvn -B -ntp exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"'
            }
        }

        stage('Run Tests') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                sh "mvn -B -ntp test -Denv=${params.TEST_ENV} -Dheadless=true"
            }
            post {
                always {
                    // TestNG / Surefire XML results
                    junit testResults: 'target/surefire-reports/**/*.xml',
                          allowEmptyResults: true

                    // Cucumber HTML report
                    publishHTML(target: [
                        reportName         : 'Cucumber Report',
                        reportDir          : 'target/cucumber-reports',
                        reportFiles        : 'index.html',
                        keepAll            : true,
                        alwaysLinkToLastBuild: true,
                        allowMissing       : true
                    ])

                    // Extent HTML report
                    publishHTML(target: [
                        reportName         : 'Extent Report',
                        reportDir          : 'target/extent-reports',
                        reportFiles        : 'TestExecutionReport.html',
                        keepAll            : true,
                        alwaysLinkToLastBuild: true,
                        allowMissing       : true
                    ])

                    // TestNG HTML report
                    publishHTML(target: [
                        reportName         : 'TestNG Report',
                        reportDir          : "target/surefire-reports/Playwright-TestNG-Java-BDD",
                        reportFiles        : 'DEV_Execution.html',
                        keepAll            : true,
                        alwaysLinkToLastBuild: true,
                        allowMissing       : true
                    ])

                    // Archive all report artifacts
                    archiveArtifacts artifacts: '''
                        target/surefire-reports/**,
                        target/extent-reports/**,
                        target/cucumber-reports/**,
                        test-output/**
                    ''', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully for environment: ${params.TEST_ENV}"
        }
        failure {
            echo "Pipeline failed for environment: ${params.TEST_ENV}"
        }
        always {
            cleanWs()
        }
    }
}

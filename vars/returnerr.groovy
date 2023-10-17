pipeline {
    agent {
        node {
            label 'hwci_p01'
        }
    }

    stages {
        stage('Build') {
            steps {
                script {
                    try {
                        // Your build steps that might fail
                        sh "hello"  // This is just an example command that will fail
                    } catch (Exception e) {
                        // Handle the exception
                        echo "Error in Build stage: ${e.getMessage()}"
                        currentBuild.result = 'FAILURE'
                        return e.getMessage()
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
            }
            steps {
                echo "Deploying..."
                // Your deployment steps go here
            }
        }
    }
}
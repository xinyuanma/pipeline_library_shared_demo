#!/usr/bin/env groovy

def call(Map parameters = [:]) {
    def environment = parameters.environment ?: 'DEV'
    def recipients = parameters.recipients ?: 'xinyuan.ma@nokia.com'
    
    def emailSubject = "Build Result for ${environment} - ${currentBuild.result}"
    def emailBody = """
        Build: ${currentBuild.fullDisplayName}
        Environment: ${environment}
        Result: ${currentBuild.result}

        Check the build details at: ${env.BUILD_URL}
    """

    emailext(
        subject: emailSubject,
        body: emailBody,
        to: recipients
    )
}
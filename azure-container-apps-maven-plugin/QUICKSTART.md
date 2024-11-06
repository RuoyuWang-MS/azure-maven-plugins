# Azure Container Apps Maven Plugin Quick Start

Azure Container Apps Maven Plugin is intended to help you easily create and update Azure Container Apps.
With this tooling, you could run the deploy jobs automatically with pre defined configuration.

> NOTE:
>
> More features will be coming after the initial Private Preview announcement.

### Prerequisite

- Azure Subscription
- JDK 17 and above installed on your local machine
- Maven 3.0 and above installed on your local machine

### Build and Deploy microservices applications

1. Clone git repository by running below command.
    ```
    git clone https://github.com/spring-projects/spring-petclinic.git
    ```

1. Change directory and build the project (Optional) by running below command.
    ```
    cd spring-petclinic
    mvn clean package -DskipTests (Optional)
    ```

1. Add configuration in your pom.xml
    ```xml
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.microsoft.azure</groupId>
                <artifactId>azure-container-apps-maven-plugin</artifactId>
                <version>0.1.0</version>
                <configuration>
                    <subscriptionId>your-subscription-id</subscriptionId>
                    <resourceGroup>your-resource-group</resourceGroup>
                    <appEnvironmentName>your-app-environment-name</appEnvironmentName>
                    <region>your-region</region>
                    <appName>your-app-name</appName>
                    <containers>
                        <container>
                            <type>code</type>
                            <directory>${project.basedir}</directory>
                        </container>
                    </containers>
                    <ingress>
                        <external>true</external>
                        <targetPort>8080</targetPort>
                    </ingress>
                    <scale>
                        <minReplicas>0</minReplicas>
                        <maxReplicas>10</maxReplicas>
                    </scale>
                </configuration>
            </plugin>
         </plugins>
     </build>
    ```

1. Deploy the above apps with the following command

    ``` 
    mvn azure-container-apps:deploy
    ```

1. You may go to Azure Portal to check the deployment status.

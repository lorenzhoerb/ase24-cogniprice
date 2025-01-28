### Running PostgreSQL with Docker

To start the PostgreSQL database, run:

```bash
docker-compose --profile prod up -d
```
If not able to connect to db try looking if something already listening on :5432. Could be preinstalled postgres or wsl.
```bash
netstat -ano | findstr :5432
```
If something is already listening on this port just kill the process(command works on windows in cmd, NOT BASH)
```bash
kill -9 <PID>
```

### Setting Up and Running the Test Database 
To run the test database with a temporary, non-persistent volume, follow the steps below. This ensures that the data is discarded after the tests are completed, and no data is persisted in the database.
1. Run the Test Database
```bash
docker-compose --profile test up -d
```
This command will:
- Start the test database service defined in the docker-compose.yml file under the test profile. 
- Use a temporary volume (/tmp/test-db-data) for storing test data.
- The data will be discarded automatically when the container is stopped.
2. Stop and Remove the Test Database Container
```bash
docker-compose --profile test down -v
```
The -v flag ensures that the non-persistent volume is removed along with the container. This means:
- The test database container will be stopped
- The volume used for test data (/tmp/test-db-data) will be discarded.
- No test data will be kept after the tests are finished.

### Command for testing
```bash
mvn test -Dspring.profiles.active=test
```

### Command for testing with code coverage
```bash
mvn clean test -Dspring.profiles.active=test jacoco:report
```
The coverage file will be created in folder target/site/jacoco/index.html and can be opened in Browser.
Viewing the report in IntelliJ IDEA
Press Shift key twice and search “Import external coverage report” option in IntelliJ IDEA. 
Clicking the result should open file manager using which you can navigate and select the target/jacoco.exec file

### Command for starting application with generateData
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=generateData

```
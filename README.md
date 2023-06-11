
A java web app.

### Run Web Server

    mvn compile exec:java

### Run Gen Keys

    mvn compile exec:java@keys

### Package as Executable Jar

    mvn clean compile package

### Run Tests

    mvn test

### Benchmarks

    mvn test-compile exec:java@benchmarks

Dispatching to if-statements takes about 417.254ns/op. Using lambdas takes 1289ns/op.



A java web app.

### Run Web Server

    mvn compile exec:java

### Run Gen Keys

    mvn compile exec:java@keys

### Package as Executable Jar

    mvn clean compile package

### Run Tests

    mvn test

Specific test class:

    mvn test -Dtest=RequestAnnotationTests

Specific test in a class:

    mvn test -Dtest=RequestAnnotationTests#searchParamsCorrect

### Benchmarks

    mvn test-compile exec:java@benchmarks

Dispatching to if-statements takes about 417ns/op. Using lambdas takes 1,289ns/op.


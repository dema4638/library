FROM maven:3.6.3-jdk-11
COPY . /library
WORKDIR /library
RUN mvn assembly:assembly -DdescriptorId=jar-with-dependencies
CMD java -jar target/library-1.0-SNAPSHOT-jar-with-dependencies.jar
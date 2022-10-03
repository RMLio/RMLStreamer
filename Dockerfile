FROM eclipse-temurin:19_36-jre-alpine
RUN mkdir /opt/app
COPY target/RMLStreamer-*.jar /opt/app/RMLStreamer.jar
ENTRYPOINT ["java", "-jar", "/opt/app/RMLStreamer.jar"]

FROM eclipse-temurin:19-jre
RUN mkdir /opt/app
COPY target/RMLStreamer-*.jar /opt/app/RMLStreamer.jar
ENTRYPOINT ["java", "-jar", "/opt/app/RMLStreamer.jar"]

FROM openjdk:11
COPY ../../ /usr/src/enguage
WORKDIR /usr/src/enguage
ENV CLASSPATH=/usr/src/enguage
RUN find . -name "*.class" -exec rm {} \;
RUN javac opt/api/Server.java
ENTRYPOINT ["java", "opt.api.Server"]
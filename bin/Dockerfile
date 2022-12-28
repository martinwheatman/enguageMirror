FROM openjdk:7
COPY . /usr/src/enguage
WORKDIR /usr/src/enguage
ENV CLASSPATH=/usr/src/enguage
RUN find . -name "*.class" -exec rm {} \;
RUN javac org/enguage/Enguage.java
CMD ["java", "org.enguage.Enguage", "--data", "/var/local/eng/", "--httpd"]

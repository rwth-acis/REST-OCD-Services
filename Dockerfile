FROM openjdk:17-alpine
COPY . /usr/src/webocd
WORKDIR /usr/src/webocd
RUN apk update && apk add bash \
        coreutils \
        freetype \
        fontconfig \
        ghostscript-fonts \
        build-base 
# Fetch fitting gradle version manually because so far no os container has both jdk14 and a high enough gradle package version
RUN mkdir ../gradleFolder && wget https://services.gradle.org/distributions/gradle-7.3.2-bin.zip -P ../gradleFolder \
 && unzip -d ../gradleFolder ../gradleFolder/gradle-7.3.2-bin.zip && rm -R ../gradleFolder/gradle-7.3.2-bin.zip

# Replace HOST for database configuration (standard & for testing) with the db container name
RUN sed -i "s/^HOST.*/HOST=arangodb/" ocd/arangoDB/config.properties


# This is to clean the previously generated agents and avoid error in case WebOCD was built before image creation
RUN ../gradleFolder/gradle-7.3.2/bin/gradle clean

# Execute gradle build and tests excluding db tests
RUN ../gradleFolder/gradle-7.3.2/bin/gradle build -x test
RUN ../gradleFolder/gradle-7.3.2/bin/gradle testWithoutDB

# Add a loop that waits for arangodb to run, before webocd service starts when start_network.sh is used
RUN sed -i '2 i while ! nc -z arangodb 8529; do sleep 10; done' bin/start_network.sh
RUN chmod +x bin/start_network.sh
CMD ["bin/start_network.sh"]

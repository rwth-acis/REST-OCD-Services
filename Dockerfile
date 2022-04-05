FROM openjdk:14-alpine
COPY . /usr/src/webocd
WORKDIR /usr/src/webocd
RUN apk update && apk add bash \
        coreutils \
        freetype \
        fontconfig \
        ghostscript-fonts \
        build-base 
# Fetch fitting gradle version manually because so far no os container has both jdk14 and a high enough gradle package version
RUN mkdir ../gradleFolder && wget https://services.gradle.org/distributions/gradle-6.8.3-bin.zip -P ../gradleFolder \
 && unzip -d ../gradleFolder ../gradleFolder/gradle-6.8.3-bin.zip && rm -R ../gradleFolder/gradle-6.8.3-bin.zip
RUN ../gradleFolder/gradle-6.8.3/bin/gradle build
RUN chmod +x bin/start_network.sh
CMD ["bin/start_network.sh"]

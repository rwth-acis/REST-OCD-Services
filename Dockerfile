FROM openjdk:14-alpine
COPY . /usr/src/webocd
WORKDIR /usr/src/webocd
RUN apk update && apk add bash \
        coreutils \
        freetype \
        fontconfig \
        ghostscript-fonts \
        build-base \
		apache-ant
RUN make -C ocd/MEAs-SN  # compiling of MEA and replacing old one with it is necessary to avoid tests failing in the image
RUN mv -f ./ocd/MEAs-SN/output ./ocd/mea/MeaLinux
RUN ant -buildfile ocd_build.xml get_deps
RUN ant -buildfile ocd_build.xml all
RUN chmod +x bin/start_network.sh
CMD ["bin/start_network.sh"]

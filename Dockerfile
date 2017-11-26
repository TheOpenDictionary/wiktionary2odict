FROM openjdk:8
ENV lang en
CMD mkdir -p /wiki2odict
COPY ./build/libs/wiktionary2odict-0.0.2.jar /wiki2odict/
ENTRYPOINT java -jar /wiki2odict/wiktionary2odict-0.0.2.jar ${lang}
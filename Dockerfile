FROM clojure:openjdk-11-lein-2.9.1

WORKDIR /usr/src/app

COPY project.clj /usr/src/app/

RUN lein deps

COPY resources/proto /usr/src/app/resources/

RUN lein protoc

COPY . /usr/src/app
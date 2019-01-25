(defproject tupelo-client "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.google.protobuf/protobuf-java "3.6.1"]
                 [javax.annotation/javax.annotation-api "1.3.2"]
                 [io.grpc/grpc-core "1.18.0"]
                 [io.grpc/grpc-protobuf "1.18.0"]
                 [io.grpc/grpc-stub "1.18.0"]
                 [io.grpc/grpc-netty "1.18.0"]
                 [mvxcvi/clj-cbor "0.7.1"]]
  :plugins [[lein-protoc "0.5.0"]]
  :proto-source-paths ["resources/proto"]
  :protoc-version "3.6.1"
  :protoc-grpc {:version "1.17.1"}
  :java-source-paths ["target/generated-sources/protobuf"]
  :repl-options {:init-ns tupelo-client.core}
  :main ^:skip-aot tupelo-client.core
  :profiles {:uberjar {:aot :all}})

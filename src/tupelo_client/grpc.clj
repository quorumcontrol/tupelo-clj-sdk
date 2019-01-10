(ns tupelo-client.grpc
  (:require [clojure.string :as str])
  (:import (io.grpc ManagedChannelBuilder)
           (walletrpc WalletRPCServiceGrpc)))

(defn client* [server & [tls]]
  (let [[host port-str] (str/split server #":")
        port (Integer/parseInt port-str)
        channel (-> (ManagedChannelBuilder/forAddress host port)
                    (.usePlaintext (not tls))
                    .build)]
    (WalletRPCServiceGrpc/newBlockingStub channel)))

(def client (memoize client*))

(ns tupelo-client.core
  (:require [tupelo-client.chain-tree :as chain-tree]
            [tupelo-client.grpc :as grpc]
            [tupelo-client.key :as key]
            [tupelo-client.wallet :as wallet])
  (:import (java.util Date))
  (:gen-class))

(defn -main [& args]
  (println "Do the damn thing!")
  (let [client (grpc/client "localhost:50051")
        creds {:walletName (str "foo-" (Date.))
               :passPhrase "bar"}
        wallet-resp (wallet/register client creds)
        _ (println "Register wallet response:" (pr-str wallet-resp))
        {:keys [key-addr] :as gen-key-resp} (key/generate client creds)
        _ (println "Generate key response:" (pr-str gen-key-resp))
        {:keys [chain-tree-id] :as chain-tree-resp} (chain-tree/create client creds key-addr)
        _ (println "Create chain tree response:" (pr-str chain-tree-resp))
        set-data-resp (chain-tree/set-data client creds chain-tree-id key-addr "/foo" "bar")]
    (println "set-data response:" (pr-str set-data-resp))))


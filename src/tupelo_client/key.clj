(ns tupelo-client.key
  (:require [tupelo-client.credentials :as creds])
  (:import (com.quorumcontrol.tupelo.walletrpc
            TupeloRpc$GenerateKeyRequest
            WalletRPCServiceGrpc$WalletRPCServiceBlockingStub)))

(defn generate [^WalletRPCServiceGrpc$WalletRPCServiceBlockingStub client
                {:keys [wallet-name pass-phrase]}]
  (let [req (-> (TupeloRpc$GenerateKeyRequest/newBuilder)
                (creds/set wallet-name pass-phrase)
                .build)
        resp (.generateKey client req)]
    {:key-addr (.getKeyAddr resp)}))


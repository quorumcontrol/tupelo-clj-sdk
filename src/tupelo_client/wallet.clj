(ns tupelo-client.wallet
  (:require [tupelo-client.credentials :as creds])
  (:import (walletrpc TupeloRpc$RegisterWalletRequest
                      WalletRPCServiceGrpc$WalletRPCServiceBlockingStub)))

(defn register [^WalletRPCServiceGrpc$WalletRPCServiceBlockingStub client
                {wallet-name :walletName, pass-phrase :passPhrase}]
  (let [req (-> (TupeloRpc$RegisterWalletRequest/newBuilder)
                (creds/set wallet-name pass-phrase)
                .build)
        resp (.register client req)]
    {:wallet-name (.getWalletName resp)}))

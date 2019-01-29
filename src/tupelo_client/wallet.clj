(ns tupelo-client.wallet
  (:require [tupelo-client.credentials :as creds])
  (:import (com.quorumcontrol.tupelo.walletrpc
            TupeloRpc$RegisterWalletRequest
            WalletRPCServiceGrpc$WalletRPCServiceBlockingStub)))

(defn register [^WalletRPCServiceGrpc$WalletRPCServiceBlockingStub client
                {:keys [wallet-name pass-phrase]}]
  (let [req (-> (TupeloRpc$RegisterWalletRequest/newBuilder)
                (creds/set wallet-name pass-phrase)
                .build)
        resp (.register client req)]
    {:wallet-name (.getWalletName resp)}))


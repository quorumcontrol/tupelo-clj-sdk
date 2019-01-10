(ns tupelo-client.credentials
  (:import (com.quorumcontrol.tupelo.walletrpc TupeloRpc$Credentials))
  (:refer-clojure :exclude [set]))

(defn build [wallet-name pass-phrase]
  (-> (TupeloRpc$Credentials/newBuilder)
      (.setWalletName wallet-name)
      (.setPassPhrase pass-phrase)))

(defn set [obj wallet-name pass-phrase]
  (let [credentials (build wallet-name pass-phrase)]
    (.setCreds obj credentials)))
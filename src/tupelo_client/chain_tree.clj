(ns tupelo-client.chain-tree
  (:require [clj-cbor.core :as cbor]
            [tupelo-client.credentials :as creds]
            [clojure.string :as str])
  (:import (com.quorumcontrol.tupelo.walletrpc
            TupeloRpc$GenerateChainRequest
            WalletRPCServiceGrpc$WalletRPCServiceBlockingStub
            TupeloRpc$SetDataRequest TupeloRpc$ResolveRequest)
           (com.google.protobuf ByteString)))

(defn create [^WalletRPCServiceGrpc$WalletRPCServiceBlockingStub client
              {wallet-name :walletName, pass-phrase :passPhrase} key-addr]
  (let [req (-> (TupeloRpc$GenerateChainRequest/newBuilder)
                (creds/set wallet-name pass-phrase)
                (.setKeyAddr key-addr)
                .build)
        resp (.createChainTree client req)]
    {:chain-tree-id (.getChainId resp)}))

(defn set-data [^WalletRPCServiceGrpc$WalletRPCServiceBlockingStub client
                {wallet-name :walletName, pass-phrase :passPhrase}
                chain-id key-addr path data]
  (let [req (-> (TupeloRpc$SetDataRequest/newBuilder)
                (creds/set wallet-name pass-phrase)
                (.setChainId chain-id)
                (.setKeyAddr key-addr)
                (.setPath path)
                (.setValue (ByteString/copyFrom (cbor/encode data)))
                .build)
        resp (.setData client req)]
    {:tip (.getTip resp)}))

(defn resolve [^WalletRPCServiceGrpc$WalletRPCServiceBlockingStub client
               {wallet-name :walletName, pass-phrase :passPhrase}
               chain-id path]
  (let [req (-> (TupeloRpc$ResolveRequest/newBuilder)
                (creds/set wallet-name pass-phrase)
                (.setChainId chain-id)
                (.setPath path)
                .build)
        resp (.resolve client req)
        data {:data (-> resp .getData .newInput cbor/decode)}]
    (let [remaining-path (.getRemainingPath resp)]
      (if (not (str/blank? remaining-path))
        (assoc data :remaining-path remaining-path)
        data))))
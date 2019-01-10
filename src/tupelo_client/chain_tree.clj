(ns tupelo-client.chain-tree
  (:require [tupelo-client.credentials :as creds])
  (:import (com.quorumcontrol.tupelo.walletrpc
            TupeloRpc$GenerateChainRequest
            WalletRPCServiceGrpc$WalletRPCServiceBlockingStub
            TupeloRpc$SetDataRequest)
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
                ;; TODO: Assuming data is a String probably isn't right.
                ;; Seems likely it should be a CBOR blob instead.
                (.setValue (ByteString/copyFromUtf8 data))
                .build)
        resp (.setData client req)]
    resp))
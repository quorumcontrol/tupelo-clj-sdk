(ns tupelo-client.integration-tests
  (:require [clojure.test :refer :all]
            [tupelo-client.chain-tree :as chain-tree]
            [tupelo-client.grpc :as grpc]
            [tupelo-client.key :as key]
            [tupelo-client.wallet :as wallet])
  (:import (io.grpc StatusRuntimeException)
           (java.time Instant)))

(def client
  (delay
   (grpc/client "localhost:50051")))

(defn gen-creds []
  {:wallet-name (str "test-" (.getEpochSecond (Instant/now)) "-"
                     (rand-int 1000))
   :pass-phrase "testing"})

(deftest register-wallet-test
  (let [creds (gen-creds)]
    (testing "register a wallet succeeds"
      (is (= (:wallet-name creds)
             (->> creds
                  (wallet/register @client)
                  :wallet-name))))
    (testing "re-registering same wallet throws error"
      (is (thrown? StatusRuntimeException
                   (wallet/register @client creds))))))

(deftest generate-key-test
  (let [creds (gen-creds)]
    (testing "generating a key before registering wallet throws error"
      (is (thrown? StatusRuntimeException
                   (key/generate @client creds))))
    (testing "generating a key after registering wallet succeeds"
      (wallet/register @client creds)
      (is (re-matches #"^0x[\da-fA-F]{40}$"
                      (->> creds
                           (key/generate @client)
                           :key-addr))))))

(deftest create-chain-tree-test
  (let [creds (gen-creds)]
    (testing "creating a chain tree with wallet & key succeeds"
      (wallet/register @client creds)
      (let [{:keys [key-addr]} (key/generate @client creds)]
        (is (= (str "did:tupelo:" key-addr)
               (:chain-tree-id (chain-tree/create @client creds key-addr))))))))

(deftest set-data-and-resolve-test
  (let [creds (gen-creds)]
    (wallet/register @client creds)
    (let [{:keys [key-addr]} (key/generate @client creds)
          {:keys [chain-tree-id]} (chain-tree/create @client creds key-addr)]
      (testing "setting data at new path succeeds & is resolveable"
        (is (= "foo"
               (do
                 (chain-tree/set-data @client creds chain-tree-id key-addr
                                      "/test" "foo")
                 (:data (chain-tree/resolve @client creds chain-tree-id
                                            "/test"))))))
      (testing "setting data at deep path succeeds & is resolveable"
        (is (= "hello"
               (do
                 (chain-tree/set-data @client creds chain-tree-id key-addr
                                      "/testing/a/deep/path" "hello")
                 (:data (chain-tree/resolve @client creds chain-tree-id
                                            "/testing/a/deep/path"))))))
      (testing "setting data a third time succeeds & is resolveable"
        (is (= "out"
               (do
                 (chain-tree/set-data @client creds chain-tree-id key-addr
                                      "/peace" "out")
                 (:data (chain-tree/resolve @client creds chain-tree-id
                                            "/peace"))))))
      (testing "ovewriting existing path w/ nested data succeeds"
        (is (= "howdy"
               (do
                 (chain-tree/set-data @client creds chain-tree-id key-addr
                                      "/test/nested/path" "howdy")
                 (:data (chain-tree/resolve @client creds chain-tree-id
                                            "/test/nested/path"))))))
      (testing "add data to existing path succeeds"
        (is (= "moar"
               (do
                 (chain-tree/set-data @client creds chain-tree-id key-addr
                                      "/test/nested/other" "moar")
                 (:data (chain-tree/resolve @client creds chain-tree-id
                                            "/test/nested/other")))))))))

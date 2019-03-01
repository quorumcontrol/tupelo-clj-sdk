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
   (grpc/client
     (or (System/getenv "TUPELO_RPC_HOST") "localhost:50051"))))

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

;; TODO: Remove the `resolve` versions of these once resolve-data is released
(deftest set-data-and-resolve-test
  (let [creds (gen-creds)]
    (wallet/register @client creds)
    (let [{:keys [key-addr]} (key/generate @client creds)
          {:keys [chain-tree-id]} (chain-tree/create @client creds key-addr)]
      (testing "setting data at new path succeeds & is resolveable"
        (chain-tree/set-data @client creds chain-tree-id key-addr
                             "/test" "foo")
        (is (or
             (= "foo"
                (:data (chain-tree/resolve @client creds chain-tree-id
                                           "/test")))
             (= "foo"
                (:data (chain-tree/resolve-data @client creds chain-tree-id
                                                "/test"))))))
      (testing "setting data at deep path succeeds & is resolveable"
        (chain-tree/set-data @client creds chain-tree-id key-addr
                             "/testing/a/deep/path" "hello")
        (is (or
             (= "hello"
                (:data (chain-tree/resolve @client creds chain-tree-id
                                           "/testing/a/deep/path")))
             (= "hello"
                (:data (chain-tree/resolve-data @client creds chain-tree-id
                                                "/testing/a/deep/path"))))))
      (testing "setting data a third time succeeds & is resolveable"
        (chain-tree/set-data @client creds chain-tree-id key-addr
                             "/peace" "out")
        (is (or
             (= "out"
                (:data (chain-tree/resolve @client creds chain-tree-id
                                           "/peace")))
             (= "out"
                (:data (chain-tree/resolve-data @client creds chain-tree-id
                                                "/peace"))))))
      (testing "ovewriting existing path w/ nested data succeeds"
        (chain-tree/set-data @client creds chain-tree-id key-addr
                             "/test/nested/path" "howdy")
        (is (or
             (= "howdy"
                (:data (chain-tree/resolve @client creds chain-tree-id
                                           "/test/nested/path")))
             (= "howdy"
                (:data (chain-tree/resolve-data @client creds chain-tree-id
                                                "/test/nested/path"))))))
      (testing "add data to existing path succeeds"
        (chain-tree/set-data @client creds chain-tree-id key-addr
                             "/test/nested/other" "moar")
        (is (or
             (= "moar"
                (:data (chain-tree/resolve @client creds chain-tree-id
                                           "/test/nested/other")))
             (= "moar"
                (:data (chain-tree/resolve-data @client creds chain-tree-id
                                                "/test/nested/other")))))))))

(deftest change-chain-tree-owner-test
  (let [creds (gen-creds)]
    (wallet/register @client creds)
    (let [{:keys [key-addr]} (key/generate @client creds)
          {:keys [chain-tree-id]} (chain-tree/create @client creds key-addr)
          {new-owner-key :key-addr} (key/generate @client creds)]
      (testing "changing to one new owner succeeds"
        (let [new-tip (:tip
                       (chain-tree/change-owner @client creds chain-tree-id
                                                key-addr [new-owner-key]))]
          (is (and (string? new-tip)
                   (= 49 (count new-tip)))))))))


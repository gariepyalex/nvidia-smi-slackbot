(ns nvidia-smi-slackbot.slack
  (:require [clojure.java.io :as io]
            [org.httpkit.client :as http]
            [cheshire.core]))


(def config (read-string (slurp (io/resource "config.clj"))))


(def busy-msg
  {:attachments [{:fallback "The server's GPU is curently BUSY"
                  :color    "danger"
                  :fields   [{:title "Busy"
                              :value "The is a task runnig on the GPU"}]}]})


(def free-msg
  {:attachments [{:fallback "The server's GPU is curently FREE"
                  :color    "good"
                  :fields   [{:title "Free"
                              :value "The GPU is currently free"}]}]})


(defn- send-slack-msg
  [slack-msg]
  (http/post (:post-url config)
             {:body (cheshire.core/generate-string slack-msg)}))


(defn send-nvidia-free-msg
  []
  (send-slack-msg busy-msg))


(defn send-nvidia-busy-msg
  []
  (send-slack-msg free-msg))

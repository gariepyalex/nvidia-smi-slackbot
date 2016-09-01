(ns nvidia-smi-slackbot.slack
  (:require [clojure.java.io :as io]
            [org.httpkit.client :as http]
            [cheshire.core]))


(def config (read-string (slurp (io/resource "config.clj"))))


(defn format-process
  [{:keys [user pid memory]}]
  {:title (str "Used by " user)
   :value (format "PID %s, using %s GPU memory" pid memory)})


(defn busy-msg
  [processes]
  {:attachments [{:fallback "The server's GPU is curently BUSY"
                  :color    "danger"
                  :fields   (apply vector {:title "Busy"
                                           :value ""}
                                   (map format-process processes))}]})


(def free-msg
  {:attachments [{:fallback "The server's GPU is curently FREE"
                  :color    "good"
                  :fields   [{:title "Free"
                              :value "The GPU is currently free."}]}]})


(defn- send-slack-msg
  [slack-msg]
  (http/post (:post-url config)
             {:body (cheshire.core/generate-string slack-msg)}))


(defn send-nvidia-free-msg
  []
  (send-slack-msg free-msg))


(defn send-nvidia-busy-msg
  [processes]
  (print processes)
  (send-slack-msg (busy-msg processes)))

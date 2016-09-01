(ns nvidia-smi-slackbot.core
  (:gen-class)
  (:require [clojure.core.async :refer [chan go-loop <!]]
            [nvidia-smi-slackbot.nvidia-status :as nvidia-status]
            [nvidia-smi-slackbot.slack :as slack]))

(def nvidia-watch-chan (atom nil))
(def stop-nvidia-watch-chan (atom nil))


(defn nvidia-status-watch
  [event-fn]
  (let [c (chan)]
    (go-loop []
      (event-fn (<! c))
      (recur))
    (reset! nvidia-watch-chan c)
    (reset! stop-nvidia-watch-chan (nvidia-status/run-process-check-job c 30))))


(defn slack-status-watch
  []
  (nvidia-status-watch #(if (empty? %)
                          (slack/send-nvidia-free-msg)
                          (slack/send-nvidia-busy-msg %))))


(defn -main
  []
  (println "Starting slack bot")
  (slack-status-watch))

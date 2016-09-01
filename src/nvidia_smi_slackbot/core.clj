(ns nvidia-smi-slackbot.core
  (:require [clojure.core.async :refer [chan go-loop <!]]
            [nvidia-smi-slackbot.nvidia-status :as nvidia-status]))

(def nvidia-watch-chan (atom nil))
(def stop-nvidia-watch-chan (atom nil))

(defn nvidia-status-watch
  [event-fn]
  (let [c (chan)]
    (go-loop []
      (event-fn (<! c))
      (recur))
    (reset! nvidia-watch-chan c)
    (reset! stop-nvidia-watch-chan (nvidia-status/run-process-check-job c))))


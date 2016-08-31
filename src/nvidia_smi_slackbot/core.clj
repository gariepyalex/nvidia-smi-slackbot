(ns nvidia-smi-slackbot.core
  (:require [clojure.java.shell :as sh]
            [clojure.data.xml :as xml]
            [clojure.core.async :refer [chan go-loop >! >!! <! alts! timeout]]))

(defn- find-first
  [filter-fn collection]
  (first (filter filter-fn collection)))


(defn- nvidia-xml-has-processes?
  [xml-log]
  (->> xml-log
       (:content)
       (find-first #(= :gpu (:tag %)))
       (:content)
       (find-first #(= :processes (:tag %)))
       (:content)
       (empty?)
       (not)))

(defn- nvidia-smi-output-xml
  []
  (-> (sh/sh "nvidia-smi" "-q" "-x")
      :out
      xml/parse-str))


(defn card-runnig-processes?
  []
  (nvidia-xml-has-processes? (nvidia-smi-output-xml)))


(defn process-event
  [has-process])


(defn- exit-job?
  [stop-chan]
  (alts! stop-chan :default false))


(defn run-process-check-job
  ([out-chan]
   (run-process-check-job out-chan 15))

  ([out-chan delay-sec]
   (let [initial-value (card-runnig-processes?)
         stop-chan     (chan)]
     (>!! out-chan initial-value)
     (go-loop [has-process? initial-value]
       (when-not (exit-job? stop-chan)
         (<! (timeout (* 1000 delay-sec)))
         (if (not= has-process? (card-runnig-processes?))
           (do (>! out-chan (not has-process?))
               (recur (not has-process?)))
           (recur has-process?))))
     stop-chan)))

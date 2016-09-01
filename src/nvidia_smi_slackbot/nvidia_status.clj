(ns nvidia-smi-slackbot.nvidia-status
  (:require [clojure.java.shell :as sh]
            [clojure.data.xml :as xml]
            [clojure.core.async :refer [chan go go-loop >! >!! <! alt! timeout]]
            [clojure.string :as string]))

(defn- find-first
  [filter-fn collection]
  (first (filter filter-fn collection)))


(defn- add-user-of-process
  [{pid :pid :as process}]
  (let [ps-out (:out (sh/sh "bash" "-c" (str "ps -u -p " pid)))]
    (-> ps-out
        (string/split-lines)
        (last)
        (string/split #" ")
        (first)
        (#(assoc process :user %)))))
         

(defn- process-xml-to-map
  [xml]
  (->> xml
       (:content)
       ((fn [content]
         {:pid    (->> content (find-first #(= :pid (:tag %))) :content first)
          :memory (->> content (find-first #(= :used_memory (:tag %))) :content first)}))))

(defn- nvidia-xml-processes
  [xml-log]
  (->> xml-log
       (:content)
       (find-first #(= :gpu (:tag %)))
       (:content)
       (find-first #(= :processes (:tag %)))
       (:content)
       (map process-xml-to-map)
       (mapv add-user-of-process)))


(defn- nvidia-smi-output-xml
  []
  (-> (sh/sh "nvidia-smi" "-q" "-x")
      :out
      xml/parse-str))


(defn runnig-processes
  []
  (nvidia-xml-processes (nvidia-smi-output-xml)))


(defn run-process-check-job
  ([out-chan]
   (run-process-check-job out-chan 10))

  ([out-chan delay-sec]
   (let [initial-value (runnig-processes)
         stop-chan     (chan)]
     (>!! out-chan initial-value)
     (go-loop [processes initial-value]
       (if (alt! stop-chan true :default false)
         (println "Stopped watching Nvidia processes")

         (do (<! (timeout (* 1000 delay-sec)))
             (let [new-processes (runnig-processes)]
               (when (not= new-processes processes)
                 (>! out-chan new-processes))
               (recur new-processes)))))
     stop-chan)))

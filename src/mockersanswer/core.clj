(ns mockersanswer.core
  (:gen-class)
  (:require [clojure.data.json :as json])
  (:require [org.httpkit.client :as http])
  (:require [clojure.string :as str])
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.restful]
   [clojure.java.io :as io])
  (:import
   (twitter.callbacks.protocols SyncSingleCallback)
   (java.net URLEncoder)))


(def twtr-creds (make-oauth-creds
                 *app-consumer-key*
                 *app-consumer-secret*
                 *user-access-token*
                 *user-access-token-secret*))


(def reply-id-record-file "/tmp/mockersanwser-latest-replied")


(defn get-latest-reply-status [] (slurp reply-id-record-file))


(defn record-latest-reply-status
  [status-id]
  (with-open
      [w (io/writer reply-id-record-file)]
    (.write w status-id)))


(defn get-answer
  [question]
  (let [response
        (http/post
         *chat-api*
         {:body (json/write-str {:q question})})]
    ((json/read-str (:body @response)) "answer")))


(defn reply-tweet
  [tweet-text status-id]
  (statuses-update
   :oauth-creds twtr-creds
   :params {:in_reply_to_status_id status-id
            :status tweet-text}))


(defn reply-to-users
  [mention]

  ;; (def mention-with-randomized-char
  ;;  (str (mention :text) (str (rand-int 1000))))

  (def status-id (mention :id))

  (def user-screen-name
    (str "@" ((mention :user) :screen_name)))

  ;; Get answer from API
  (def answer (get-answer
               (clojure.string/replace
                (mention :text)
                #"@mockersanswer" "")))

  (reply-tweet
   (format "%s %s" user-screen-name answer) status-id)

  (println "reply to status id " status-id)
  (println (format "%s said: %s" user-screen-name (mention :text)))
  (println (format "Our reply: %s %s" user-screen-name answer)))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  ;; Get since id in file
  (if (.exists (as-file reply-id-record-file))
    (def since-id (str/trim-newline (get-latest-reply-status)))
    (def since-id "1"))

  (println (str "Fetching metions since " since-id))

  ;; Get mention timeline
  (def mentions
    (statuses-mentions-timeline :oauth-creds twtr-creds
                                :params {:count 20
                                         :since_id (Long/parseLong since-id)}))

  (if (> (count (mentions :body)) 0)
    (do
     ;; Record since id to file
     (println "Recording latest reply id")
     (record-latest-reply-status
      (str
       ((first (mentions :body)) :id)))

     (println (format "Got %d metions." (count (mentions :body))))

     ;; Reply users mention
     (doall (map #(reply-to-users %) (mentions :body))))
    (println "No user mentions"))
)

(ns ^{:doc    "Forwards events over SMS via Twilio"
      :author "Patrick O'Doherty"}
  riemann.twilio
  (:require [clj-http.client :as client]))

(defn- twilio-api-url
  "Generate the SMS endpoint URL"
  [account-sid]
  (str "https://api.twilio.com/2010-04-01/" account-sid "/SMS/Messages"))

(defn- format-sms [{:keys [host service state metric]}]
  (str "Host: " host
       ",\nservice: " service
       ",\nstate: " state
       ",\nmetric: " metric))

(defn- post
  "POST to the Twilio API"
  [{:keys [account-sid auth-token from-number to-number] :as conf} event]
  (client/post (twilio-api-url account-sid)
               {:basic-auth            [account-sid auth-token]
                :form-params           (assoc {:from from-number :to to-number} :body (format-sms event))
                :socket-timeout        5000
                :conn-timeout          5000
                :accept                :json
                :throw-entire-message? true}))

(defn twilio
  "Creates a Twilio SMS adapter. Takes a Twilio Account SID, auth token, from-number and to-number

  (let [sms (twilio {:account-sid \"...\"
                     :auth-token \"...\"
                     :from-number 1234567890
                     :to-number 0987654321})]
    (changed-state sms))"
  [conf]
  (fn [e] (post (conf e))))

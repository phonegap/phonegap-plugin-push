require 'rubygems'
require 'pushmeup'
GCM.host = 'https://android.googleapis.com/gcm/send'
GCM.format = :json
GCM.key = "AIzaSyB9bWG4OL5-m0eSE5PYaOhsE4lvKwpWBeg"
destination = ["APA91bHi2_juaN9NMBn8bh2rzC-VTg47E8DckJzxVyVr8zxOHI-IswZZibeyyNQo6Wj9u7XVHs_eizoILByPODGtYo71O0qjGaqOoloq6fRBc8DyhQCR1KmZY6qOlUJAKqE21pD5VGN9"]
data = {:message => "this is a test", :msgcnt => "1"}
GCM.send_notification( destination, data)

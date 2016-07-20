#!/usr/bin/env node
'use strict';

var fs = require('fs');

var getValue = function(config, name) {
    var value = config.match(new RegExp('<' + name + '>(.*?)</' + name + '>', "i"))
    if(value && value[1]) {
        return value[1]
    } else {
        return null
    }
}

function fileExists(path) {
  try  {
    return fs.statSync(path).isFile();
  }
  catch (e) {
    return false;
  }
}

function directoryExists(path) {
  try  {
    return fs.statSync(path).isDirectory();
  }
  catch (e) {
    return false;
  }
}

var config = fs.readFileSync("config.xml").toString();
var name = getValue(config, "name");

if(fileExists("GoogleService-Info.plist") && directoryExists("platforms/ios/")){
  try {
  	var contents = fs.readFileSync("GoogleService-Info.plist").toString();
    fs.writeFileSync("platforms/ios/" + name + "/Resources/GoogleService-Info.plist", contents);
  } catch(err) {
    process.stdout.write(err);
  }
}
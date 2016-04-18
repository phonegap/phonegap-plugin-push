/*global require, module*/

var fs = require('fs');

module.exports = function insertAfter(filename, after, text) {
  var data = fs.readFileSync(filename, 'utf8');

  if (data.indexOf(text) < 0) {
    data = data.replace(new RegExp(after, 'g'), after + text);
  }

  fs.writeFileSync(filename, data, 'utf8');
};

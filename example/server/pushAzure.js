/*
** Sample Table Definition - this supports the Azure Mobile Apps
** TodoItem product
** https://azure.microsoft.com/en-us/documentation/articles/app-service-mobile-cordova-get-started/
*/
var azureMobileApps = require('azure-mobile-apps'),
promises = require('azure-mobile-apps/src/utilities/promises'),
logger = require('azure-mobile-apps/src/logger');

// Create a new table definition
var table = azureMobileApps.table();

// In the TodoItem product, sends a push notification
// when a new item inserted into the table.
table.insert(function (context) {
    // For more information about the Notification Hubs JavaScript SDK,
    // see http://aka.ms/nodejshubs
    logger.info('Running TodoItem.insert');

    // Define the push notification template payload.
    // Requires template specified in the client app. See
    // https://azure.microsoft.com/en-us/documentation/articles/app-service-mobile-cordova-get-started-push/
    var payload = '{"message": "' + context.item.text + '" }';

    // Execute the insert.  The insert returns the results as a Promise,
    // Do the push as a post-execute action within the promise flow.
    return context.execute()
        .then(function (results) {
            // Only do the push if configured
            if (context.push) {

                context.push.send(null, payload, function (error) {
                    if (error) {
                        logger.error('Error while sending push notification: ', error);
                    } else {
                        logger.info('Push notification sent successfully!');
                    }
                });
            }
            // Don't forget to return the results from the context.execute()
            return results;
        })
        .catch(function (error) {
            logger.error('Error while running context.execute: ', error);
        });
});

module.exports = table;
